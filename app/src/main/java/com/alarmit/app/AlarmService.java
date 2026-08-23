package com.alarmit.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

public class AlarmService extends Service {

    private static final String CHANNEL_ID = "alarmit_alarm";
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("AlarmIt")
                        .setContentText("Alarm is ringing")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setOngoing(true)
                        .build();

        startForeground(1001, notification);

        startAlarmSound();
    }

    private void createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "AlarmIt Alarms",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("AlarmIt alarm notifications");
            channel.setLockscreenVisibility(
                    Notification.VISIBILITY_PUBLIC
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private void startAlarmSound() {

        try {

            player = MediaPlayer.create(
                    this,
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            );

            if (player != null) {

                player.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(
                                        AudioAttributes.USAGE_ALARM
                                )
                                .setContentType(
                                        AudioAttributes.CONTENT_TYPE_MUSIC
                                )
                                .build()
                );

                player.setLooping(true);
                player.start();
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (player != null) {

            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (Exception ignored) {
            }

            player.release();
            player = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
