package ru.tomsksoft.notificator;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Context.TELEPHONY_SERVICE;

public class MessageSender {
    private static final String SENDER_ID = "AAAARXunFHQ:APA91bFg3eJGuJgy1V5qjHuYgjchhfxRonJ_VeIlFnYVk7onc2k1wvLWngCsu5flIbVTS1oW05kLQJN0erO2LIgbKxtxD8M1zHK9JOfgSJU6AlMgQTG3P7zbcutK1sF16vc1T7QRA8qB";
    private static final AtomicInteger msgId = new AtomicInteger();

    public static void sendMessage(Context context, MessageType type, String content) {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        RemoteMessage.Builder rmb = new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com");

        rmb.setMessageId(Integer.toString(msgId.incrementAndGet()))
                .addData("registraition_token", FirebaseInstanceId.getInstance().getToken());

        if (ContextCompat.checkSelfPermission(context, READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            rmb.addData("telephone_number", ((TelephonyManager) context.getSystemService(TELEPHONY_SERVICE)).getLine1Number());
        } else {
            rmb.addData("telephone_number", "absent");
        }

        rmb.addData("type", type.toString()).addData("content", content);
        fm.send(rmb.build());
    }

    public static void setMessageID(int value) {
        msgId.set(value);
    }

    public static int getMessageID() {
        return msgId.get();
    }
}
