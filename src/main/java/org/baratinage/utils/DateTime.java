package org.baratinage.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility helpers for dealing with date-time conversions.
 * <p>
 * Provides static methods to convert between LocalDateTime and epoch seconds
 * (UTC), timestamps, and to operate on arrays of date-times.
 * </p>
 */
public class DateTime {

    /**
     * Convert a LocalDateTime to a timestamp string using the provided format.
     * If the format is null, a default pattern of yyyyMMdd_HHmmss is used.
     *
     * @param dateTime the LocalDateTime to format
     * @param format   the date-time pattern to use, or null for the default
     * @return formatted timestamp string
     */
    public static String dateTimeToTimeStamp(LocalDateTime dateTime, String format) {
        if (format == null) {
            format = "yyyyMMdd_HHmmss";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Convert a LocalDateTime to epoch seconds (UTC).
     *
     * @param dateTime input date-time
     * @return number of seconds since the epoch (UTC)
     */
    public static double dateTimeToDouble(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * Convert an array of LocalDateTime to an array of epoch seconds (UTC).
     *
     * @param dateTime input array
     * @return array of epoch seconds
     */
    public static double[] dateTimeToDoubleArray(LocalDateTime[] dateTime) {
        int n = dateTime.length;
        double[] dateTimeDouble = new double[n];
        for (int k = 0; k < n; k++) {
            dateTimeDouble[k] = dateTime[k].toEpochSecond(ZoneOffset.UTC);
        }
        return dateTimeDouble;
    }

    /**
     * Convert an array of LocalDateTime to milliseconds since epoch (UTC).
     *
     * @param dateTime input array
     * @return array of milliseconds since epoch
     */
    public static double[] dateTimeToDoubleArrayMilliseconds(LocalDateTime[] dateTime) {
        int n = dateTime.length;
        double[] dateTimeDouble = new double[n];
        for (int k = 0; k < n; k++) {
            dateTimeDouble[k] = dateTime[k].toEpochSecond(ZoneOffset.UTC) * 1000;
        }
        return dateTimeDouble;
    }

    /**
     * Convert epoch seconds to LocalDateTime in UTC.
     *
     * @param dateTimeDouble seconds since epoch
     * @return corresponding LocalDateTime in UTC
     */
    public static LocalDateTime doubleToDateTime(double dateTimeDouble) {
        return LocalDateTime.ofEpochSecond((long) dateTimeDouble, 0, ZoneOffset.UTC);
    }

    /**
     * Convert an array of epoch seconds to LocalDateTime objects (UTC).
     *
     * @param dateTimeDouble input seconds since epoch
     * @return array of LocalDateTime
     */
    public static LocalDateTime[] doubleToDateTimeArray(double[] dateTimeDouble) {
        int n = dateTimeDouble.length;
        LocalDateTime[] dateTime = new LocalDateTime[n];
        for (int k = 0; k < n; k++) {
            dateTime[k] = doubleToDateTime(dateTimeDouble[k]);
        }
        return dateTime;
    }

    /**
     * Convert an array of values in seconds to milliseconds by scaling by 1000.
     *
     * @param data input seconds
     * @return values in milliseconds
     */
    public static double[] dateTimeDoubleToSecondsDouble(double[] data) {
        double[] res = new double[data.length];
        for (int k = 0; k < data.length; k++) {
            res[k] = data[k] * 1000;
        }
        return res;
    }

    /**
     * Build LocalDateTime[] from arrays of year, month, day, hour, minute, second
     * values.
     * All arrays must have the same length.
     *
     * @param years   years
     * @param months  months
     * @param days    days
     * @param hours   hours
     * @param minutes minutes
     * @param seconds seconds
     * @return array of LocalDateTime objects
     */
    public static LocalDateTime[] ymdhmsDoubleToTimeArray(
            double[] years,
            double[] months,
            double[] days,
            double[] hours,
            double[] minutes,
            double[] seconds) {
        int n = years.length;
        if (months.length != n) {
            ConsoleLogger.error("'months' vector length not matching 'years' vector length!");
            return null;
        }
        if (days.length != n) {
            ConsoleLogger.error("'days' vector length not matching 'years' vector length!");
            return null;
        }
        if (hours.length != n) {
            ConsoleLogger.error("'hours' vector length not matching 'years' vector length!");
            return null;
        }
        if (minutes.length != n) {
            ConsoleLogger.error("'minutes' vector length not matching 'years' vector length!");
            return null;
        }
        if (seconds.length != n) {
            ConsoleLogger.error("'seconds' vector length not matching 'years' vector length!");
            return null;
        }
        LocalDateTime[] dateTime = new LocalDateTime[n];
        for (int k = 0; k < n; k++) {
            dateTime[k] = LocalDateTime.of(
                    (int) years[k],
                    (int) months[k],
                    (int) days[k],
                    (int) hours[k],
                    (int) minutes[k],
                    (int) seconds[k],
                    0);
        }
        return dateTime;
    }
}
