package pl.bejc.android;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoadBalancesTask extends AsyncTask<Void, Void, Integer> {

    private static final int LOAD_OK = 1;
    private static final int LOAD_INVALID_TOKEN = 2;
    private static final int LOAD_ERROR = 3;

    private MainActivity activity;
    private String accessToken;
    Map<String, String> balancesList;

    public LoadBalancesTask(MainActivity paramActivity) {
        activity = paramActivity;
        accessToken = activity.getAccessToken();
        Log.i("bejc_load_balances", "access_token: " + accessToken);
    }

    public void onPreExecute() {
        activity.markNetActivity();
    }

    public void onPostExecute(Integer result) {
        switch (result) {
            case LOAD_OK:
                activity.loadBalancesListCallback(balancesList);
                Log.i("bejc_load_balances", "ok");
                break;
            case LOAD_INVALID_TOKEN:
                activity.renewToken();
                Log.i("bejc_load_balances", "invoking token renewal");
                break;
            case LOAD_ERROR:
                Log.e("bejc_load_balances", "not ok");
                break;
        }

        activity.unmarkNetActivity();
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        try {
            balancesList = new HashMap<String, String>();
            HttpResponse apiResponse = queryBalancesApi();

            Log.i("bejc_load_balances", "API response code: " + apiResponse.getStatusLine().getStatusCode());

            switch (apiResponse.getStatusLine().getStatusCode()) {
                case 200:
                    String content = convertStreamToString(apiResponse.getEntity().getContent());
                    Log.i("bejc_load_balances", "balances: " + content);
                    balancesList = getBalancesListFromJsonString(content);
                    return LOAD_OK;
                case 401:
                    return LOAD_INVALID_TOKEN;
            }
        } catch (Exception e) {
            Log.e("bejc_load_balances", e.getClass() + ": " + e.getMessage());
        }

        return LOAD_ERROR;
    }

    private HttpResponse queryBalancesApi() throws IOException {
        HttpGet httpGet = new HttpGet(activity.getResources().getString(R.string.api_url) + "/api/1/debt/summary");
        httpGet.setHeader("Bejc-Authentication-Provider", "google");
        httpGet.setHeader("Bejc-Authentication-Token", accessToken);
        HttpClient client = new DefaultHttpClient();
        return client.execute(httpGet);
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) sb.append(line + "\n");
        is.close();
        return sb.toString();
    }

    private Map<String, String> getBalancesListFromJsonString(String jsonString) throws JSONException {
        Map<String, String> balancesList = new HashMap<String, String>();
        JSONArray jsonArray = new JSONArray(jsonString);
        int len = jsonArray.length();
        for (int i = 0; i < len; i++)
            balancesList.put(jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("balance"));
        return balancesList;
    }
}
