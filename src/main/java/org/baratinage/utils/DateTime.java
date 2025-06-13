package org.baratinage.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTime {

    public static String dateTimeToTimeStamp(LocalDateTime dateTime, String format) {
        if (format == null) {
            format = "yyyyMMdd_HHmmss";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static double[] dateTimeToDoubleArray(LocalDateTime[] dateTime) {
        int n = dateTime.length;
        double[] dateTimeDouble = new double[n];
        for (int k = 0; k < n; k++) {
            dateTimeDouble[k] = dateTime[k].toEpochSecond(ZoneOffset.UTC);
        }
        return dateTimeDouble;
    }

    public static double[] dateTimeToDoubleArrayMilliseconds(LocalDateTime[] dateTime) {
        int n = dateTime.length;
        double[] dateTimeDouble = new double[n];
        for (int k = 0; k < n; k++) {
            dateTimeDouble[k] = dateTime[k].toEpochSecond(ZoneOffset.UTC) * 1000;
        }
        return dateTimeDouble;
    }

    public static LocalDateTime doubleToDateTime(double dateTimeDouble) {
        return LocalDateTime.ofEpochSecond((long) dateTimeDouble, 0, ZoneOffset.UTC);
    }

    public static LocalDateTime[] doubleToDateTimeArray(double[] dateTimeDouble) {
        int n = dateTimeDouble.length;
        LocalDateTime[] dateTime = new LocalDateTime[n];
        for (int k = 0; k < n; k++) {
            dateTime[k] = doubleToDateTime(dateTimeDouble[k]);
        }
        return dateTime;
    }

    public static double[] dateTimeDoubleToSecondsDouble(double[] data) {
        double[] res = new double[data.length];
        for (int k = 0; k < data.length; k++) {
            res[k] = data[k] * 1000;
        }
        return res;
    }

    public static LocalDateTime[] ymdhmsDoubleToTimeArray(double[] years, double[] months, double[] days,
            double[] hours, double[] minutes, double[] seconds) {
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
