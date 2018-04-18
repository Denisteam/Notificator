package ru.tomsksoft.notificator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Context.TELEPHONY_SERVICE;

public class MessageSender {

    private static final String TAG  = "MessageSender";
    private static final String SENDER_ID = "AAAARXunFHQ:APA91bFg3eJGuJgy1V5qjHuYgjchhfxRonJ_VeIlFnYVk7onc2k1wvLWngCsu5flIbVTS1oW05kLQJN0erO2LIgbKxtxD8M1zHK9JOfgSJU6AlMgQTG3P7zbcutK1sF16vc1T7QRA8qB";
    private static final AtomicInteger msgId = new AtomicInteger();

    public static boolean sendMessage(Context context, String time) throws IncorrectDataException {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final String userName = sharedPref.getString("login", "");
        final String passvord = sharedPref.getString("password", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        URL url = null;

        try {
            url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, passvord.toCharArray());
            }});

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.getOutputStream().write(("{\"jsonrpc\": \"2.0\", \"method\":" +
                " \"notification\", \"params\": {\"type\": 1, \"message\": \"will " +
                "be in office at " + time + "\", \"date\": \"" + dateFormat.format(new Date()) + "\", \"remind_at\": " +
                "\"2018-03-19 13:15:00\"}, \"id\": 1}").getBytes());
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String content;
            while ( (content = reader.readLine()) != null) {
                Log.d(TAG, content);
            }

            int rc = connection.getResponseCode();
            if (rc == 200) {
                return true;
            } else if(rc == 401) {
                throw new IncorrectDataException(userName + ":" + passvord);
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean sendMessage(Context context, MessageType type, String content) {
        return false;
    }

    public static void setMessageID(int value) {
        msgId.set(value);
    }

    public static int getMessageID() {
        return msgId.get();
    }
}
