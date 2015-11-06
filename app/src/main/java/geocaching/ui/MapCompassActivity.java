package geocaching.ui;

import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import geocaching.MapWrapper;
import geocaching.ui.compass.MapCompassSensorActivity;
import map.test.myapplication3.app.R;

public class MapCompassActivity extends MapCompassSensorActivity {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    View markerInfo;
    SupportMapFragment mapFragment;
    LocationSource.OnLocationChangedListener locationChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_compass);
        if (googleMap != null) {
            setUpMap();
        } else {
            setUpMapIfNeeded();
        }
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
        if (currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();
            googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13.0f));
            googleMap.map.addPolyline(new PolylineOptions().add(new LatLng(lat, lon), new LatLng(lat - 0.01, lon - 0.01)));
            googleMap.map.addMarker(new MarkerOptions().position(new LatLng(lat - 0.01, lon - 0.01)));
            LatLngBounds.Builder b = new LatLngBounds.Builder();

            b.include(new LatLng(lat, lon));
            b.include(new LatLng(lat - 0.01, lon - 0.01));
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
            LatLngBounds bounds = b.build();
            View view = mapFragment.getView();
//Change the padding as per needed
            if (view != null) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, view.getWidth(), view.getHeight(), 50);
                googleMap.map.animateCamera(cu);
            }
            startLocationUpdates();
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
}
