package ru.tomsksoft.notificator.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import ru.tomsksoft.notificator.UserDataStorage;

public class AlarmBootReceiver extends BroadcastReceiver {
    private static final String TAG = "ALARM_BOOT_RECEIVER";

    AlarmManager am;
    PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        Intent target = new Intent(appContext, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(appContext, 0, target, PendingIntent.FLAG_CANCEL_CURRENT);

        UserDataStorage dataStorage = new UserDataStorage(context);
        int[] tmp = dataStorage.getTime();
        int hourOfDay = tmp[0];
        int minute = tmp[1];

        Set<DayOfWeek> dayOfWeekSet = dataStorage.loadDaysOfWeekSet();

    }
}
