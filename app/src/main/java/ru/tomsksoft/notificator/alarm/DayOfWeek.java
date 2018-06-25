package ru.tomsksoft.notificator.alarm;


import java.util.ArrayList;
import java.util.List;

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

    DayOfWeek(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getMaskByDayOfWeekList(DayOfWeek... daysOfWeeks) {
        int tmp = 0;
        for (DayOfWeek dayOfWeek: daysOfWeeks) {
            tmp += dayOfWeek.value;
        }

        return tmp;
    }

    public boolean isDayOfWeekSet(int mask) {
        return !((this.getValue() & mask) == 0);
    }

    public List<DayOfWeek> getListOfDayOfWeekByMask(int mask) {
        List<DayOfWeek> dayOfWeeks = new ArrayList<>();

        for (DayOfWeek dayOfWeek: values()) {
            if ( (dayOfWeek.getValue() & mask) == 1) {
                dayOfWeeks.add(dayOfWeek);
            }
        }

        return dayOfWeeks;
    }
}
