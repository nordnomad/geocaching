package geocaching.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import geocaching.GoTo;
import geocaching.login.UserLoginTask;
import map.test.myapplication3.app.R;

import static geocaching.Utils.isBlank;

public class LoginActivity extends Activity {
    AccountAuthenticatorResponse accountAuthenticatorResponse;
    Bundle resultBundle;
    UserLoginTask authTask;
    EditText emailView;
    EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse.onRequestContinued();
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
            GoTo.mainActivity(this);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    void attemptLogin() {
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
        if (isBlank(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }
        if (isBlank(email)) {
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
}
