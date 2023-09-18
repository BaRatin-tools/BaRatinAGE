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
}
