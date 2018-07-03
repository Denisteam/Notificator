package ru.tomsksoft.notificator.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import ru.tomsksoft.notificator.UI.LoginActivity;
import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserDataStorage;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "ALARM";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.ENGLISH);

    Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = context.getApplicationContext();
        Intent target = new Intent(appContext, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, 0, target, PendingIntent.FLAG_CANCEL_CURRENT);

        UserDataStorage dataStorage = new UserDataStorage(context);
        int[] tmp = dataStorage.getTime();
        int hourOfDay = tmp[0];
        int minute = tmp[1];

        Set<DayOfWeek> dayOfWeekSet = dataStorage.loadDaysOfWeekSet();
        //dayOfWeekSet.remove(DayOfWeek.valueOf(DATE_FORMAT.format(new Date()).toUpperCase()));
        AlarmTuner.setAlarm(appContext, hourOfDay, minute, alarmIntent, dayOfWeekSet);

        sendNotification("Если вы опаздываете - оповеcтите об этом");
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(appContext, LoginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = appContext.getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(appContext, channelId)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notificator")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
