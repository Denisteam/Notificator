package ru.tomsksoft.notificator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashSet;
import java.util.Set;

import ru.tomsksoft.notificator.UI.MainActivity;
import ru.tomsksoft.notificator.alarm.AlarmReceiver;
import ru.tomsksoft.notificator.alarm.AlarmTuner;
import ru.tomsksoft.notificator.alarm.DayOfWeek;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Context appContext = this.getApplicationContext();
        AlarmManager am = (AlarmManager) appContext.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(appContext, NotificationRepeater.class);
        intent.putExtra("body", remoteMessage.getData().get("body"));
        intent.putExtra("title", remoteMessage.getData().get("title"));

        PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (remoteMessage.getData().containsValue("STOP")) {
            am.cancel(alarmIntent);
            return;
        }

        Log.d(TAG, "MessageSender Notification Body: " + remoteMessage.getData().get("body"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, alarmIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmIntent);
        }
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "MessageSender data payload: " + remoteMessage.getData());
        }
    }
}
