package ru.tomsksoft.notificator.message;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
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
    private static boolean alreadyTriedAuthenticating = false;

    public static boolean send(Context context, Message message) throws IncorrectDataException {
        final String userName = UserDataStorage.getUserLogin(context);
        final String password = UserDataStorage.getUserPassword(context);
        alreadyTriedAuthenticating = false;

        URL url;
        try {
            url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Log.d(TAG, userName);
            Log.d(TAG, password);
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (!alreadyTriedAuthenticating) {
                        alreadyTriedAuthenticating = true;
                        return new PasswordAuthentication(userName, password.toCharArray());
                    } else {
                        return null;
                    }
                }
            });

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.setReadTimeout(TIMEOUT_VALUE);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            Gson gson = new Gson();
            Log.d(TAG, gson.toJson(message));
            connection.getOutputStream().write(gson.toJson(message).getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            connection.connect();

            String content;
            while ((content = reader.readLine()) != null) {
                Log.d(TAG, content);
            }

            int rc = connection.getResponseCode();
            Log.d(TAG, String.valueOf(rc));
            if (rc == 200) {
                connection.disconnect();
                if ("token_add".equals(message.getMethod())) {
                    UserDataStorage.setTokenRefreshed(context, false);
                    Log.d(TAG, "Token successfully refreshed!");
                }
                return true;
            } else if (rc == 401) {
                connection.disconnect();
                throw new IncorrectDataException(userName + ":" + password);
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkLogIn(Context context, final String userName, final String password) throws IncorrectDataException {
        alreadyTriedAuthenticating = false;

        try {
            URL url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {

                    if (!alreadyTriedAuthenticating) {
                        alreadyTriedAuthenticating = true;
                        return new PasswordAuthentication(userName, password.toCharArray());
                    } else {
                        return null;
                    }
                }
            });
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.setReadTimeout(TIMEOUT_VALUE);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.connect();
            int rc = connection.getResponseCode();
            if (rc == 200) {
                UserDataStorage.saveUserLogin(context, userName);
                UserDataStorage.saveUserPassword(context, password);
                if (UserDataStorage.isTokenRefreshed(context)) {
                    Message message = new Message(context, RPCMethod.TOKEN_ADD);
                    message.addParam("token", UserDataStorage.getToken(context));
                    message.addParam("model", Build.MANUFACTURER + " " + Build.MODEL);
                    message.addParam("os", Build.VERSION.RELEASE);
                    send(context, message);
                }
                return true;
            } else if (rc == 401) {
                throw new IncorrectDataException(userName + ":" + password);
            }
            connection.disconnect();
        } catch (SocketTimeoutException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
