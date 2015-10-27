package geocaching.ui.compass;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.text.DateFormat;
import java.util.Date;

import map.test.myapplication3.app.R;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static geocaching.Utils.coordinateToString;
import static geocaching.Utils.distanceToString;

public class CompassActivity extends CompassSensorsActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    protected static final String TAG = "location-updates";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    CompassView compassView;
    TextView distanceView;
    TextView accuracyView;
    TextView myLocationView;
    TextView cacheLocationView;

    GoogleApiClient gapiClient;
    LocationRequest locationRequest;
    Location currentLocation;
    String lastUpdateTime;
    Location geoCacheLocation = new Location("geoCacheLocation");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        distanceView = (TextView) findViewById(R.id.distance_compass_view);
        accuracyView = (TextView) findViewById(R.id.accuracy_compass_view);
        myLocationView = (TextView) findViewById(R.id.my_location_view);
        cacheLocationView = (TextView) findViewById(R.id.cache_location_view);

        compassView = (CompassView) findViewById(R.id.compassView);

        setTitle(getIntent().getStringExtra("name"));
        double[] ol = getIntent().getDoubleArrayExtra("objectLocation");
        if (ol != null) {
            geoCacheLocation.setLongitude(ol[0]);
            geoCacheLocation.setLatitude(ol[1]);
            cacheLocationView.setText(coordinateToString(geoCacheLocation));
        }
        lastUpdateTime = "";
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        gapiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        FusedLocationApi.requestLocationUpdates(gapiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (gapiClient.isConnected()) {
            FusedLocationApi.removeLocationUpdates(gapiClient, this);
        }
    }

    private void updateUI() {
        distanceView.setText(distanceToString(geoCacheLocation.distanceTo(currentLocation), true));
        accuracyView.setText(String.format("+/- %s", distanceToString(currentLocation.getAccuracy(), true)));
        myLocationView.setText(coordinateToString(currentLocation));
        //TODO why I need here 2 points? may be remove geoCacheLocation
        compassView.initializeCompass(currentLocation, geoCacheLocation, R.drawable.img_compass);
//        compassView.initializeCompass(currentLocation, geoCacheLocation, R.drawable.img_compass);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        gapiClient.connect();
    }

    @Override
    public void onStop() {
        if (gapiClient.isConnected()) {
            gapiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (currentLocation == null) {
            currentLocation = FusedLocationApi.getLastLocation(gapiClient);
            lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gapiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        gapiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Toast.makeText(this, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }
}
