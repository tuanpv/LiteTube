package com.liteyoutube.youtubelite.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liteyoutube.youtubelite.R;


/**
 * Created by tuan.phanvan on 1/21/2017.
 */
public class AppRater {
        private final static String APP_TITLE = "Audio Video Rocket";// App Name
        private final static String APP_PNAME = "com.liteyoutube.youtubelite";// Package Name

        private final static int DAYS_UNTIL_PROMPT = 1;//Min number of days
        private final static int LAUNCHES_UNTIL_PROMPT = 8;//Min number of launches

        public static void app_launched(Context mContext) {
            SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
            if (prefs.getBoolean("dontshowagain", false)) { return ; }

            SharedPreferences.Editor editor = prefs.edit();

            // Increment launch counter
            long launch_count = prefs.getLong("launch_count", 0) + 1;
            editor.putLong("launch_count", launch_count);

            // Get date of first launch
            Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
            if (date_firstLaunch == 0) {
                date_firstLaunch = System.currentTimeMillis();
                editor.putLong("date_firstlaunch", date_firstLaunch);
            }

            // Wait at least n days before opening
            if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
                if (System.currentTimeMillis() >= date_firstLaunch +
                        (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                    showRateDialog(mContext, editor);
                }
            }

            editor.apply();
        }

        public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_rate);

            int width = (int)(mContext.getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(mContext.getResources().getDisplayMetrics().heightPixels*0.90);
            dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
//            dialog.setTitle("Rate " + APP_TITLE);
//
//            LinearLayout ll = new LinearLayout(mContext);
//            ll.setOrientation(LinearLayout.VERTICAL);

            TextView tv =(TextView) dialog.findViewById(R.id.tvDialogTitle);
            tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
//            tv.setWidth(240);
//            tv.setPadding(4, 0, 4, 10);
//            ll.addView(tv);

            Button b1 = (Button) dialog.findViewById(R.id.btnRateUs);
//            b1.setText("Rate " + APP_TITLE);
            b1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                }
            });
//            ll.addView(b1);

            Button b2 = (Button) dialog.findViewById(R.id.btnLater);
//            b2.setText("Remind me later");
            b2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
//            ll.addView(b2);

            Button b3 = (Button) dialog.findViewById(R.id.btnNo);
//            b3.setText("No, thanks");
            b3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                }
            });
//            ll.addView(b3);

//            dialog.setContentView(ll);
            dialog.show();
        }
}
