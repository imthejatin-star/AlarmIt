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

public class MainActivity extends Activity {

    private WebView webView;
    private int pendingAlarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWebView();

        handleAlarmIntent(getIntent());
    }

    private void setupWebView() {

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

        webView.setBackgroundColor(0xFF030711);

        webView.loadUrl(
                "file:///android_asset/index.html"
        );

        setContentView(webView);
    }

    private void handleAlarmIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        if (AlarmReceiver.ACTION_ALARM.equals(intent.getAction())) {

            pendingAlarmId =
                    intent.getIntExtra(
                            "alarmId",
                            -1
                    );

            webView.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            triggerPendingAlarm();
                        }
                    },
                    1200
            );
        }
    }

    private void triggerPendingAlarm() {

        if (pendingAlarmId == -1) {
            return;
        }

        final int id = pendingAlarmId;

        pendingAlarmId = -1;

        webView.evaluateJavascript(
                "window.nativeTriggerAlarm(" +
                        id +
                        ");",
                null
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        setIntent(intent);

        handleAlarmIntent(intent);
    }

    public class AlarmBridge {

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
                    (AlarmManager)
                            context.getSystemService(
                                    Context.ALARM_SERVICE
                            );

            if (alarmManager == null) {
                return;
            }

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

                        context.startActivity(
                                settingsIntent
                        );

                    } catch (Exception ignored) {
                    }

                    return;
                }
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
                    "alarmId",
                    id
            );

            alarmIntent.putExtra(
                    "label",
                    label
            );

            alarmIntent.putExtra(
                    "hour",
                    hour
            );

            alarmIntent.putExtra(
                    "minute",
                    minute
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            id,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            java.util.Calendar calendar =
                    java.util.Calendar.getInstance();

            calendar.set(
                    java.util.Calendar.HOUR_OF_DAY,
                    hour
            );

            calendar.set(
                    java.util.Calendar.MINUTE,
                    minute
            );

            calendar.set(
                    java.util.Calendar.SECOND,
                    0
            );

            calendar.set(
                    java.util.Calendar.MILLISECOND,
                    0
            );

            /*
             * The HTML alarm system already determines
             * repeating days. The native layer schedules
             * the next occurrence.
             */
            if (
                    calendar.getTimeInMillis()
                            <= System.currentTimeMillis()
            ) {

                calendar.add(
                        java.util.Calendar.DAY_OF_YEAR,
                        1
                );
            }

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.M) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

            } else {

                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }

        @JavascriptInterface
        public void cancelAlarm(int id) {

            AlarmManager alarmManager =
                    (AlarmManager)
                            context.getSystemService(
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

            alarmManager.cancel(
                    pendingIntent
            );

            pendingIntent.cancel();
        }
    }

    @Override
    public void onBackPressed() {

        if (
                webView != null &&
                webView.canGoBack()
        ) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}
