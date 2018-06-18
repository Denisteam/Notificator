package ru.tomsksoft.notificator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class AlarmBootReceiver extends BroadcastReceiver {
    AlarmManager am;
    PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent target = new Intent(appContext, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(appContext, 0, target, PendingIntent.FLAG_CANCEL_CURRENT);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        int hourOfDay = sharedPref.getInt("hour", -1);
        int minute = sharedPref.getInt("minute", -1);

        if (minute == -1 || hourOfDay == -1) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - 60 * 1000,
                1000 * 60 * 60 * 24, alarmIntent);
    }
}
