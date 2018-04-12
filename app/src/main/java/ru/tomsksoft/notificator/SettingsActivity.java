package ru.tomsksoft.notificator;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.Calendar;


public class SettingsActivity extends AppCompatActivity
{
    AlarmManager am;
    PendingIntent alarmIntent;
    boolean changed;
    TimePicker tp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        changed = false;
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        tp = (TimePicker) findViewById(R.id.timePicker);
        tp.setIs24HourView(true);

        final LinearLayout alarmSettingsLayout = findViewById(R.id.alarmSettingsLayout);
        final LinearLayout addAlarmLayout = findViewById(R.id.addAlarmLayout);

        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        final ToggleButton setAlarmTB = findViewById(R.id.toggleButtonSetAlarm);

        setAlarmTB.setChecked(sharedPref.getBoolean("set_Alarm", false));
        if (setAlarmTB.isChecked())
            setAlarmTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
        else
            setAlarmTB.setBackgroundColor(Color.RED);
        setAlarmTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("set_Alarm", isChecked);
                editor.apply();
                if (isChecked)
                {
                    setAlarmTB.setBackgroundColor(Color.argb(100, 0, 60, 80));
                    alarmSettingsLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    setAlarmTB.setBackgroundColor(Color.RED);
                    alarmSettingsLayout.setVisibility(View.INVISIBLE);
                    addAlarmLayout.setVisibility(View.INVISIBLE);
                    disableAlarm();
                }
            }
        });

        CheckBox c = findViewById(R.id.checkBox1);
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    addAlarmLayout.setVisibility(View.VISIBLE);
            }
        });
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                addAlarmLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void setPenetrationSignalOnClick(View view)
    {
        //TODO
    }

    public void onClickSetAlarm(View view)
    {
        boolean[] chek = new boolean[7];
        chek[0] = ((CheckBox)findViewById(R.id.checkBox1)).isChecked();
        chek[1] = ((CheckBox)findViewById(R.id.checkBox2)).isChecked();
        chek[2] = ((CheckBox)findViewById(R.id.checkBox3)).isChecked();
        chek[3] = ((CheckBox)findViewById(R.id.checkBox4)).isChecked();
        chek[4] = ((CheckBox)findViewById(R.id.checkBox5)).isChecked();
        chek[5] = ((CheckBox)findViewById(R.id.checkBox6)).isChecked();
        chek[6] = ((CheckBox)findViewById(R.id.checkBox7)).isChecked();
        System.out.println(chek[0] + " " + chek[1] + " " + chek[2] + " " + chek[3] + " " + chek[4] + " " + chek[5] + " " + chek[6]);
        enableAlarm(tp.getCurrentHour(), tp.getCurrentMinute(), chek);
        (findViewById(R.id.addAlarmLayout)).setVisibility(View.INVISIBLE);
        Toast.makeText(SettingsActivity.this, "Напоминание установлено", Toast.LENGTH_SHORT).show();
    }

    public void enableAlarm(int hourOfDay, int minute, boolean[] chek)
    {
        updateDays();

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            editor.putBoolean(dayOfWeek.name(), chek[dayOfWeek.ordinal()]);
        }
        editor.putInt("hour", hourOfDay);
        editor.putInt("minute", minute);
        editor.apply();


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        System.out.println(calendar.toString() + " -------------!!!!!!!!!!");

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 60 * 24, alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableAlarm()
    {
        updateDays();

        am.cancel(alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void updateDays()
    {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for (DayOfWeek dayOfWeek : DayOfWeek.values() ) {
            editor.putBoolean(dayOfWeek.name(), false);
        }
        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        if ((findViewById(R.id.addAlarmLayout)).getVisibility() == View.VISIBLE)
            Toast.makeText(SettingsActivity.this, "отменено", Toast.LENGTH_SHORT).show();

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
