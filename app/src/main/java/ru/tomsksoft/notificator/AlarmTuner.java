package ru.tomsksoft.notificator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.time.DayOfWeek;
import java.util.Calendar;

public class AlarmTuner extends Activity {

    AlarmManager am;
    PendingIntent alarmIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //TODO: implement user view
        super.onCreate(savedInstanceState);

        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    public void enableAlarm(int hourOfDay, int minute, DayOfWeek ... daysOfWeek) {

        if (daysOfWeek.length > 0) {
            SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            for (DayOfWeek dayOfWeek : daysOfWeek) {
                editor.putBoolean(dayOfWeek.name(), true);
            }
            editor.putInt("hour", hourOfDay);
            editor.putInt("minute", minute);
            editor.apply();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 60 * 24, alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableAlarm() {
        am.cancel(alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
