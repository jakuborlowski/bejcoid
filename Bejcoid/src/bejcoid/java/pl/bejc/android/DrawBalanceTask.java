package pl.bejc.android;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pl.polidea.webimageview.WebImageView;

public class DrawBalanceTask extends AsyncTask<Pair<String, String>, Void, LinearLayout> {

    private MainActivity activity;

    public DrawBalanceTask(MainActivity paramActivity) {
        activity = paramActivity;
    }

    protected void onPostExecute(LinearLayout view) {
        ((LinearLayout) activity.findViewById(R.id.balances)).addView(view);
    }

    @Override
    protected LinearLayout doInBackground(Pair<String, String>... balance) {

        return getBalanceLinearLayout(balance[0].first, balance[0].second);
    }

    private LinearLayout getBalanceLinearLayout(final String name, final String balance) {

        LinearLayout linearLayout = new LinearLayout(activity.getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(4, 10, 10, 0);

        WebImageView imageView = new WebImageView(activity.getApplicationContext());
        imageView.setBackgroundColor(0xFFD0D0D0);

        try {
            imageView.setLayoutParams(new LinearLayout.LayoutParams(dp2px(50), dp2px(50)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURL("http://www.gravatar.com/avatar/" + md5(name) + "?s=" + dp2px(50) + "&default=monsterid");
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((TextView) activity.findViewById(R.id.affiliateTextArea)).setText(name);
                }
            });

        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
            return null;
        }

        linearLayout.addView(imageView);

        LinearLayout linearLayout2 = new LinearLayout(activity.getApplicationContext());
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        linearLayout2.setPadding(10, 0, 0, 0);

        TextView textView1 = new TextView(activity.getApplicationContext());
        textView1.setText(name);

        TextView textView2 = new TextView(activity.getApplicationContext());
        textView2.setGravity(Gravity.RIGHT);
        textView2.setText(balance);
        textView2.setTextColor(balance.charAt(0) == '-' ? Color.RED : Color.GREEN);
        textView2.setTextSize(25);

        linearLayout2.addView(textView1);
        linearLayout2.addView(textView2);

        linearLayout.addView(linearLayout2);

        return linearLayout;
    }

    public static String md5(final String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Integer dp2px(Integer dp) {
        Resources resources = activity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
