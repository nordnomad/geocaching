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
import java.util.Set;

import map.test.myapplication3.app.R;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static geocaching.Utils.coordinateToString;
import static geocaching.Utils.distanceToString;

public class CompassActivity extends CompassSensorsActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    protected static final String TAG = "location-updates-sample";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    CompassView compassView;
    TextView distanceView;
    TextView accuracyView;
    TextView myLocationView;
    TextView cacheLocationView;

    GoogleApiClient gapiClient;
    LocationRequest locationRequest;
    Location currentLocation;
    Boolean requestingLocationUpdates;
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
        requestingLocationUpdates = false;
        lastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            Set<String> savedKeys = savedInstanceState.keySet();
            if (savedKeys.contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedKeys.contains(LOCATION_KEY)) {
                currentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedKeys.contains(LAST_UPDATED_TIME_STRING_KEY)) {
                lastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
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
        if (gapiClient.isConnected())
            FusedLocationApi.removeLocationUpdates(gapiClient, this);
    }

    private void updateUI() {
        distanceView.setText(distanceToString(geoCacheLocation.distanceTo(currentLocation), true));
        accuracyView.setText("+/- " + distanceToString(currentLocation.getAccuracy(), true));
        myLocationView.setText(coordinateToString(currentLocation));
        //TODO why I need here 2 ponts? may be remove geoCacheLocation
        compassView.initializeCompass(currentLocation, geoCacheLocation, R.drawable.img_compass);
//        compassView.initializeCompass(currentLocation, geoCacheLocation, R.drawable.img_compass);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Toast.makeText(this, "Location canged", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        gapiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (gapiClient.isConnected()) {
            gapiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (currentLocation == null) {
            currentLocation = FusedLocationApi.getLastLocation(gapiClient);
            lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gapiClient.isConnected() && !requestingLocationUpdates) {
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
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, currentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, lastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }
}
