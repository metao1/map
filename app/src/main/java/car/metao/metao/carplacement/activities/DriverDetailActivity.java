package car.metao.metao.carplacement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.adapter.DriverAdapter.DriverAdapter;
import car.metao.metao.carplacement.model.Driver;
import car.metao.metao.carplacement.model.DriverSubItem;

import java.util.ArrayList;

/**
 * Created by metao on 2/14/2017.
 */
public class DriverDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView driverRecycleView;
    private ArrayList<DriverSubItem> driversList;
    private DriverAdapter driverAdapter;
    private Driver driver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_detail);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            driver = (Driver) extras.getSerializable("driver_info");
            if (driver != null) {
                Log.d("Driver", driver.address);
            }
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
}
