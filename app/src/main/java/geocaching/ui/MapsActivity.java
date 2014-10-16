package geocaching.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import geocaching.login.UserLoginTask;
import map.test.myapplication3.app.R;

import static android.text.TextUtils.isEmpty;

public class MapsActivity extends ActionBarActivity /*implements ConnectionCallbacks, OnConnectionFailedListener */ {

    AccountAuthenticatorResponse mAccountAuthenticatorResponse;
    Bundle mResultBundle;

    String[] menuItems;
    DrawerLayout menuLayout;
    ListView menuList;

    UserLoginTask authTask;
    EditText emailView;
    EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.geocaching");
        if (accounts.length == 0) {
            setContentView(R.layout.activity_login);
            emailView = (EditText) findViewById(R.id.email);
            passwordView = (EditText) findViewById(R.id.password);
            passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });
            Button loginButton = (Button) findViewById(R.id.login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        } else {
            setContentView(R.layout.activity_maps);
            //menuItems = getResources().getStringArray(R.array.planets_array);
            menuItems = new String[]{"CurrentUser.username", "Карта", "Избранное", "Настройки"};
            menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            menuList = (ListView) findViewById(R.id.left_drawer);
            // Set the adapter for the list view
            menuList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuItems));
            // Set the list's click listener
            class DrawerItemClickListener implements ListView.OnItemClickListener {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    Toast.makeText(MapsActivity.this, "DrawerItemClickListener.", Toast.LENGTH_SHORT).show();
                    selectItem(position);
                }
            }
            menuList.setOnItemClickListener(new DrawerItemClickListener());
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                    this, /* host Activity */
                    menuLayout, /* DrawerLayout object */
                    R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                    R.string.drawer_open, /* "open drawer" description */
                    R.string.drawer_close /* "close drawer" description */
            ) {
                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle("mTitle");
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle("mDrawerTitle");
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            //menuLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setIcon(R.drawable.ic_drawer);
            selectItem(0);
        }
    }

    private void selectItem(int position) {
        // Update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ProfileScreen();
                break;
            case 1:
                fragment = new MapScreen();
                break;
            case 2:
                fragment = new OptionsScreen();
                break;
            default:
                break;
        }
        // Insert the fragment by replacing any existing fragment
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
            // Highlight the selected item, update the title, and close the drawer
            menuList.setItemChecked(position, true);
            setTitle(menuItems[position]);
            menuLayout.closeDrawer(menuList);
        } else {
            // Error
            Log.e(getLocalClassName(), "Error. Fragment is not created");
        }
    }

    public void attemptLogin() {
        if (!isOnline()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = alertDialogBuilder
                    .setTitle("Network is required")
                    .setMessage("You are offline, please connect to internet to be able to login!")
                    .setCancelable(false)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
            alertDialog.show();
            return;
        }
        if (authTask != null) {
            return;
        }
        emailView.setError(null);
        passwordView.setError(null);

        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (isEmpty(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }
        if (isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            authTask = new UserLoginTask(this, email, password);
            authTask.execute((Void) null);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}
