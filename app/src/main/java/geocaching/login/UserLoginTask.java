package geocaching.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geocaching.ui.MainActivity;
import map.test.myapplication3.app.R;

import static geocaching.Const.ACCOUNT_TYPE;
import static geocaching.Const.EMAIL_PARAM;
import static geocaching.Const.LOGIN_PARAM;
import static geocaching.Const.LOGIN_PARAM_VAL;
import static geocaching.Const.LOGIN_URL;
import static geocaching.Const.LONGTERM_PARAM;
import static geocaching.Const.LONGTERM_PARAM_VAL;
import static geocaching.Const.PASSWORD_PARAM;
import static geocaching.Const.USER_AGENT;
import static geocaching.Const.caching_auth_lt;
import static geocaching.Const.caching_auth_st;
import static geocaching.Const.geocaching;

public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    String email;
    String password;
    MainActivity context;

    public UserLoginTask(MainActivity context, String email, String password) {
        this.email = email;
        this.password = password;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        showProgress(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HttpClient client = AndroidHttpClient.newInstance(USER_AGENT);
        HttpPost post = new HttpPost(LOGIN_URL);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(EMAIL_PARAM, email));
        urlParameters.add(new BasicNameValuePair(PASSWORD_PARAM, password));
        urlParameters.add(new BasicNameValuePair(LONGTERM_PARAM, LONGTERM_PARAM_VAL));
        urlParameters.add(new BasicNameValuePair(LOGIN_PARAM, LOGIN_PARAM_VAL));

        HttpResponse response;
        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            response = client.execute(post);
        } catch (IOException e) {
            Log.d(getClass().getCanonicalName(), e.getMessage());
            return false;
        }
        Map<String, String> tokens = new HashMap<>();
        if (response.getStatusLine().getStatusCode() == 302) {
            Header[] geoCookie = response.getHeaders("Set-Cookie");
            for (Header header : geoCookie) {
                String[] nameValuePair = header.getValue().split("=");
                tokens.put(nameValuePair[0], nameValuePair[1].split(";")[0]);
            }
            if (!tokens.containsKey(caching_auth_st)) {
                return false;
            }
        } else {
            return false;
        }
        String accountType = context.getIntent().getStringExtra("auth.token");
        if (accountType == null) {
            accountType = ACCOUNT_TYPE;
        }
        Bundle tokensBundle = new Bundle();
        tokensBundle.putString(caching_auth_lt, tokens.get(caching_auth_lt));
        tokensBundle.putString(caching_auth_st, tokens.get(caching_auth_st));
        tokensBundle.putString(geocaching, tokens.get(geocaching));
        AccountManager.get(context).addAccountExplicitly(new Account(email, accountType), password, tokensBundle);

        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);

        intent.putExtra(caching_auth_lt, tokens.get(caching_auth_lt));
        intent.putExtra(caching_auth_st, tokens.get(caching_auth_st));
        intent.putExtra(geocaching, tokens.get(geocaching));

        CurrentUser.initCurrentUser(email, tokens);

        context.setAccountAuthenticatorResult(intent.getExtras());
        context.setResult(Activity.RESULT_OK, intent);
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        showProgress(false);
        if (success) {
            context.recreate();
        } else {
            EditText passwordView = (EditText) context.findViewById(R.id.password);
            passwordView.setError(context.getString(R.string.error_incorrect_password));
            passwordView.requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        showProgress(false);
    }

    public void showProgress(final boolean show) {
        final View loginFormView = context.findViewById(R.id.login_form);
        final View progressView = context.findViewById(R.id.login_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
