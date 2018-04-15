package ru.tomsksoft.notificator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    Context appContext;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        appContext = context.getApplicationContext();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        Date date = new Date();
        String dayOfWeek = dateFormat.format(date).toUpperCase();
        boolean rightDay = sharedPref.getBoolean(dayOfWeek, false);



        if (rightDay) { sendNotification("Если вы опаздываете -  оповетите об этом"); }
    }

    private void sendNotification(String messageBody)
    {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = appContext.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(appContext, channelId)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notificator")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
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
