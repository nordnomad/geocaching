package geocaching.ui;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import map.test.myapplication3.app.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AccountAuthenticatorResponse accountAuthenticatorResponse;
    Bundle resultBundle;
    ActionBar actionBar = getSupportActionBar();
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

        TextView textView = (TextView) findViewById(R.id.userNameView);
        textView.setText(getUserName());

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        actionBar = getSupportActionBar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MapScreen()).commit();

            actionBar.setTitle(getResources().getString(R.string.drawer_menu_map));
        }
    }

    private String getUserName() {
        accountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse.onRequestContinued();
        }
        return AccountManager.get(this).getAccountsByType("com.geocaching")[0].name;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (drawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//        switch (item.getItemId()) {
//            case R.id.action_search:
//                menuLayout.isDrawerOpen(item.getActionView());
//                Toast.makeText(this, "Settings selected", LENGTH_LONG).show();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_screen_action_bar, menu);
        return true;
    }

    @Override
    public void finish() {
        if (accountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (resultBundle != null) {
                accountAuthenticatorResponse.onResult(resultBundle);
            } else {
                accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            accountAuthenticatorResponse = null;
        }
        super.finish();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_map) {
            fragment = new MapScreen();
        } else if (id == R.id.nav_favourite) {
            fragment = new FavoritesScreen();
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileScreen();
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
            actionBar.setTitle(item.getTitle());
        } else {
            Log.e(getLocalClassName(), "Error. Fragment is not created");
        }
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
