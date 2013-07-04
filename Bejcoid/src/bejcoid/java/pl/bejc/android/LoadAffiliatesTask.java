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

public class LoadAffiliatesTask extends AsyncTask<Void, Void, List<String>> {

    private MainActivity activity;
    private String accessToken;

    public LoadAffiliatesTask(MainActivity paramActivity) {
        activity = paramActivity;
        accessToken = activity.getAccessToken();
    }

    public void onPreExecute() {
        activity.markNetActivity();
    }

    public void onPostExecute(List<String> affiliates) {
        activity.loadAffiliatesListCallback(affiliates);
        activity.unmarkNetActivity();
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        List<String> affiliatesList = new ArrayList<String>();

        try {
            HttpResponse apiResponse = queryAffiliatesApi();
            switch (apiResponse.getStatusLine().getStatusCode()) {
                case 200:
                    String content = convertStreamToString(apiResponse.getEntity().getContent());
                    Log.i("bejc_draw_affiliates", "affiliates: " + content);
                    affiliatesList = getAffiliatesListFromJsonString(content);
                    break;
                default:
                    Log.e("bejc_draw_affiliates", "API response code: " + apiResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
        }

        return affiliatesList;
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
