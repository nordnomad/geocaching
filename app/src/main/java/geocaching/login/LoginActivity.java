package geocaching.login;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AccountAuthenticatorActivity {

    UserLoginTask authTask;
    EditText emailView;
    EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            loginButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        } else {
            setContentView(R.layout.logged);
            EditText editText = (EditText) findViewById(R.id.username);
            editText.setText(accounts[0].name);
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}



