package ru.tomsksoft.notificator.exceptions;

import android.util.Log;

public class IncorrectDataException extends Exception {
    private static final String TAG = "IncorrectData";

    public IncorrectDataException(String message) {
        super(message);
        Log.i(TAG, message);
    }
}
