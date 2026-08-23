package com.alarmit.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;package com.alarmit.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
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

        webView.loadUrl("file:///android_asset/index.html");

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

            if (alarmManager == null) return;

            if (android.os.Build.VERSION.SDK_INT >= 31 &&
                    !alarmManager.canScheduleExactAlarms()) {

                try {
                    Intent intent = new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse(
                                    "package:" +
                                    context.getPackageName()
                            )
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    context.startActivity(intent);

                } catch (Exception ignored) {
                }

                return;
            }

            Calendar calendar = Calendar.getInstance();

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    hour
            );

            calendar.set(
                    Calendar.MINUTE,
                    minute
            );

            calendar.set(
                    Calendar.SECOND,
                    0
            );

            calendar.set(
                    Calendar.MILLISECOND,
                    0
            );

            if (calendar.getTimeInMillis()
                    <= System.currentTimeMillis()) {

                calendar.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );
            }

            Intent intent =
                    new Intent(
                            context,
                            AlarmReceiver.class
                    );

            intent.setAction(
                    AlarmReceiver.ACTION_ALARM
            );

            intent.putExtra(
                    "label",
                    label
            );

            intent.putExtra(
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
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
                    );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        @JavascriptInterface
        public void cancelAlarm(int id) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager == null) return;

            Intent intent =
                    new Intent(
                            context,
                            AlarmReceiver.class
                    );

            intent.setAction(
                    AlarmReceiver.ACTION_ALARM
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            id,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
                    );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
import android.webkit.WebViewClient;

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

        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
