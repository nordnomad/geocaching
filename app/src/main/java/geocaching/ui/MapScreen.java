package geocaching.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import geocaching.GeoCache;
import geocaching.GoTo;
import geocaching.MapWrapper;
import geocaching.Utils;
import geocaching.managers.Network;
import geocaching.managers.Storage;
import geocaching.services.GCDownloadService;
import geocaching.tasks.LoadCachesTask;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static geocaching.Const.PREFS_KEY_BULK_SAVE_PROGRESS;
import static geocaching.Const.PREFS_NAME;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_ERROR;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_FINISHED;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_STARTED;
import static geocaching.services.GCDownloadService.Constants.KEY_STATUS;

public class MapScreen extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener/*, GeoCacheDownloadReceiver.Receiver*/ {
    MapWrapper googleMap; // Might be null if Google Play services APK is not available.
    View markerInfo;
    GoogleApiClient gapiClient;
    FragmentManager fragmentManager;
    Location lastLocation;
    MenuItem saveMenuItem;

    GCDownloadReceiver downloadStateReceiver = new GCDownloadReceiver();
    IntentFilter downloadStateIntentFilter = new IntentFilter(GCDownloadService.Constants.BROADCAST_ACTION);

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
        buildGoogleApiClient();
        downloadStateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    }

    protected synchronized void buildGoogleApiClient() {
        gapiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(API)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(downloadStateReceiver, downloadStateIntentFilter);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCameraPosition();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(downloadStateReceiver);
    }

    CameraPosition cameraPosition;

    private void saveCameraPosition() {
        cameraPosition = googleMap.map.getCameraPosition();
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
            if (cameraPosition != null) {
                googleMap.map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                cameraPosition = null;
            } else {
                googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13.0f));
            }
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

    LatLngBounds previousBounds;
    LoadCachesTask previousLoadCachesTask;

    private void setUpMap() {
        googleMap.map.setMyLocationEnabled(true);
        googleMap.map.getUiSettings().setCompassEnabled(true);
        googleMap.map.getUiSettings().setZoomControlsEnabled(false);
        googleMap.map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom < 10) return;
                LatLngBounds currentProjection = googleMap.map.getProjection().getVisibleRegion().latLngBounds;
                boolean contains = false;
                if (previousBounds != null) {
                    contains = previousBounds.contains(currentProjection.northeast)
                            && previousBounds.contains(currentProjection.southwest);
                }
                if (!contains) {
                    previousBounds = googleMap.map.getProjection().getVisibleRegion().latLngBounds;
                    if (cameraPosition == null) {
                        if (previousLoadCachesTask != null && !previousLoadCachesTask.isCancelled()) {
                            previousLoadCachesTask.cancel(true);
                            previousLoadCachesTask = null;
                        }
                        previousLoadCachesTask = new LoadCachesTask(googleMap);
                        previousLoadCachesTask.execute(googleMap.map.getProjection().getVisibleRegion().latLngBounds);
                    }
                }
            }
        });
        googleMap.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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

                switch (geoCache.type) {
                    case TRADITIONAL:
                        geoCacheTypeView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_traditional));
                        break;
                    case STEP_BY_STEP_TRADITIONAL:
                        geoCacheTypeView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_traditional_step_by_step));
                        break;
                    case VIRTUAL:
                        geoCacheTypeView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_virtual));
                        break;
                    case STEP_BY_STEP_VIRTUAL:
                        geoCacheTypeView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_virtual_step_by_step));
                        break;
                }

                ImageButton findBtn = (ImageButton) markerInfo.findViewById(R.id.findBtn);
                findBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location cacheLocation = new Location("");
                        cacheLocation.setLatitude(geoCache.la);
                        cacheLocation.setLongitude(geoCache.ln);
                        GoTo.compassActivity(getActivity(), geoCache);
                    }
                });

                final ImageButton saveBtn = (ImageButton) markerInfo.findViewById(R.id.saveBtn);
                final ImageButton deleteBtn = (ImageButton) markerInfo.findViewById(R.id.deleteBtn);

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Network.with(getActivity()).loadGeoCacheFullInfo(geoCache.id,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        Storage.with(getActivity()).saveGeoCacheFullInfo(Utils.merge(geoCache, jsonObject));
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError e) {
                                        //TODO implement error handling
                                        Log.e(getClass().getName(), e.getMessage(), e);
                                    }
                                });
                        deleteBtn.setVisibility(View.VISIBLE);
                        saveBtn.setVisibility(View.GONE);
                    }
                });

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Storage.with(getActivity()).deleteGeoCache(geoCache.id);
                        saveBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.GONE);
                    }
                });
                if (Storage.with(getActivity()).isGeoCacheInFavouriteList(geoCache.id)) {
                    deleteBtn.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.GONE);
                } else {
                    saveBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.GONE);
                }

                markerInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GoTo.geoCacheActivity(getActivity(), geoCache);
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
        saveMenuItem = menu.findItem(R.id.map_save);
        saveMenuItem.setOnMenuItemClickListener(new MyOnMenuItemClickListener());
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        boolean bulkSaveInProgress = settings.getBoolean(PREFS_KEY_BULK_SAVE_PROGRESS, false);
        if (bulkSaveInProgress) {
            saveMenuItem.setActionView(R.layout.actionbar_save_progress);
            saveMenuItem.expandActionView();
        }
    }

    private class GCDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(KEY_STATUS, -1);
            switch (resultCode) {
                case DOWNLOAD_STARTED:
                    saveMenuItem.setActionView(R.layout.actionbar_save_progress);
                    saveMenuItem.expandActionView();
                    break;
                case DOWNLOAD_FINISHED:
                    saveMenuItem.collapseActionView();
                    saveMenuItem.setActionView(null);
                    break;
                case DOWNLOAD_ERROR:
                    saveMenuItem.collapseActionView();
                    saveMenuItem.setActionView(null);
                    break;
                default:
                    Toast.makeText(getActivity(), "Unknown result code", Toast.LENGTH_SHORT).show();
                    Log.e(getClass().getName(), "Unknown result code");
                    break;
            }
        }
    }

    private class MyOnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            LatLngBounds bounds = googleMap.map.getProjection().getVisibleRegion().latLngBounds;
            double nLat = bounds.northeast.latitude;
            double nLon = bounds.northeast.longitude;
            double sLat = bounds.southwest.latitude;
            double sLon = bounds.southwest.longitude;

            Intent intent = new Intent(getActivity(), GCDownloadService.class);
            intent.putExtra("nLat", nLat);
            intent.putExtra("nLon", nLon);
            intent.putExtra("sLat", sLat);
            intent.putExtra("sLon", sLon);
            List<Integer> excludedIds = Storage.with(getActivity()).idsOfStoredGeoCachesInt();
            intent.putIntegerArrayListExtra("excludedIds", (ArrayList<Integer>) excludedIds);
            getActivity().startService(intent);

            return true;
        }
    }
}
