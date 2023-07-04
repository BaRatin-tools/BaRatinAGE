package org.baratinage.utils;

import java.util.Arrays;

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

    public static double[] copy(double[] values) {
        int n = values.length;
        double[] copy = new double[n];
        for (int k = 0; k < n; k++) {
            copy[k] = values[k];
        }
        return copy;
    }

    public static double[] percentiles(double[] values, double probabilities[]) {

        double[] copy = copy(values);
        Arrays.sort(copy);
        int n = values.length;
        int m = probabilities.length;
        double[] percentiles = new double[m];
        for (int k = 0; k < m; k++) {
            double p = probabilities[k];
            if (p > 1)
                p = 1;
            if (p < 0)
                p = 0;
            int index = (int) Math.floor(p * n);
            percentiles[k] = copy[index];
        }
        return percentiles;
    }
}
