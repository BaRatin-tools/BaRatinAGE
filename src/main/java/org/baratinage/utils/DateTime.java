package org.baratinage.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTime {
    public static double[] dateTimeToDoubleVector(LocalDateTime[] dateTime) {
        int n = dateTime.length;
        double[] dateTimeDouble = new double[n];
        for (int k = 0; k < n; k++) {
            dateTimeDouble[k] = dateTime[k].toEpochSecond(ZoneOffset.UTC);
        }
        return dateTimeDouble;
    }

    public static LocalDateTime[] doubleToDateTimeVector(double[] dataTimeDouble) {
        int n = dataTimeDouble.length;
        LocalDateTime[] dateTime = new LocalDateTime[n];
        for (int k = 0; k < n; k++) {
            dateTime[k] = LocalDateTime.ofEpochSecond((long) dataTimeDouble[k], 0, ZoneOffset.UTC);
        }
        return dateTime;
    }

    public static LocalDateTime[] ymdhmsDoubleToTimeVector(double[] years, double[] months, double[] days,
            double[] hours, double[] minutes, double[] seconds) {
        int n = years.length;
        if (months.length != n) {
            System.err.println("DateTime Error: 'months' vector length not matching 'years' vector length!");
            return null;
        }
        if (days.length != n) {
            System.err.println("DateTime Error: 'days' vector length not matching 'years' vector length!");
            return null;
        }
        if (hours.length != n) {
            System.err.println("DateTime Error: 'hours' vector length not matching 'years' vector length!");
            return null;
        }
        if (minutes.length != n) {
            System.err.println("DateTime Error: 'minutes' vector length not matching 'years' vector length!");
            return null;
        }
        if (seconds.length != n) {
            System.err.println("DateTime Error: 'seconds' vector length not matching 'years' vector length!");
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
