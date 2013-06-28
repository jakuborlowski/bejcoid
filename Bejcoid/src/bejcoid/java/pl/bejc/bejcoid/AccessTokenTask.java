package pl.bejc.bejcoid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class AccessTokenTask extends AsyncTask<Void, Void, String> {

    public static final int USER_RECOVERABLE_AUTH_EXCEPTION = 7001;

    private MainActivity activity;

    public AccessTokenTask(MainActivity paramActivity) {
        activity = paramActivity;
    }

    @Override
    protected String doInBackground(Void... voids) {

        AccountManager accountManager = AccountManager.get(activity); // "this" references the current Context
        Account[] accounts = accountManager.getAccountsByType("com.google");
        String access_token = "null";

        try {
            access_token = GoogleAuthUtil.getToken(activity, accounts[0].name, "oauth2:https://www.googleapis.com/auth/userinfo.email");
        } catch (UserRecoverableAuthException e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
            activity.startActivityForResult(e.getIntent(), USER_RECOVERABLE_AUTH_EXCEPTION);
        } catch (IOException e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
        }

        return access_token;
    }

    protected void onPreExecute() {
        activity.lockInterface();
    }

    @Override
    protected void onPostExecute(String accessToken) {

        SharedPreferences preferences = activity.getSharedPreferences("myPrefs", activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString("access_token", accessToken);
        editor.commit();

        Log.i("bejc", "access_token: " + accessToken);

        activity.unlockInterface();
        activity.accessTokenReadyCallback(accessToken);
    }
}
