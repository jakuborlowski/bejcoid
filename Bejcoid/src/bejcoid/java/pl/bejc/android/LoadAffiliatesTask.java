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
import java.util.List;

public class LoadAffiliatesTask extends AsyncTask<Void, Void, Integer> {

    private static final int LOAD_OK = 1;
    private static final int LOAD_INVALID_TOKEN = 2;
    private static final int LOAD_ERROR = 3;

    private MainActivity activity;
    private String accessToken;
    List<String> affiliatesList;

    public LoadAffiliatesTask(MainActivity paramActivity) {
        activity = paramActivity;
        accessToken = activity.getAccessToken();
        Log.i("bejc_load_affiliates", "access_token: " + accessToken);
    }

    public void onPreExecute() {
        activity.markNetActivity();
    }

    public void onPostExecute(Integer result) {
        switch (result) {
            case LOAD_OK:
                activity.loadAffiliatesListCallback(affiliatesList);
                Log.i("bejc_load_affiliates", "ok");
                break;
            case LOAD_INVALID_TOKEN:
                activity.renewToken();
                Log.i("bejc_load_affiliates", "invoking token renewal");
                break;
            case LOAD_ERROR:
                Log.e("bejc_load_affiliates", "not ok");
                break;
        }

        activity.unmarkNetActivity();
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        try {
            affiliatesList = new ArrayList<String>();
            HttpResponse apiResponse = queryAffiliatesApi();

            Log.i("bejc_load_affiliates", "API response code: " + apiResponse.getStatusLine().getStatusCode());

            switch (apiResponse.getStatusLine().getStatusCode()) {
                case 200:
                    String content = convertStreamToString(apiResponse.getEntity().getContent());
                    Log.i("bejc_load_affiliates", "affiliates: " + content);
                    affiliatesList = getAffiliatesListFromJsonString(content);
                    return LOAD_OK;
                case 401:
                    return LOAD_INVALID_TOKEN;
            }
        } catch (Exception e) {
            Log.e("bejc_load_affiliates", e.getClass() + ": " + e.getMessage());
        }

        return LOAD_ERROR;
    }

    private HttpResponse queryAffiliatesApi() throws IOException {
        HttpGet httpGet = new HttpGet(activity.getResources().getString(R.string.api_url) + "/api/1/user/affiliates");
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

    private List<String> getAffiliatesListFromJsonString(String jsonString) throws JSONException {
        List<String> affiliatesList = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray(jsonString);
        int len = jsonArray.length();
        for (int i = 0; i < len; i++)
            affiliatesList.add(jsonArray.getJSONObject(i).getString("email"));
        return affiliatesList;
    }
}
