package ru.tomsksoft.notificator.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class AlarmTuner {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.ENGLISH);
    private static final String TAG = "ALARM_TUNER";

    public static void setAlarm(Context context, int hourOfDay, int minute, PendingIntent alarmIntent, Set<DayOfWeek> dayOfWeekSet) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        DayOfWeek currentDay = DayOfWeek.valueOf(DATE_FORMAT.format(new Date()).toUpperCase());
        int min = 8;
        for (DayOfWeek dayOfWeek: dayOfWeekSet)
        {
            int d = DayOfWeek.countOfDaysBetween(currentDay, dayOfWeek);
            if (d < min)
            {
                min = d;
            }
        }

        if (minute == -1 || hourOfDay == -1 || min == 8) {
            return;
        }

        SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar calendar = Calendar.getInstance();

        Log.d(TAG, logDate.format(calendar.getTime()));
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, min);
        Log.d(TAG, logDate.format(calendar.getTime()));
        Log.d(TAG, String.valueOf(min));

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    public static void disableAlarm(Context context, PendingIntent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
    }
}
