package pl.bejc.bejcoid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class AccessTokenTask extends AsyncTask<Activity, Void, Void> {

    public static final int USER_RECOVERABLE_AUTH_EXCEPTION = 7001;

    @Override
    protected Void doInBackground(Activity... contexts) {
        AccountManager accountManager = AccountManager.get(contexts[0]); // "this" references the current Context
        Account[] accounts = accountManager.getAccountsByType("com.google");

        String access_token = "null";

        try {
            access_token = GoogleAuthUtil.getToken(contexts[0], accounts[0].name, "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
        } catch (UserRecoverableAuthException e) {
            Log.e("bejc", e.getClass()+": "+e.getMessage());
            contexts[0].startActivityForResult(e.getIntent(), USER_RECOVERABLE_AUTH_EXCEPTION);
        }catch (IOException e) {
            Log.e("bejc", e.getClass()+": "+e.getMessage());
        } catch (Exception e) {
            Log.e("bejc", e.getClass()+": "+e.getMessage());
        }

        SharedPreferences myPrefs = contexts[0].getSharedPreferences("myPrefs", contexts[0].MODE_PRIVATE);
        SharedPreferences.Editor e = myPrefs.edit();
        e.putString("access_token", access_token);
        e.commit();

        //Toast.makeText(contexts[0], access_token, Toast.LENGTH_LONG).show();

        Log.i("bejc", "access_token: " + access_token);
        return null;
    }
}
