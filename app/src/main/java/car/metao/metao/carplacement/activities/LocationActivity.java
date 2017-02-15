package car.metao.metao.carplacement.activities;

/**
 * Created by metao on 2/14/2017.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.fragments.DriverDetailFragment;
import car.metao.metao.carplacement.model.Driver;
import car.metao.metao.carplacement.model.PlaceMark;
import car.metao.metao.carplacement.mvp.ActionOnMapPresenter;
import car.metao.metao.carplacement.mvp.MapListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.metao.async.repository.Repository;
import com.metao.async.repository.RepositoryCache;
import com.metao.async.repository.RepositoryCallback;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class LocationActivity extends FragmentActivity implements
        LocationListener, MapListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String API_END_POINT = "https://s3-us-west-2.amazonaws.com/wunderbucket/locations";
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute
    private static Repository<PlaceMark> placeMarkRepository;
    private static ArrayList<Driver> placemarksHolder;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private GoogleMap googleMap;
    private Activity activityCompat;
    private DriverDetailFragment driverDetailFragment;
    private ActionOnMapPresenter actionOnMapPresenter;
    private MessageHandler messageHandler;
    private static int driverIndex;
    private RepositoryCache<String, Marker> markerMapCache;
    private RepositoryCache<String, Driver> driverMapCache;
    private RepositoryCache<String, String> driverMarkerMapCache;
    private RepositoryCache<String, String> markerDriverMapCache;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        setupFragment();
        activityCompat = this;
    }

    private void setupFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        driverDetailFragment = new DriverDetailFragment();
        fragmentManager.beginTransaction()
                .add(R.id.container, driverDetailFragment, "driver_detail_fragment")
                .addToBackStack("location").commit();
        actionOnMapPresenter = new ActionOnMapPresenter();
        actionOnMapPresenter.setView(this);//Set The Presenter
        driverDetailFragment.setPresenter(actionOnMapPresenter);
        messageHandler = new MessageHandler();
        markerDriverMapCache = new RepositoryCache<>(1024);
        driverMarkerMapCache = new RepositoryCache<>(1024);
        markerMapCache = new RepositoryCache<>(1024);
        driverMapCache = new RepositoryCache<>(1024);
    }

    private void initRepository() {
        placeMarkRepository = new Repository<PlaceMark>("PlaceMarkRepository") {
            static final long RAM_SIZE = 40 * 1024 * 1024;// 40MiB Repo Size

            @Override
            public long getRamUsedInBytes() {
                return RAM_SIZE;
            }

            @Override
            public RepositoryType repositoryType() {
                return RepositoryType.JSON;
            }
        };
    }

    private void callService() {
        placeMarkRepository.addService(API_END_POINT, new RepositoryCallback<PlaceMark>() {
            @Override
            public void onDownloadFinished(String urlAddress, PlaceMark placeMark) {
                if (placeMark != null) {
                    ArrayList<Driver> placemarks = placeMark.placemarks;
                    if (placemarks != null && placemarks.size() > 0) {
                        placemarksHolder = placeMark.placemarks;
                        createMap(placemarks);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(getBaseContext(), "Can't connect to " + throwable.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void createMap(ArrayList<Driver> placemarks) {
        for (Driver placemark : placemarks) {
            double lat = placemark.coordinates[0];
            double lon = placemark.coordinates[1];
            addMarker(placemark, placemark.name, lat, lon);
        }
        if (placemarks.size() > 0) {
            double lat = placemarks.get(0).coordinates[0];
            double lon = placemarks.get(0).coordinates[1];
            LatLng currentLatLng = new LatLng(lat, lon);
            animateTo(currentLatLng, 13, 1, 1, 1000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fm.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gm) {
                googleMap = gm;
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setOnMarkerClickListener(new MarkerClickHandler());
                if (placeMarkRepository == null) {
                    initRepository();
                    callService();
                } else {
                    createMap(placemarksHolder);
                }
            }
        });
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        addMarker(null, "My Location", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    }

    private void addMarker(Driver driver, String title, double lat, double lon) {
        MarkerOptions options = new MarkerOptions();
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        LatLng currentLatLng = new LatLng(lat, lon);
        options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        if (driver != null) {
            markerDriverMapCache.put(mapMarker.getId(), driver.vin);
            driverMarkerMapCache.put(driver.vin, mapMarker.getId());
            markerMapCache.put(mapMarker.getId(), mapMarker);
            driverMapCache.put(mapMarker.getId(), driver);
        }
        if (mCurrentLocation != null) {
            long atTime = mCurrentLocation.getTime();
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
            mapMarker.setTitle(mLastUpdateTime);
            animateTo(mapMarker.getPosition(), 15, 2, 1, 1000);
        }
        mapMarker.setTitle(title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        driverDetailFragment.show(false);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG, "Location update stopped .......................");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void backDriver() {
        if (driverIndex + 1 < placemarksHolder.size()) {
            Driver driver = placemarksHolder.get(driverIndex++);
            String markerId = driverMarkerMapCache.get(driver.vin);
            if (markerId != null) {
                sendDriverToMessageHandler(markerId);
            }
        }
    }

    @Override
    public void nextDriver() {
        if (driverIndex - 1 >= 0) {
            Driver driver = placemarksHolder.get(driverIndex--);
            String markerId = driverMarkerMapCache.get(driver.vin);
            if (markerId != null) {
                sendDriverToMessageHandler(markerId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class MessageHandler extends Handler {

        MessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            String markerId = (String) msg.obj;
            if (markerId != null) {
                Driver driver = driverMapCache.get(markerId);
                Marker marker = markerMapCache.get(markerId);
                if (marker != null) {
                    animateTo(marker.getPosition(), 15, 2, 1, 1000);
                }
                if (driver != null) {
                    driverDetailFragment.show(true);
                    driverDetailFragment.getResultPresenter().getPresenter().updateView(driver);
                }
            }
        }
    }

    private void animateTo(LatLng latLng, double zoom, double bearing, double tilt, final int milliseconds) {
        if (markerMapCache == null) return;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) zoom));
        googleMap.animateCamera(CameraUpdateFactory
                .scrollBy(250 - (float) Math.random()
                        * 500 - 250, 250 - (float) Math.random()
                        * 500), milliseconds, null);
    }

    private class MarkerClickHandler implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    sendDriverToMessageHandler(marker.getId());
                }
            });
            return true;
        }
    }

    private void sendDriverToMessageHandler(String markerId) {
        Message message = new Message();
        message.obj = markerId;
        messageHandler.sendMessage(message);
    }
}