package car.metao.metao.carplacement.activities;

/**
 * Created by metao on 2/14/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.model.Driver;
import car.metao.metao.carplacement.model.PlaceMark;
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
import com.metao.async.repository.RepositoryCallback;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class LocationActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String API_END_POINT = "http://192.168.1.3/website/v5/drivers.php";
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute
    public ArrayList<Driver> placemarks;
    private Repository<PlaceMark> placeMarkRepository;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    GoogleMap googleMap;
    Activity activityCompat;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_maps);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fm.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gm) {
                googleMap = gm;
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                /*Location location = new Location("");
                location.setLatitude(sydney.latitude);
                location.setLongitude(sydney.longitude);
                onLocationChanged(location);*/
                googleMap.setOnMarkerClickListener(new MarkerClickHandler());
                if (placeMarkRepository == null) {
                    initRepository();
                    callService();
                }
            }
        });
        activityCompat = this;
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
                        placemarks = placeMark.placemarks;
                        for (Driver placemark : placemarks) {
                            double lat = placemark.coordinates[0];
                            double lon = placemark.coordinates[1];
                            addMarker(placemark, placemark.name, lat, lon);
                        }
                        if (placemarks.size() > 0) {
                            double lat = placemarks.get(0).coordinates[0];
                            double lon = placemarks.get(0).coordinates[1];
                            LatLng currentLatLng = new LatLng(lat, lon);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("repo", placeMarkRepository);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
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

    private void addMarker(Driver placemark, String title, double lat, double lon) {
        MarkerOptions options = new MarkerOptions();
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        LatLng currentLatLng = new LatLng(lat, lon);
        options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        if (placemark != null) {
            mapMarker.setTag(placemark);
        }
        if (mCurrentLocation != null) {
            long atTime = mCurrentLocation.getTime();
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
            mapMarker.setTitle(mLastUpdateTime);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
        }
        mapMarker.setTitle(title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    private class MarkerClickHandler implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 20));
                    Intent intent = new Intent(activityCompat, DriverDetailActivity.class);
                    Bundle bundle = new Bundle();
                    Driver driver = (Driver) marker.getTag();
                    bundle.putSerializable("driver_info", driver);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            return true;
        }
    }
}