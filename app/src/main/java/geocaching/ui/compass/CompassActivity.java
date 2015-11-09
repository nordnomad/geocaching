package geocaching.ui.compass;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

import geocaching.GeoCache;
import geocaching.GoTo;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static geocaching.Utils.coordinateToString;
import static geocaching.Utils.distanceToString;

public class CompassActivity extends LocationAndSensorActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    CompassView compassView;
    TextView distanceView;
    TextView accuracyView;
    TextView myLocationView;
    TextView cacheLocationView;

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
    }

    private void updateUI() {
        distanceView.setText(distanceToString(geoCache.location().distanceTo(currentLocation), true));
        accuracyView.setText(String.format("+/- %s", distanceToString(currentLocation.getAccuracy(), true)));
        myLocationView.setText(coordinateToString(currentLocation));
        compassView.initializeCompass(currentLocation, geoCache.location(), R.drawable.compass_background);
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        updateUI();
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
