package ru.tomsksoft.notificator.message;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ru.tomsksoft.notificator.UserCreditans;
import ru.tomsksoft.notificator.UserDataStorage;
import ru.tomsksoft.notificator.exceptions.IncorrectDataException;

public class MessageSender {

    private static final String TAG = "MESSAGE SENDER";
    private static final int TIMEOUT_VALUE = 5000;
    private static boolean alreadyTriedAuthenticating = false;

    public static boolean send(Context context, Message message) throws IncorrectDataException, InterruptedException {
        UserCreditans authData = new UserDataStorage(context).getUserAuthData();
        final String userName = authData.getLogin();
        final String password = authData.getPassword();
        alreadyTriedAuthenticating = false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL url;
                try {
                    url = new URL("https://extern.tomsksoft.com/user/note/set/");

                    Log.d(TAG, userName);
                    Log.d(TAG, password);
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userName, password.toCharArray());
                        }
                    });
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setConnectTimeout(TIMEOUT_VALUE);
                    connection.setReadTimeout(TIMEOUT_VALUE);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Accept-Charset", "UTF-8");
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
                    connection.disconnect();

                    if (rc == 200) {
                        if ("token_add".equals(message.getMethod())) {
                            UserDataStorage dataStorage = new UserDataStorage(context);
                            dataStorage.setTokenRefreshed(false);
                            Log.d(TAG, "Token successfully refreshed!");
                        }
                        return true;
                    } else if (rc == 401) {
                        new UserDataStorage(context).cleanUserData();
                        throw new IncorrectDataException(userName + ":" + password);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        executor.shutdown();

        executor.awaitTermination(5, TimeUnit.SECONDS);

        if (result.isDone()) {
            boolean res;
            try {
                res = result.get();
            } catch (ExecutionException e) {
                throw new IncorrectDataException(userName + ":" + password);
            }
            Log.d(TAG, "connecting result: " + String.valueOf(res));
            return res;
        }

        return false;
    }

    public static boolean checkLogIn(Context context, final String userName, final String password) throws IncorrectDataException, InterruptedException {
        alreadyTriedAuthenticating = false;

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL url;
                try {
                    url = new URL("https://extern.tomsksoft.com/user/note/set/");

                    Log.d(TAG, userName);
                    Log.d(TAG, password);
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userName, password.toCharArray());
                        }
                    });

                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setConnectTimeout(TIMEOUT_VALUE);
                    connection.setReadTimeout(TIMEOUT_VALUE);
                    connection.setInstanceFollowRedirects(false);
                    connection.connect();

                    int rc = connection.getResponseCode();
                    connection.disconnect();
                    Log.d(TAG, String.valueOf(rc));
                    if (rc == 200) {
                        UserDataStorage dataStorage = new UserDataStorage(context);
                        dataStorage.saveUserAuthData(new UserCreditans(userName, password));

                        return true;
                    } else if (rc == 401) {
                        throw new IncorrectDataException(userName + ":" + password);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        if (result.isDone()) {
            boolean res;
            try {
                res = result.get();
            } catch (ExecutionException e) {
                throw new IncorrectDataException(userName + ":" + password);
            }
            Log.d(TAG, "connecting result: " + String.valueOf(res));
            return res;
        }

        return false;
    }
}
