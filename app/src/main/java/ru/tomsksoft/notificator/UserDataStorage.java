package ru.tomsksoft.notificator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.tomsksoft.notificator.alarm.DayOfWeek;

public class UserDataStorage {
    private SharedPreferences preferences;
    private static final String TAG = "USER_DATA_STORAGE";

    private static final String TIME = "time";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String DAYS_OF_WEEK = "days_of_week";
    private static final String CURRENT_MESSAGE_ID = "current_message_id";
    private static final String TEMPLATE = "template";
    private static final String TOKEN = "token";
    private static final String REFRESHED = "refreshed";
    private static final String MESSAGE = "message";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SET_NOTIFICATION = "set_notifications";
    private static final String SET_ALARM = "set_alarm";

    public UserDataStorage(Context context) {
        Context appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);
    }

    public static synchronized long getCurrentMessageId(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preference = appContext.getSharedPreferences(appContext.getString(R.string.settings_storage), Context.MODE_PRIVATE);
        long id = preference.getLong(CURRENT_MESSAGE_ID, 0L);

        SharedPreferences.Editor editor = preference.edit();
        editor.putLong(CURRENT_MESSAGE_ID, id + 1L);
        editor.apply();

        Log.d(TAG, "Current id: " + id);
        return id;
    }

    public void saveUserAuthData(UserCreditans creditans) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LOGIN, creditans.getLogin());
        editor.putString(PASSWORD, creditans.getPassword());
        editor.apply();
    }

    public UserCreditans getUserAuthData() {
        String login = preferences.getString(LOGIN, "login");
        String password = preferences.getString(PASSWORD, "password");

        return new UserCreditans(login, password);
    }

    public void cleanUserData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LOGIN);
        editor.remove(PASSWORD);
        editor.remove(MESSAGE);
        editor.remove(TEMPLATE);
        editor.putString("Date", "");
        editor.apply();
    }

    public void saveUserTemplate(String template) {
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, "user template: " + template);
        editor.putString(TEMPLATE, template);
        editor.apply();
    }

    public String getUserTemplate() {
        String template = preferences.getString(TEMPLATE, null);
        Log.d(TAG, "user template: " + template);
        return template;
    }

    public void saveMessage(String message) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MESSAGE, message);
        editor.apply();
    }

    public String getMessage() {
        String message = preferences.getString(MESSAGE, "");
        Log.d(TAG, "message: " + message);
        return message;
    }

    public void saveNotificationsCheck(boolean setNotifications) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SET_NOTIFICATION, setNotifications);
        editor.apply();
    }

    public boolean getNotificationsCheck() {
        boolean data = preferences.getBoolean(SET_NOTIFICATION, true);
        Log.d(TAG, "setNotifications: " + data);
        return data;
    }

    public void saveAlarmParam(boolean setAlarm, Set<DayOfWeek> dayOfWeeks, int hour, int minute) {
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, Integer.toString(DayOfWeek.getMaskByDayOfWeekList(dayOfWeeks)));
        editor.putBoolean(SET_ALARM, setAlarm);
        editor.putInt(DAYS_OF_WEEK, DayOfWeek.getMaskByDayOfWeekList(dayOfWeeks));
        editor.putInt(HOUR, hour);
        editor.putInt(MINUTE, minute);
        editor.apply();
    }

    public boolean isDayOfWeekSet(DayOfWeek dayOfWeek) {
        return (preferences.getInt(DAYS_OF_WEEK, 0) & dayOfWeek.getValue()) > 0;
    }

    public Set<DayOfWeek> loadDaysOfWeekSet() {
        return DayOfWeek.getListOfDayOfWeekByMask(preferences.getInt(DAYS_OF_WEEK, 0));
    }

    public boolean getAlarmCheck() {
        boolean data = preferences.getBoolean(SET_ALARM, false);
        Log.d(TAG, "set_Alarm: " + data);
        return data;
    }
    
    public Calendar getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, preferences.getInt(HOUR, 0));
        calendar.set(Calendar.MINUTE, preferences.getInt(MINUTE, 0));
        return calendar;
    }

    public void refreshToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, token);
        editor.putBoolean(REFRESHED, true);
        editor.apply();
    }

    public String getToken() {
        return preferences.getString(TOKEN, null);
    }

    public boolean isTokenRefreshed() {
        return preferences.getBoolean(REFRESHED, false);
    }

    public void setTokenRefreshed(boolean refreshed) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(REFRESHED, refreshed);
        editor.apply();
    }
}
