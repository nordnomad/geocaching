package geocaching.ui;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import map.test.myapplication3.app.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActionBar actionBar;

    private static final int LOCATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        actionBar = getSupportActionBar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MapScreen()).commit();

            actionBar.setTitle(getResources().getString(R.string.drawer_menu_map));
        }
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView textView = (TextView) headerLayout.findViewById(R.id.userNameView);
        textView.setText(getUserName());
        if (!canAccessLocation()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
                // TODO implement request ui
                Toast.makeText(this, "Please grant Location permission for this application", Toast.LENGTH_SHORT).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
            }
        }
    }

    private boolean canAccessLocation() {
        return hasPermission(ACCESS_FINE_LOCATION) || hasPermission(ACCESS_COARSE_LOCATION);
    }

    private boolean hasPermission(String perm) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (canAccessLocation()) {
                    // TODO implement code if permission just granted
                    Toast.makeText(this, "permission just granted", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO implement code if permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String getUserName() {
        AccountAuthenticatorResponse accountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse.onRequestContinued();
        }
        return AccountManager.get(this).getAccountsByType("com.geocaching")[0].name;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_map) {
            fragmentTransaction.replace(R.id.content_frame, new MapScreen(), "MAP_SCREEN");
        } else if (id == R.id.nav_favourite) {
            fragmentTransaction.replace(R.id.content_frame, new FavoritesScreen(), "FAVOURITE_SCREEN");
        }
//        else if (id == R.id.nav_profile) {
//            fragmentTransaction.replace(R.id.content_frame, new FavoritesScreen(), "PROFILE_SCREEN");
//            GoTo.compassMapActivity(this);
//        }
        fragmentTransaction.commit();
        actionBar.setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
