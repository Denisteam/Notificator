package ru.tomsksoft.notificator.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import ru.tomsksoft.notificator.UserDataStorage;

public class AlarmBootReceiver extends BroadcastReceiver {
    private static final String TAG = "ALARM_BOOT_RECEIVER";

    AlarmManager am;
    PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent target = new Intent(appContext, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(appContext, 0, target, PendingIntent.FLAG_CANCEL_CURRENT);

        UserDataStorage dataStorage = new UserDataStorage(context);
        int hourOfDay = dataStorage.getHour();
        int minute = dataStorage.getMinute();

        if (minute == -1 || hourOfDay == -1) {
            Log.e(TAG, "time is empty");
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
