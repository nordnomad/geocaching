package geocaching.ui;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import geocaching.GeoCache;
import geocaching.GoTo;
import geocaching.MapWrapper;
import geocaching.ui.compass.LocationAndSensorActivity;
import map.test.myapplication3.app.R;

import static geocaching.Utils.distanceToString;
import static geocaching.Utils.markerFromCache;

public class MapCompassActivity extends LocationAndSensorActivity {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    SupportMapFragment mapFragment;
    LocationSource.OnLocationChangedListener locationChangedListener;
    GeoCache geoCache;
    TextView distanceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_compass);
        if (googleMap != null) {
            setUpMap();
        } else {
            setUpMapIfNeeded();
        }
        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);
        distanceView = (TextView) findViewById(R.id.distanceView);
    }

    public void updateUI() {
        distanceView.setText(distanceToString(geoCache.location().distanceTo(currentLocation), true));
        googleMap.map.clear();
        drawMapContent();
    }

    private void setUpMap() {
        googleMap.map.setMyLocationEnabled(true);
        googleMap.map.getUiSettings().setCompassEnabled(true);
        googleMap.map.getUiSettings().setZoomControlsEnabled(false);
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            googleMap = new MapWrapper(mapFragment.getMap());
            setUpMap();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        centerMap();
        startLocationUpdates();
        googleMap.map.setMyLocationEnabled(true);
        googleMap.map.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                locationChangedListener = onLocationChangedListener;
            }

            @Override
            public void deactivate() {
                locationChangedListener = null;
            }
        });
        updateUI();
    }

    private void centerMap() {
        if (currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();
            LatLng curLoc = new LatLng(lat, lon);
            googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13.0f));
            drawMapContent();
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(curLoc);
            b.include(geoCache.latLng());
            LatLngBounds bounds = b.build();
            View view = mapFragment.getView();
            if (view != null) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, view.getWidth(), view.getHeight(), 100);
                googleMap.map.animateCamera(cu);
            }
        }
    }

    private void drawMapContent() {
        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        LatLng curLoc = new LatLng(lat, lon);
        googleMap.map.addPolyline(new PolylineOptions().add(curLoc, geoCache.latLng()).color(Color.parseColor("#F57C00")));
        googleMap.map.addMarker(markerFromCache(geoCache));
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        if (locationChangedListener != null) {
            location.setBearing(getAzimuth());
            locationChangedListener.onLocationChanged(location);
        }
        updateUI();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event);
        if (currentLocation != null) {
            currentLocation.setBearing(getAzimuth());
            locationChangedListener.onLocationChanged(currentLocation);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.map_compass_activity_action_bar, menu);
        MenuItem centerMap = menu.findItem(R.id.center_map);
        //TODO implement action in future
        centerMap.setVisible(false);
        centerMap.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                centerMap();
                return true;
            }
        });
        MenuItem findCacheItem = menu.findItem(R.id.find_cache_map);
        findCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                GoTo.compassActivity(MapCompassActivity.this, geoCache);
                return true;
            }
        });
        return true;
    }
}
