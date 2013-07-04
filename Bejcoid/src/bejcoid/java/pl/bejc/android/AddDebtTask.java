package pl.bejc.android;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AddDebtTask extends AsyncTask<JSONObject, Void, HttpResponse> {

    private MainActivity activity;

    public AddDebtTask(MainActivity paramMainActivity) {
        activity = paramMainActivity;
    }

    @Override
    protected HttpResponse doInBackground(JSONObject... jsons) {

        try {
            JSONObject json = jsons[0];
            HttpPost httpPost = null;
            httpPost = new HttpPost(activity.getResources().getString(R.string.api_url) + "/api/1/debt/add");
            StringEntity entity = null;
            entity = new StringEntity(jsons[0].toString(), HTTP.UTF_8);
            entity.setContentType("text/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Bejc-Authentication-Provider", "google");
            httpPost.setHeader("Bejc-Authentication-Token", json.get("access_token").toString());
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(httpPost);
            return response;
        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
            return null;
        }
    }

    protected void onPreExecute() {
        activity.lockInterface();
        activity.markNetActivity();
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String jsonResponse = reader.readLine();
            Toast.makeText(activity, "Dodano zobowiązanie", Toast.LENGTH_LONG).show();
            Log.i("bejc", "code: " + response.getStatusLine().toString());
            Log.i("bejc", "response: " + jsonResponse);

            switch (response.getStatusLine().getStatusCode()) {
                case 401:
                    Toast.makeText(activity, "brak użytkownika lub nieprawidłowy token", Toast.LENGTH_LONG).show();
                    //activity.renewToken();
                    break;
                case 400:
                    Toast.makeText(activity, "formularz zawiera błędy", Toast.LENGTH_LONG).show();
                    activity.unlockInterface();
                case 200:
                    activity.resetTextAreas();
                    activity.unlockInterface();
                    break;
                default:
                    activity.unlockInterface();
            }

        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
        }

        activity.unmarkNetActivity();
    }
}
