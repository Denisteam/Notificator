package ru.tomsksoft.notificator.alarm;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum DayOfWeek {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(4),
    THURSDAY(8),
    FRIDAY(16),
    SATURDAY(32),
    SUNDAY(64);

    private int value;
    public static final int WEEK = 127;
    private static final String TAG = "DAY_OF_WEEK";

    DayOfWeek(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int getMaskByDayOfWeekList(Set<DayOfWeek> daysOfWeeks) {
        int tmp = 0;
        for (DayOfWeek dayOfWeek: daysOfWeeks) {
            tmp += dayOfWeek.value;
        }

        return tmp;
    }

    public static int countOfDaysBetween(DayOfWeek first, DayOfWeek second) {
        int difference = second.ordinal() - first.ordinal();

        if (difference < 0) {
            return (7 + difference);
        }

        return difference;
    }

    public boolean isDayOfWeekSet(int mask) {
        return !((this.getValue() & mask) == 0);
    }

    public static Set<DayOfWeek> getListOfDayOfWeekByMask(int mask) {
        Set<DayOfWeek> dayOfWeeks = new HashSet<>();

        for (DayOfWeek dayOfWeek: values()) {
            if ( (dayOfWeek.getValue() & mask) > 0) {
                dayOfWeeks.add(dayOfWeek);
            }
        }
        return dayOfWeeks;
    }
}
