package org.baratinage.utils;

public class Calc {

    public static double sum(double[] values) {
        double s = 0;
        for (double d : values) {
            s += d;
        }
        return s;
    }

    public static double mean(double[] values) {
        double s = Calc.sum(values);
        return s / values.length;
    }

    public static double min(double[] values) {
        double m = Double.POSITIVE_INFINITY;
        for (double d : values) {
            if (d < m) {
                m = d;
            }
        }
        return m;
    }

    public static double max(double[] values) {
        double m = Double.NEGATIVE_INFINITY;
        for (double d : values) {
            if (d > m) {
                m = d;
            }
        }
        return m;
    }

    public static double[] range(double[] values) {
        return new double[] { Calc.min(values), Calc.max(values) };
    }
}
