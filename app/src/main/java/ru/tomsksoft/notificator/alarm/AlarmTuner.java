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

//Если будильник ставится на текущий день недели, то проверяем время, если время врабатывания позже чем текущее,
// то ставим на след. неделю, иначе на сегодня, т.к. время срабатывания ещё не настало
        if (min == 0) {
            if((Calendar.HOUR_OF_DAY > hourOfDay) || (Calendar.HOUR_OF_DAY == hourOfDay && Calendar.MINUTE >= minute)) {
                calendar.add(Calendar.DATE, 7);
            }
        } else {
            calendar.add(Calendar.DATE, min);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Log.d(TAG, logDate.format(calendar.getTime()));

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    public static void disableAlarm(Context context, PendingIntent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
    }
}
