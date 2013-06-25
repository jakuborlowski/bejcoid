package pl.bejc.bejcoid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private static final int CONTACT_PICKER_RESULT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AccessTokenTask().execute(this);

        AccountManager am = AccountManager.get(this); // "this" references the current Context
        Account[] accounts = am.getAccountsByType("com.google");
        TextView view = (TextView) findViewById(R.id.textView2);
        String name = accounts.length == 0 ? "brak" : accounts[0].name;
        view.setText(name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void doLaunchContactPicker(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    public void doAddDebt(View view) throws IOException, JSONException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        EditText affiliateEditText = (EditText)findViewById(R.id.affiliateTextArea);
        EditText amountEditText = (EditText)findViewById(R.id.amountTextArea);
        EditText descriptionEditText = (EditText)findViewById(R.id.descriptionTextArea);

        String access_token = getSharedPreferences("myPrefs", MODE_PRIVATE).getString("access_token", "null");
        //"ya29.AHES6ZQ0CL1PGV05V-Eqgkr26SiHk0v-0xyh6scFrf961Zuk2LsLzOVEVw";

        JSONObject json = new JSONObject();
        json.put("amount", amountEditText.getText().toString());
        json.put("affiliate", affiliateEditText.getText().toString());
        json.put("description", descriptionEditText.getText().toString());

        switch(view.getId()){
            case R.id.heOwesMeButton:
                json.put("heOwesMe", 1);
                break;
            case R.id.iOweHimButton:
                json.put("iOweHim", 1);
                break;
        }

        HttpPost httpPost = new HttpPost("http://staging.bejc.pl/api/google/addDebt/"+access_token);
        StringEntity entity = new StringEntity(json.toString(), HTTP.UTF_8);
        entity.setContentType("text/json");
        httpPost.setEntity(entity);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(httpPost);

        // odczytanie odpowiedzi
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String jsonResponse = reader.readLine();
        Toast.makeText(this, response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
        Log.i("bejc", "code: " + response.getStatusLine().toString());
        Log.i("bejc", "response: " + jsonResponse);

        affiliateEditText.setText("");
        amountEditText.setText("");
        descriptionEditText.setText("");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:

                    Uri result = data.getData();
                    String id = result.getLastPathSegment();
                    Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);
                    String email = "";
                    if (cursor.moveToFirst()) {
                        int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        email = cursor.getString(emailIdx);
                        Log.v("debug", "Got email: " + email);
                    }
                    EditText emailEntry = (EditText)findViewById(R.id.affiliateTextArea);
                    emailEntry.setText(email);
                    if (email.length() == 0) {
                        Toast.makeText(this, "Wybrany kontakt nie posiada adresu email.", Toast.LENGTH_LONG).show();
                    }

                    break;
                case AccessTokenTask.USER_RECOVERABLE_AUTH_EXCEPTION:
                    break;
            }
        }
    }

}
