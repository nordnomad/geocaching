package geocaching.ui.compass;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.text.DateFormat;
import java.util.Date;

import geocaching.GeoCache;
import geocaching.GoTo;
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
    GeoCache geoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        distanceView = (TextView) findViewById(R.id.distance_compass_view);
        accuracyView = (TextView) findViewById(R.id.accuracy_compass_view);
        myLocationView = (TextView) findViewById(R.id.my_location_view);
        cacheLocationView = (TextView) findViewById(R.id.cache_location_view);
        compassView = (CompassView) findViewById(R.id.compassView);

        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);
        cacheLocationView.setText(coordinateToString(geoCache.location()));
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
        distanceView.setText(distanceToString(geoCache.location().distanceTo(currentLocation), true));
        accuracyView.setText(String.format("+/- %s", distanceToString(currentLocation.getAccuracy(), true)));
        myLocationView.setText(coordinateToString(currentLocation));
        compassView.initializeCompass(currentLocation, geoCache.location(), R.drawable.compass_background);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.compass_activity_action_bar, menu);
        MenuItem findCacheItem = menu.findItem(R.id.find_cache_map);
        findCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                GoTo.compassMapActivity(CompassActivity.this, geoCache);
                return true;
            }
        });
        return true;
    }

}
