package geocaching.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import geocaching.GeoCache;
import geocaching.LoadCachesTask;
import geocaching.MapWrapper;
import geocaching.db.DB;
import geocaching.db.GeoCacheProvider;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

public class MapScreen extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    LocationClient locationClient;
    View markerInfo;

    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locationClient = new LocationClient(getActivity(), this, this);
        locationClient.connect();
        return inflater.inflate(R.layout.map_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        markerInfo = view.findViewById(R.id.markerInfo);
        if (googleMap != null) {
            setUpMap();
        } else {
            setUpMapIfNeeded();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (googleMap != null) {
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.findFragmentById(R.id.map))
                    .commit();
            googleMap = null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
        Location l = locationClient.getLastLocation();
        googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 14.0f));
        googleMap.map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                new LoadCachesTask(googleMap).execute(googleMap.map.getProjection().getVisibleRegion().latLngBounds);
            }
        });
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Failed.", Toast.LENGTH_SHORT).show();
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            googleMap = new MapWrapper(((SupportMapFragment) fragmentManager.findFragmentById(R.id.map)).getMap());
            if (googleMap != null) setUpMap();
        }
    }

    private void setUpMap() {
        googleMap.map.setMyLocationEnabled(true);
        googleMap.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (markerInfo.getVisibility() == View.GONE) {
                    markerInfo.setVisibility(View.VISIBLE);
                }

                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
                Location location = new Location("Test");
                location.setLatitude(marker.getPosition().latitude);
                location.setLongitude(marker.getPosition().longitude);

                float distanceTo = locationClient.getLastLocation().distanceTo(location);

                TextView textView = (TextView) markerInfo.findViewById(R.id.nameView);
                textView.setText(marker.getTitle() + " " + String.format("%.1f km", distanceTo / 1000.0));

                ImageButton findBtn = (ImageButton) markerInfo.findViewById(R.id.findBtn);
                findBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CompassActivity.class);
                        startActivity(intent);
                    }
                });

                ImageButton saveBtn = (ImageButton) markerInfo.findViewById(R.id.saveBtn);
                final GeoCache geoCache = googleMap.inverseMap().get(marker);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        cv.put(DB.Column._ID, geoCache.id);
                        cv.put(DB.Column.NAME, geoCache.name);
                        cv.put(DB.Column.LAT, geoCache.la);
                        cv.put(DB.Column.LON, geoCache.ln);
                        cv.put(DB.Column.STATUS, geoCache.status.ordinal());
                        cv.put(DB.Column.TYPE, geoCache.type.ordinal());

                        ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                        resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, cv);
                    }
                });

                return true; // to prevent center marker on screen
            }
        });
        googleMap.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerInfo.getVisibility() == View.VISIBLE) {
                    markerInfo.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
