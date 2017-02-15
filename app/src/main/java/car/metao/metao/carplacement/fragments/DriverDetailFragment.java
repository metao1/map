package car.metao.metao.carplacement.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.adapter.DriverAdapter.DriverAdapter;
import car.metao.metao.carplacement.model.Driver;
import car.metao.metao.carplacement.model.DriverSubItem;
import car.metao.metao.carplacement.mvp.ActionOnMapPresenter;
import car.metao.metao.carplacement.mvp.MapUpdateListener;
import car.metao.metao.carplacement.mvp.ResultOnMapPresenter;

import java.util.ArrayList;

/**
 * Created by metao on 2/15/2017.
 */
public class DriverDetailFragment extends Fragment implements View.OnClickListener, MapUpdateListener {

    private View rootView;
    private Toolbar toolbar;
    private RecyclerView driverRecycleView;
    private ArrayList<DriverSubItem> driversList;
    private DriverAdapter driverAdapter;
    private Driver driver;
    private ImageView arrowBack;
    private ImageView arrowForward;
    private ActionOnMapPresenter presenter;
    private ResultOnMapPresenter resultOnMapPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.driver_detail, container, false);
        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        driverRecycleView = (RecyclerView) rootView.findViewById(R.id.driver_recycle_view);
        arrowBack = (ImageView) rootView.findViewById(R.id.arrow_back);
        arrowForward = (ImageView) rootView.findViewById(R.id.arrow_forward);
        arrowBack.setOnClickListener(this);
        arrowForward.setOnClickListener(this);
        driversList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driversList);
        driverRecycleView.setLayoutManager(
                new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, false));
        driverRecycleView.setAdapter(driverAdapter);
        resultOnMapPresenter = new ResultOnMapPresenter();
        resultOnMapPresenter.setView(this);//Set The Presenter
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        Log.d("issue", "called");
        menuInflater.inflate(R.menu.menu_search, menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show(false);
            }
        });
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    public void setPresenter(ActionOnMapPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arrow_forward:
                presenter.getPresenter().nextDriver();
                break;
            case R.id.arrow_back:
                presenter.getPresenter().backDriver();
                break;
        }
    }

    @Override
    public void updateView(final Driver driver) {
        if (driver != null) {
            driversList.clear();
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

    public ResultOnMapPresenter getResultPresenter() {
        return resultOnMapPresenter;
    }

    public void show(boolean show) {
        if (show) {
            rootView.setVisibility(View.VISIBLE);
        } else {
            rootView.setVisibility(View.GONE);
        }
    }
}
