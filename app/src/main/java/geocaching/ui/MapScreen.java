package geocaching.ui;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.SearchView;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import geocaching.GeoCache;
import geocaching.MapWrapper;
import geocaching.db.GeoCacheProvider;
import geocaching.tasks.LoadCachesTask;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import static geocaching.Utils.geoCacheToContentValues;

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
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            googleMap = new MapWrapper(mapFragment.getMap());
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
                        ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                        resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheToContentValues(geoCache));
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
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Set<GeoCache> geoCaches = googleMap.markerGeoCaches.keySet();
                for (GeoCache geoCache : geoCaches) {
                    ContentResolver resolver = MapScreen.this.getActivity().getContentResolver();
                    resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheToContentValues(geoCache));
                }
                return true;
            }
        });
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.map_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    geocoder.getFromLocationName(s, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new GetAddressTask(getActivity()).execute(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                loadHistory(query);
                return false;

            }

            List<String> items = Arrays.asList("test1", "test2", "test3", "test4");

            private void loadHistory(String query) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    // Load data from list to cursor
                    String[] columns = new String[]{"_id", "text"};
                    Object[] temp = new Object[]{0, "default"};
                    MatrixCursor cursor = new MatrixCursor(columns);
                    for (int i = 0; i < items.size(); i++) {
                        temp[0] = i;
                        temp[1] = items.get(i);
                        cursor.addRow(temp);
                    }
                    // Alternatively load data from database
                    //Cursor cursor = db.rawQuery("SELECT * FROM table_name", null);
                    SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                    final SearchView search = (SearchView) menu.findItem(R.id.map_search).getActionView();
                    search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
                    search.setSuggestionsAdapter(new ExampleAdapter(getActivity(), cursor, items));
                }
            }

            class ExampleAdapter extends CursorAdapter {
                private List<String> items;
                private TextView text;

                public ExampleAdapter(Context context, Cursor cursor, List items) {
                    super(context, cursor, false);
                    this.items = items;
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    // Show list item data from cursor
                    text.setText(items.get(cursor.getPosition()));
                    // Alternatively show data direct from database
                    //text.setText(cursor.getString(cursor.getColumnIndex("column_name")));

                }

                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.history_item, parent, false);
                    text = (TextView) view.findViewById(R.id.text);
                    return view;
                }

            }
        });
    }

    class GetAddressTask extends AsyncTask<String, Void, LatLng> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(params[0], 1);
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null)
                googleMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            else
                Toast.makeText(mContext, "Неудалось найти указанный адрес", Toast.LENGTH_SHORT).show();
        }
    }

}
