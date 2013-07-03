package pl.bejc.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DrawAffiliateTask extends AsyncTask<String, Void, ImageView> {

    private MainActivity activity;
    private LinearLayout placeholder;
    private ProgressBar progressBar;

    public DrawAffiliateTask(MainActivity paramActivity) {
        activity = paramActivity;
    }

    public void onPreExecute() {
        progressBar = new ProgressBar(activity.getApplicationContext(), null, android.R.attr.progressBarStyleSmall);
        placeholder = new LinearLayout(activity.getApplicationContext());
        placeholder.setLayoutParams(new LinearLayout.LayoutParams(dp2px(50), dp2px(50)));
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setPadding(0, 0, 10, 0);
        placeholder.addView(progressBar);
        ((LinearLayout) activity.findViewById(R.id.affiliates)).addView(placeholder);
    }

    @Override
    protected void onPostExecute(ImageView view) {
        // add thumbnail view to the horizontal scroll
        progressBar.setVisibility(View.GONE);
        placeholder.addView(view);
    }

    @Override
    protected ImageView doInBackground(String... strings) {

        return getAffiliateImageView(strings[0]);
    }

    private ImageView getAffiliateImageView(final String email) {
        ImageView imageView = new ImageView(activity.getApplicationContext());

        try {
            URL url = new URL("http://www.gravatar.com/avatar/" + md5(email) + "?s=" + dp2px(50) + "&default=monsterid");
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            imageView.setLayoutParams(new LinearLayout.LayoutParams(dp2px(50), dp2px(50)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmp);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((TextView) activity.findViewById(R.id.affiliateTextArea)).setText(email);
                }
            });

        } catch (Exception e) {
            Log.e("bejc", e.getClass() + ": " + e.getMessage());
            return null;
        }

        return imageView;
    }

    public static String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
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

    private Integer dp2px(Integer px) {
        return px * ((Double)Math.floor(activity.getResources().getDisplayMetrics().density)).intValue();
    }
}
