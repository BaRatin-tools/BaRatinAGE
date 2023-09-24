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
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double d : values) {
            if (d < min) {
                min = d;
            }
            if (d > max) {
                max = d;
            }
        }
        return new double[] { min, max };
    }

    public static double[] copy(double[] values) {
        int n = values.length;
        double[] copy = new double[n];
        for (int k = 0; k < n; k++) {
            copy[k] = values[k];
        }
        return copy;
    }

    public static double[] sort(double[] values) {
        double[] sorted = copy(values);
        Arrays.sort(sorted);
        return sorted;
    }

    public static double[] percentiles(double[] values, double... probabilities) {
        return percentiles(false, values, probabilities);
    }

    public static double[] percentiles(boolean valuesAlreadySorted, double[] values, double... probabilities) {
        double[] sorted = valuesAlreadySorted ? values : sort(values);
        int n = values.length;
        int m = probabilities.length;
        double[] percentiles = new double[m];
        for (int k = 0; k < m; k++) {
            double p = probabilities[k];
            if (p > 1)
                p = 1;
            if (p < 0)
                p = 0;
            // int index = (int) Math.floor(p * n);
            int index = Math.round((float) (p * n));
            if (index >= n) {
                System.out.println("ERROR!");
                index = n - 1;
            }
            percentiles[k] = sorted[index];
        }
        return percentiles;
    }

    public static boolean isEven(int value) {
        // source: https://stackoverflow.com/a/7342273
        return (value & 1) == 0;
    }

    public static double[] smooth(double[] toSmooth, int windowSize) {
        System.out.println(windowSize);
        if (windowSize < 2) {
            return toSmooth;
        }
        if (isEven(windowSize)) {
            System.out.println("Even window size unsupported! Increasing window size by one!");
            windowSize++;
        }
        int n = toSmooth.length;
        double[] smoothed = new double[n];
        int halfWindowSize = (int) (windowSize / 2d);
        for (int k = 0; k < n; k++) {
            if (k >= halfWindowSize && k < n - halfWindowSize) {
                double s = 0;
                for (int i = 0; i < windowSize; i++) {
                    int index = k - halfWindowSize + i;
                    s += toSmooth[index];
                }
                smoothed[k] = s / windowSize;
            } else {
                smoothed[k] = Double.NaN;
            }

        }
        return smoothed;

    }

}
