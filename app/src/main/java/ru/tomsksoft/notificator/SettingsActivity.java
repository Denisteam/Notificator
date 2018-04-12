package ru.tomsksoft.notificator;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Set;


public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
        tp.setIs24HourView(true);

        final LinearLayout alarmSettingsLayout = (LinearLayout)findViewById(R.id.alarmSettingsLayout);

        final ToggleButton setAlarmTB = (ToggleButton)findViewById(R.id.toggleButtonSetAlarm);
        setAlarmTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    alarmSettingsLayout.setVisibility(View.VISIBLE);
                else
                    alarmSettingsLayout.setVisibility(View.INVISIBLE);
            }
        });

    }

    public void setPenetrationSignalOnClick(View view)
    {
        //TODO
    }

    public void onClickSetAlarm(View view)
    {
        //TODO
    }

    public void onClickSetAlarmTime(View view)
    {
        //TODO
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        //super.onBackPressed();
    }

    @Override
    public void onDestroy()
    {
        moveTaskToBack(true);

        super.onDestroy();

        System.runFinalizersOnExit(true);
        System.exit(0);
    }


}
