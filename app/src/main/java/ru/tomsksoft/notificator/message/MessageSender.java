package ru.tomsksoft.notificator.message;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import ru.tomsksoft.notificator.IncorrectDataException;
import ru.tomsksoft.notificator.UserDataStorage;

public class MessageSender {

    private static final String TAG = "MESSAGE SENDER";
    private static final int TIMEOUT_VALUE = 5000;

    public static boolean send(Context context, Message message) throws IncorrectDataException {
        final String userName = UserDataStorage.getUserLogin(context);
        final String password = UserDataStorage.getUserPassword(context);

        URL url;
        try {
            url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }
            });

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.setReadTimeout(TIMEOUT_VALUE);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            Gson gson = new Gson();
            connection.getOutputStream().write(gson.toJson(message).getBytes());
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String content;
            while ((content = reader.readLine()) != null) {
                Log.d(TAG, content);
            }

            int rc = connection.getResponseCode();
            if (rc == 200) {
                connection.disconnect();
                return true;
            } else if (rc == 401) {
                throw new IncorrectDataException(userName + ":" + password);
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
