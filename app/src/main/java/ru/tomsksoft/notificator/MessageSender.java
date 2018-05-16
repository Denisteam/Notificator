package ru.tomsksoft.notificator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;

public class MessageSender
{

    private static final String TAG  = "MessageSender";
    private static final String SENDER_ID = "AAAARXunFHQ:APA91bFg3eJGuJgy1V5qjHuYgjchhfxRonJ_VeIlFnYVk7onc2k1wvLWngCsu5flIbVTS1oW05kLQJN0erO2LIgbKxtxD8M1zHK9JOfgSJU6AlMgQTG3P7zbcutK1sF16vc1T7QRA8qB";
    private static final AtomicInteger msgId = new AtomicInteger();
    private static final int TIMEOUT_VALUE = 5000;


    public static boolean sendMessage(Context context, String date, String msg) throws IncorrectDataException
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final String userName = sharedPref.getString("login", "login");
        final String password = sharedPref.getString("password", "password");

        URL url = null;

        try {
            url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }});

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.setReadTimeout(TIMEOUT_VALUE);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.getOutputStream().write(("{\"jsonrpc\": \"2.0\", \"method\":" +
                    " \"notification\", \"params\": {\"type\": 1, \"message\": \"" + msg + "\", \"date\": \"" + date + "\", \"remind_at\": " +
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
                throw new IncorrectDataException(userName + ":" + password);
            }
            connection.disconnect();

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
