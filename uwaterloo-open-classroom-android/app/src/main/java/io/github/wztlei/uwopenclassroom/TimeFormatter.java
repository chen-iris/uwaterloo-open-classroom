package io.github.wztlei.uwopenclassroom;

import java.util.Locale;

public class TimeFormatter {
     static String format12hTime(int hourIn24hTime, int min) {
        if (hourIn24hTime < 0 || hourIn24hTime > 23 || min < 0 || min > 60) {
            throw new IllegalArgumentException();
        }

        String minStr = formatHourOrMin(min);

        if (hourIn24hTime == 0) {
            return String.format("12:%s AM", minStr);
        } else if (hourIn24hTime <= 11) {
            String hourStr = formatHourOrMin(hourIn24hTime);
            return String.format("%s:%s AM", hourStr, minStr);
        } else if (hourIn24hTime == 12) {
            return String.format("12:%s PM", minStr);
        } else {
            String hourStr = formatHourOrMin(hourIn24hTime - 12);
            return String.format("%s:%s PM", hourStr, minStr);
        }
    }

    private static String formatHourOrMin(int hourOrMin) {
        return String.format(Locale.CANADA, "%02d", hourOrMin);
    }
}
