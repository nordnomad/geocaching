package geocaching.ui;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import geocaching.GeoCache;
import geocaching.GoTo;
import geocaching.MapWrapper;
import geocaching.db.DB;
import geocaching.db.GeoCacheProvider;
import geocaching.tasks.LoadCachesTask;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static geocaching.Const.M.fullInfoUrl;
import static geocaching.Utils.geoCacheToContentValues;
import static geocaching.Utils.jsonGeoCacheToContentValues;

public class MapScreen extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    View markerInfo;
    GoogleApiClient gapiClient;
    FragmentManager fragmentManager;
    Location lastLocation;
    RequestQueue queue;

    @Override
    public void onLocationChanged(Location l) {
        lastLocation = l;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        gapiClient = new Builder(getActivity())
                .addApi(API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        queue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onConnected(Bundle bundle) {
        lastLocation = FusedLocationApi.getLastLocation(gapiClient);
        if (lastLocation != null) {
            googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13.0f));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
        Log.i(getActivity().getLocalClassName(), "Connection suspended");
        gapiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(getActivity().getLocalClassName(), "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Toast.makeText(getActivity(), "onConnectionFailed.", Toast.LENGTH_SHORT).show();
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            googleMap = new MapWrapper(mapFragment.getMap());
            setUpMap();
        }
    }

    private void setUpMap() {
        googleMap.map.setMyLocationEnabled(true);
        googleMap.map.getUiSettings().setCompassEnabled(true);
        googleMap.map.getUiSettings().setZoomControlsEnabled(false);
        googleMap.map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                new LoadCachesTask(googleMap).execute(googleMap.map.getProjection().getVisibleRegion().latLngBounds);
            }
        });
        googleMap.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                if (markerInfo.getVisibility() == View.GONE) {
                    markerInfo.setVisibility(View.VISIBLE);
                }

                Location location = new Location("Test");
                location.setLatitude(marker.getPosition().latitude);
                location.setLongitude(marker.getPosition().longitude);

                float distanceTo = lastLocation.distanceTo(location);

                TextView textView = (TextView) markerInfo.findViewById(R.id.nameView);
                textView.setText(marker.getTitle());

                TextView distanceView = (TextView) markerInfo.findViewById(R.id.distanceLabel);
                distanceView.setText(String.format("%.1f км", distanceTo / 1000.0));

                final GeoCache geoCache = googleMap.inverseMap().get(marker);
                TextView geoCacheTypeView = (TextView) markerInfo.findViewById(R.id.geoCacheTypeView);
                geoCacheTypeView.setText(geoCache.type.title);

                ImageButton findBtn = (ImageButton) markerInfo.findViewById(R.id.findBtn);
                findBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location cacheLocation = new Location("");
                        cacheLocation.setLatitude(geoCache.la);
                        cacheLocation.setLongitude(geoCache.ln);
                        GoTo.compassActivity(getActivity(), geoCache.id, geoCache.name, lastLocation, cacheLocation);
                    }
                });

                final ImageButton saveBtn = (ImageButton) markerInfo.findViewById(R.id.saveBtn);
                final ImageButton deleteBtn = (ImageButton) markerInfo.findViewById(R.id.deleteBtn);

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                        resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheToContentValues(geoCache));
                        deleteBtn.setVisibility(View.VISIBLE);
                        saveBtn.setVisibility(View.GONE);
                    }
                });

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                        resolver.delete(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCache.id), null, null);
                        saveBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.GONE);
                    }
                });

                ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                int count = 0;
                try (Cursor countCursor = resolver.query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCache.id),
                        new String[]{"count(*) AS count"}, null, null, null)) {
                    if (countCursor != null) {
                        countCursor.moveToFirst();
                        count = countCursor.getInt(0);
                    }
                }
                if (count > 0) {
                    deleteBtn.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.GONE);
                } else {
                    saveBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.GONE);
                }

                markerInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GoTo.geoCacheActivity(getActivity(), geoCache.id, geoCache.name);
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
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_screen_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.map_save);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                item.setActionView(R.layout.actionbar_save_progress);
                item.expandActionView();

//                Set<GeoCache> geoCaches = googleMap.markerGeoCaches.keySet();
//                for (GeoCache geoCache : geoCaches) {
//                    ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
//                    resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheToContentValues(geoCache));
//                }

                List<Long> excludedIds = new ArrayList<>();
                try (Cursor cursor = getActivity().getContentResolver().query(GeoCacheProvider.GEO_CACHE_CONTENT_URI, new String[]{DB.Column._ID}, null, null, null)) {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            excludedIds.add(cursor.getLong(0));
                        }
                    }
                }
                if (excludedIds.isEmpty()) excludedIds.add(0l);
                LatLngBounds bounds = googleMap.map.getProjection().getVisibleRegion().latLngBounds;
                double nLat = bounds.northeast.latitude;
                double nLon = bounds.northeast.longitude;
                double sLat = bounds.southwest.latitude;
                double sLon = bounds.southwest.longitude;
                DefaultRetryPolicy policy = new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                queue.add(new JsonArrayRequest(fullInfoUrl(nLon, sLon, nLat, sLat, excludedIds),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject jsonObject = response.getJSONObject(i);
                                        resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, jsonGeoCacheToContentValues(jsonObject));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                item.collapseActionView();
                                item.setActionView(null);
                                Toast.makeText(getActivity(), response.length() + " caches was saved", Toast.LENGTH_LONG).show();
                            }
                        }
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        item.collapseActionView();
                        item.setActionView(null);
                        String message = TextUtils.isEmpty(error.getMessage()) ? "0 caches was saved" : error.getMessage();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                }).setRetryPolicy(policy));

                return true;
            }
        });
    }

}
