package com.alarmit.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM =
            "com.alarmit.app.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_ALARM.equals(intent.getAction())) {

            Intent serviceIntent =
                    new Intent(context, AlarmService.class);

            serviceIntent.putExtra(
                    "label",
                    intent.getStringExtra("label")
            );

            serviceIntent.putExtra(
                    "time",
                    intent.getStringExtra("time")
            );

            context.startForegroundService(serviceIntent);
        }
    }
}
