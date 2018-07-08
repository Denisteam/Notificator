package ru.tomsksoft.notificator;

import android.app.AlarmManager;
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
import java.util.Locale;
import java.util.Set;

import ru.tomsksoft.notificator.UI.LoginActivity;

import static android.content.Context.ALARM_SERVICE;

public class NotificationRepeater extends BroadcastReceiver {
        private static final String TAG = "NOTIFICATION_REPEATER";
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        Context appContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            appContext = context.getApplicationContext();
            AlarmManager am = (AlarmManager) appContext.getSystemService(ALARM_SERVICE);
            Intent tmpIntent = new Intent(appContext, NotificationRepeater.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, 0, tmpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60 * 1000), alarmIntent);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60 * 1000), alarmIntent);
            }

            sendNotification(title, body);
        }

        private void sendNotification(String title, String messageBody) {
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.putExtra("cancel", true);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = appContext.getString(R.string.default_notification_channel_id);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(appContext, channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title)
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
