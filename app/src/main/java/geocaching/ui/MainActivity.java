package geocaching.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import map.test.myapplication3.app.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AccountAuthenticatorResponse accountAuthenticatorResponse;
    Bundle resultBundle;

    String[] menuItems;
    DrawerLayout menuLayout;
    ListView menuList;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse.onRequestContinued();
        }
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.geocaching");


        setContentView(R.layout.activity_maps);
        menuItems = new String[]{accounts[0].name, "Карта", "Избранное"/*, "Настройки"*/};
        menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        menuList = (ListView) findViewById(R.id.left_drawer);
//        menuList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, menuItems));
//        class DrawerItemClickListener implements ListView.OnItemClickListener {
//            @Override
//            public void onItemClick(AdapterView parent, View view, int position, long id) {
//                selectItem(position);
//            }
//        }
//        menuList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, menuLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Geocaching");
                supportInvalidateOptionsMenu();
            }
        };
        menuLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ProfileScreen();
                break;
            case 1:
                fragment = new MapScreen();
                break;
            case 2:
                fragment = new FavoritesScreen();
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
//            menuList.setItemChecked(position, true);
//            getSupportActionBar().setTitle(menuItems[position]);
//            menuLayout.closeDrawer(menuList);
        } else {
            Log.e(getLocalClassName(), "Error. Fragment is not created");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.map_screen_action_bar, menu);
//        MenuItem item = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) item.getActionView();
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
//        searchView.setSearchableInfo(info);

//        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                // Do something when collapsed
//                return true;  // Return true to collapse action view
//            }
//
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                // Do something when expanded
//                return true;  // Return true to expand action view
//            }
//        });

//        return true;
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.map_search).getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerToggle != null) drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

//    public void attemptLogin() {
//        if (!isOnline()) {
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//            AlertDialog alertDialog = alertDialogBuilder
//                    .setTitle("Network is required")
//                    .setMessage("You are offline, please connect to internet to be able to login!")
//                    .setCancelable(false)
//                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    })
//                    .create();
//            alertDialog.show();
//            return;
//        }
//        if (authTask != null) {
//            return;
//        }
//        emailView.setError(null);
//        passwordView.setError(null);
//
//        String email = emailView.getText().toString();
//        String password = passwordView.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//        if (isBlank(password)) {
//            passwordView.setError(getString(R.string.error_invalid_password));
//            focusView = passwordView;
//            cancel = true;
//        }
//        if (isBlank(email)) {
//            emailView.setError(getString(R.string.error_field_required));
//            focusView = emailView;
//            cancel = true;
//        }
//        if (cancel) {
//            focusView.requestFocus();
//        } else {
//            authTask = new UserLoginTask(this, email, password);
//            authTask.execute((Void) null);
//        }
//    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public final void setAccountAuthenticatorResult(Bundle result) {
        resultBundle = result;
    }

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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camara) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
