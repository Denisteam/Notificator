package ru.tomsksoft.notificator;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;

public class AlertNotification extends AppCompatActivity
{
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this,
            0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);

    String notificationText = "TEST";

    NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(String.valueOf(R.string.alert))
                    .setContentText(notificationText);

    Notification notification = builder.build();

    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

}
