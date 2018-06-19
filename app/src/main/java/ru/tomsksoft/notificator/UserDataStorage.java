package ru.tomsksoft.notificator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

public class UserDataStorage {

    private static final String TAG = "USER_DATA_STORAGE";

    public static synchronized long getCurrentMessageId(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);
        long id = preference.getLong("current_message_id", 0L);

        SharedPreferences.Editor editor = preference.edit();
        editor.putLong("current_message_id", id + 1L);
        editor.apply();

        Log.d(TAG, "Current id: " + id);
        return id;
    }

    public static void cleanUserData(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferenc.edit();
        editor.putString("login", "login");
        editor.putString("password", "password");
        editor.putString("Message", "");
        editor.putString("Date", "");
        editor.putString("Template", "");
        editor.apply();
    }

    public static String getUserLogin(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        return preferenc.getString("login", "login");
    }

    public static String getUserPassword(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        return preferenc.getString("password", "password");
    }

    public static void saveUserTamplate(Context context, String tamplate) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferenc.edit();
        editor.putString("Template", tamplate);
        editor.apply();
    }

    public static void saveMessage(Context context, String message) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferenc.edit();
        editor.putString("Message", message);
        editor.apply();
    }

    public static String getMessage(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        String message = preferenc.getString("Message", "");
        Log.d(TAG, "message: " + message);
        return message;
    }

    public static String getUserTamplate(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        String tamplate = preferenc.getString("Template", null);
        Log.d(TAG, "user tamplate: " + tamplate);
        return tamplate;
    }

    public static void refreshToken(Context context, String token) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferenc.edit();
        editor.putString("token", token);
        editor.putBoolean("refreshed", true);
        editor.apply();
    }

    public static boolean isTokenRefreshed(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        return preferenc.getBoolean("refreshed", false);
    }

    public static String getToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferenc = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        return preferenc.getString("token", null);
    }
}
