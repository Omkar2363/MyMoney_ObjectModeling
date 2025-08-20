// -------- constants/Month.java --------
package com.geektrust.backend.constants;

import java.util.Locale;

public enum Month {
    JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
    JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;

    public static Month fromString(String s) {
        return Month.valueOf(s.trim().toUpperCase(Locale.ROOT));
    }
}
