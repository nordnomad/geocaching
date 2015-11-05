package geocaching.ui;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.redinput.compassview.CompassView;

import geocaching.MapWrapper;
import map.test.myapplication3.app.R;

import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MapCompassActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    View markerInfo;
    GoogleApiClient gapiClient;
    FragmentManager fragmentManager;
    Location lastLocation;
    SupportMapFragment mapFragment;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_compass);

        CompassView compass = (CompassView) findViewById(R.id.compass);

        compass.setDegrees(57);
        compass.setShowMarker(true);
        compass.setRangeDegrees(270);
        compass.setOnCompassDragListener(new CompassView.OnCompassDragListener() {
            @Override
            public void onCompassDragListener(float degrees) {
                Toast.makeText(MapCompassActivity.this, degrees + "", Toast.LENGTH_SHORT).show();
            }
        });
        gapiClient = new GoogleApiClient.Builder(this)
                .addApi(API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
        lastLocation = FusedLocationApi.getLastLocation(gapiClient);
        if (lastLocation != null) {
            double lat = lastLocation.getLatitude();
            double lon = lastLocation.getLongitude();
            googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13.0f));
            googleMap.map.addPolyline(new PolylineOptions().add(new LatLng(lat, lon), new LatLng(lat - 0.01, lon - 0.01)));
            googleMap.map.addMarker(new MarkerOptions().position(new LatLng(lat - 0.01, lon - 0.01)));
            LatLngBounds.Builder b = new LatLngBounds.Builder();

            b.include(new LatLng(lat, lon));
            b.include(new LatLng(lat - 0.01, lon - 0.01));
            LatLngBounds bounds = b.build();
            View view = mapFragment.getView();
//Change the padding as per needed
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, view.getWidth(), view.getHeight(), 50);
            googleMap.map.animateCamera(cu);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
        Log.i(this.getLocalClassName(), "Connection suspended");
        gapiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(this.getLocalClassName(), "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Toast.makeText(this, "onConnectionFailed.", Toast.LENGTH_SHORT).show();
    }
}
