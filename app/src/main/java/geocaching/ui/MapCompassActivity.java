package geocaching.ui;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

import static geocaching.Utils.markerFromCache;

public class MapCompassActivity extends LocationAndSensorActivity {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    SupportMapFragment mapFragment;
    LocationSource.OnLocationChangedListener locationChangedListener;
    Location geoCacheLocation = new Location("geoCacheLocation");
    GeoCache geoCache;

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
        geoCacheLocation.setLongitude(geoCache.ln);
        geoCacheLocation.setLatitude(geoCache.la);
//      cacheLocationView.setText(coordinateToString(geoCacheLocation));
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
    }

    private void centerMap() {
        if (currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();
            LatLng curLoc = new LatLng(lat, lon);
            googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13.0f));
            googleMap.map.addPolyline(new PolylineOptions().add(curLoc, geoCache.location()).color(Color.parseColor("#F57C00")));
            googleMap.map.addMarker(markerFromCache(geoCache));
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(curLoc);
            b.include(geoCache.location());
            LatLngBounds bounds = b.build();
            View view = mapFragment.getView();
            if (view != null) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, view.getWidth(), view.getHeight(), 100);
                googleMap.map.animateCamera(cu);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        if (locationChangedListener != null) {
            location.setBearing(getAzimuth());
            locationChangedListener.onLocationChanged(location);
        }
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
