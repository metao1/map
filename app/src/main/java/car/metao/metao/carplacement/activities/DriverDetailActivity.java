package car.metao.metao.carplacement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.adapter.DriverAdapter.DriverAdapter;
import car.metao.metao.carplacement.model.Driver;
import car.metao.metao.carplacement.model.DriverSubItem;
import car.metao.metao.carplacement.mvp.ActionOnMapPresenter;

import java.util.ArrayList;

/**
 * Created by metao on 2/14/2017.
 */
public class DriverDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private RecyclerView driverRecycleView;
    private ArrayList<DriverSubItem> driversList;
    private DriverAdapter driverAdapter;
    private Driver driver;
    private ActionOnMapPresenter mapPresenter;
    private ImageView arrowBack;
    private ImageView arrowForward;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        arrowBack = (ImageView) findViewById(R.id.arrow_back);
        arrowForward = (ImageView) findViewById(R.id.arrow_forward);
        arrowBack.setOnClickListener(this);
        arrowForward.setOnClickListener(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    driver = (Driver) extras.getSerializable("driver_info");
                    mapPresenter = (ActionOnMapPresenter) extras.getSerializable("map_presenter");
                    if (driver != null) {
                        driversList.add(new DriverSubItem("Address:", driver.address));
                        driversList.add(new DriverSubItem("VIN:", driver.vin));
                        driversList.add(new DriverSubItem("Engine Type:", driver.engineType));
                        driversList.add(new DriverSubItem("Name:", driver.name));
                        driversList.add(new DriverSubItem("Interior:", driver.interior));
                        driversList.add(new DriverSubItem("Exterior:", driver.exterior));
                        driversList.add(new DriverSubItem("Fuel:", String.valueOf(driver.fuel)));
                        driverAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        driverRecycleView = (RecyclerView) findViewById(R.id.driver_recycle_view);
        driversList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driversList);
        driverRecycleView.setLayoutManager(
                new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false));
        driverRecycleView.setAdapter(driverAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(getBaseContext());
        menuInflater.inflate(R.menu.menu_search, menu);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arrow_forward:
               // mapPresenter.getPresenter().updateView(null);
                break;
            case R.id.arrow_back:
                break;
        }
    }
}
