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
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.tomsksoft.notificator.LoginActivity;
import ru.tomsksoft.notificator.MainActivity;
import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserDataStorage;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "ALARM";

    Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = context.getApplicationContext();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        Date date = new Date();
        String dayOfWeek = dateFormat.format(date).toUpperCase();
        UserDataStorage dataStorage = new UserDataStorage(context);
        boolean rightDay = dataStorage.isDayOfWeekSet(DayOfWeek.valueOf(dayOfWeek));

        if (rightDay) {
            sendNotification("Если вы опаздываете - оповеcтите об этом");
        }
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
