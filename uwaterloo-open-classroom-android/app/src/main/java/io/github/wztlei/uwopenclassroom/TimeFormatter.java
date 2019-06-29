package io.github.wztlei.uwopenclassroom;

import java.util.Locale;

public class TimeFormatter {

    /**
     * Returns a string that is a time in 12h format from a time in 24h format.
     * 
     * @param hour  the hour of the time in 24h format
     * @param min   the minute of the time
     * @return      the time in 12h format
     */
    static String format12hTime(int hour, int min) {
        // Ensure that the hour and minute are within the bounds of a valid time
        if (hour < 0 || hour > 23 || min < 0 || min > 60) {
            throw new IllegalArgumentException();
        }

        // Format the minute by adding 0s as padding if necessary
        String minStr = formatHourOrMin(min);

        // Determine the hour of the day to add AM or PM or subtract 12 from the hour accordingly
        if (hour == 0) {
            return String.format("12:%s AM", minStr);
        } else if (hour <= 11) {
            String hourStr = formatHourOrMin(hour);
            return String.format("%s:%s AM", hourStr, minStr);
        } else if (hour == 12) {
            return String.format("12:%s PM", minStr);
        } else {
            String hourStr = formatHourOrMin(hour - 12);
            return String.format("%s:%s PM", hourStr, minStr);
        }
    }

    /**
     * Returns a string with an integer representing an hour or minute so it has at least 2 digits,
     * by adding 0s to the left as padding if necessary.
     *
     * @param hourOrMin the hour or minute of a time
     * @return          the hour or minute formatted with 0s as left padding
     */
    private static String formatHourOrMin(int hourOrMin) {
        return String.format(Locale.CANADA, "%02d", hourOrMin);
    }
}
