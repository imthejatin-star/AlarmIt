package com.alarmit.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView screen = new TextView(this);

        screen.setText("AlarmIt\n\nYour alarm app is starting...");
        screen.setTextColor(Color.WHITE);
        screen.setTextSize(22);
        screen.setGravity(Gravity.CENTER);
        screen.setBackgroundColor(Color.rgb(3, 7, 17));

        setContentView(screen);
    }
}
