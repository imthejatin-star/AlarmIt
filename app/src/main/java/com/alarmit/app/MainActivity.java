package com.alarmit.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(
                new AlarmBridge(this),
                "AndroidAlarm"
        );

        webView.loadUrl(
                "file:///android_asset/index.html"
        );

        setContentView(webView);
    }

    public static class AlarmBridge {

        private final Context context;

        AlarmBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void scheduleAlarm(
                int id,
                int hour,
                int minute,
                String label
        ) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager == null) {
                return;
            }

            /*
             * Android 12+ requires the user to allow
             * exact alarms.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (!alarmManager.canScheduleExactAlarms()) {

                    try {

                        Intent settingsIntent =
                                new Intent(
                                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        Uri.parse(
                                                "package:" +
                                                context.getPackageName()
                                        )
                                );

                        settingsIntent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                        );

                        context.startActivity(settingsIntent);

                    } catch (Exception ignored) {
                    }

                    return;
                }
            }

            Calendar alarmTime =
                    Calendar.getInstance();

            alarmTime.set(
                    Calendar.HOUR_OF_DAY,
                    hour
            );

            alarmTime.set(
                    Calendar.MINUTE,
                    minute
            );

            alarmTime.set(
                    Calendar.SECOND,
                    0
            );

            alarmTime.set(
                    Calendar.MILLISECOND,
                    0
            );

            /*
             * If today's time has already passed,
             * schedule it for tomorrow.
             */
            if (alarmTime.getTimeInMillis()
                    <= System.currentTimeMillis()) {

                alarmTime.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );
            }

            Intent alarmIntent =
                    new Intent(
                            context,
                            AlarmReceiver.class
                    );

            alarmIntent.setAction(
                    AlarmReceiver.ACTION_ALARM
            );

            alarmIntent.putExtra(
                    "label",
                    label
            );

            alarmIntent.putExtra(
                    "time",
                    String.format(
                            "%02d:%02d",
                            hour,
                            minute
                    )
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            id,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
                    );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(),
                        pendingIntent
                );

            } else {

                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(),
                        pendingIntent
                );
            }
        }

        @JavascriptInterface
        public void cancelAlarm(int id) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager == null) {
                return;
            }

            Intent alarmIntent =
                    new Intent(
                            context,
                            AlarmReceiver.class
                    );

            alarmIntent.setAction(
                    AlarmReceiver.ACTION_ALARM
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            id,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
                    );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null &&
                webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}
