package geocaching.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import geocaching.LoadCachesTask;
import geocaching.LoginActivity;
import map.test.myapplication3.app.R;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

public class MapsActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private LocationClient locationClient;

    private String[] planetTitles;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();


        planetTitles = getResources().getStringArray(R.array.planets_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, planetTitles));
        // Set the list's click listener
        class DrawerItemClickListener implements ListView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Toast.makeText(MapsActivity.this, "DrawerItemClickListener.", Toast.LENGTH_SHORT).show();
            }
        }
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, /* host Activity */
                drawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("mTitle");
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("mDrawerTitle");
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        //drawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setIcon(R.drawable.ic_drawer);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();

            }
        }
    }

    private void setUpMap() {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        Location l = locationClient.getLastLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 14.0f));
//        new LoadCachesTask(googleMap).execute();
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                new LoadCachesTask(googleMap).execute(googleMap.getProjection().getVisibleRegion().latLngBounds);
            }
        });
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed.", Toast.LENGTH_SHORT).show();
    }
}
