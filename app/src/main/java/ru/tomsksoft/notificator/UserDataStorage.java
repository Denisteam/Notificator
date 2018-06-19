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
    public static void saveNotificationsCheck(Context context, boolean setNotifications) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("setNotifications", setNotifications);
        editor.apply();

    }

    public static void saveAlarmParam(Context context, boolean setAlarm, boolean monday, boolean tuesday, boolean wednesday,
                                      boolean thursday, boolean friday, boolean saturday, boolean sunday, int hour, int minute) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("set_Alarm", setAlarm);
        editor.putBoolean(DayOfWeek.MONDAY.toString(), monday);
        editor.putBoolean(DayOfWeek.TUESDAY.toString(), tuesday);
        editor.putBoolean(DayOfWeek.WEDNESDAY.toString(), wednesday);
        editor.putBoolean(DayOfWeek.THURSDAY.toString(), thursday);
        editor.putBoolean(DayOfWeek.FRIDAY.toString(), friday);
        editor.putBoolean(DayOfWeek.SATURDAY.toString(), saturday);
        editor.putBoolean(DayOfWeek.SUNDAY.toString(), sunday);
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.apply();
    }

    public static boolean getAlarmCheck(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean("set_Alarm", false);
        Log.d(TAG, "set_Alarm: " + data);
        return data;
    }

    public static boolean getMonday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.MONDAY.toString(), false);
        Log.d(TAG, "monday: " + data);
        return data;
    }

    public static boolean getTuesday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.TUESDAY.toString(), false);
        Log.d(TAG, "tuesday: " + data);
        return data;
    }

    public static boolean getWednesday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.WEDNESDAY.toString(), false);
        Log.d(TAG, "wednesday: " + data);
        return data;
    }

    public static boolean getThursday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.THURSDAY.toString(), false);
        Log.d(TAG, "thursday: " + data);
        return data;
    }

    public static boolean getFriday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.FRIDAY.toString(), false);
        Log.d(TAG, "friday: " + data);
        return data;
    }

    public static boolean getSaturday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.SATURDAY.toString(), false);
        Log.d(TAG, "saturday: " + data);
        return data;
    }

    public static boolean getSunday(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean(DayOfWeek.SUNDAY.toString(), false);
        Log.d(TAG, "sunday: " + data);
        return data;
    }

    public static int getHour(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        int data = preference.getInt("hour", 0);
        Log.d(TAG, "hour: " + data);
        return data;
    }

    public static int getMinute(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        int data = preference.getInt("minute", 0);
        Log.d(TAG, "minute: " + data);
        return data;
    }

    public static boolean getNotificationsCheck(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);

        boolean data = preference.getBoolean("setNotifications", true);
        Log.d(TAG, "setNotifications: " + data);
        return data;
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
