package com.alarmit.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM =
            "com.alarmit.app.ACTION_ALARM";

    private static final String CHANNEL_ID =
            "alarmit_alarm_channel";

    private static final int NOTIFICATION_ID =
            5001;

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (
                intent == null ||
                !ACTION_ALARM.equals(
                        intent.getAction()
                )
        ) {
            return;
        }

        int alarmId =
                intent.getIntExtra(
                        "alarmId",
                        -1
                );

        String label =
                intent.getStringExtra(
                        "label"
                );

        createNotificationChannel(context);

        Intent launchIntent =
                new Intent(
                        context,
                        MainActivity.class
                );

        launchIntent.setAction(
                ACTION_ALARM
        );

        launchIntent.putExtra(
                "alarmId",
                alarmId
        );

        launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent fullScreenIntent =
                PendingIntent.getActivity(
                        context,
                        alarmId,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Notification.Builder builder;

        if (
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.O
        ) {

            builder =
                    new Notification.Builder(
                            context,
                            CHANNEL_ID
                    );

        } else {

            builder =
                    new Notification.Builder(
                            context
                    );
        }

        builder
                .setSmallIcon(
                        android.R.drawable.ic_lock_idle_alarm
                )
                .setContentTitle(
                        label != null
                                ? label
                                : "AlarmIt"
                )
                .setContentText(
                        "Alarm is ringing"
                )
                .setCategory(
                        Notification.CATEGORY_ALARM
                )
                .setPriority(
                        Notification.PRIORITY_MAX
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(
                        fullScreenIntent,
                        true
                );

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager != null) {

            manager.notify(
                    NOTIFICATION_ID,
                    builder.build()
            );
        }

        /*
         * Launch AlarmIt immediately.
         * The WebView will call nativeTriggerAlarm()
         * after its HTML has loaded.
         */
        try {

            context.startActivity(
                    launchIntent
            );

        } catch (Exception ignored) {
        }
    }

    private void createNotificationChannel(
            Context context
    ) {

        if (
                Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.O
        ) {
            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "AlarmIt Alarms",
                        NotificationManager.IMPORTANCE_HIGH
                );

        channel.setDescription(
                "AlarmIt alarm notifications"
        );

        channel.setLockscreenVisibility(
                Notification.VISIBILITY_PUBLIC
        );

        manager.createNotificationChannel(
                channel
        );
    }
}
