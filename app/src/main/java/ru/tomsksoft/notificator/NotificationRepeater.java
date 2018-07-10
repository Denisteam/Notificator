package ru.tomsksoft.notificator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import ru.tomsksoft.notificator.UI.LoginActivity;
import ru.tomsksoft.notificator.UI.MainActivity;

import static android.content.Context.ALARM_SERVICE;

public class NotificationRepeater extends BroadcastReceiver {
        private static final String TAG = "NOTIFICATION_REPEATER";
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        Context appContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            String body = intent.getStringExtra("body");
            appContext = context.getApplicationContext();
            AlarmManager am = (AlarmManager) appContext.getSystemService(ALARM_SERVICE);
            Intent tmpIntent = new Intent(appContext, NotificationRepeater.class);
            tmpIntent.putExtra("body", body);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, 0, tmpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 180000, alarmIntent);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 180000, alarmIntent);
            }

            Log.d(TAG, body);
            sendNotification(body);
        }

        private void sendNotification(String messageBody) {
            Intent intent = new Intent(appContext, MainActivity.class);
            intent.putExtra("cancel", true);
            intent.putExtra("notificationText", messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = appContext.getString(R.string.default_notification_channel_id);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(appContext, channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Notificator")
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setShowWhen(true)
                            .setVibrate(new long[]{1000, 2000, 1000, 2000, 1000, 2000})
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
