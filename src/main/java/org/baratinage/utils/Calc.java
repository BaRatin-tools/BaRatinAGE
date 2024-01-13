package org.baratinage.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static double[] percentiles(double[] values, boolean isSorted, double... probabilities) {
        double[] sorted = isSorted ? values : sort(values);
        int n = values.length;
        int m = probabilities.length;
        double[] percentiles = new double[m];
        for (int k = 0; k < m; k++) {
            double p = probabilities[k];
            if (p > 1)
                p = 1;
            if (p < 0)
                p = 0;
            int index = Math.round((float) (p * n));
            if (index >= n) {
                ConsoleLogger.log("ERROR!");
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

    public static double[] smoothArray(double[] toSmooth, int halfWindowSize) {
        if (halfWindowSize < 1) {
            return toSmooth;
        }
        int n = toSmooth.length;
        double[] smoothed = new double[n];
        for (int k = 0; k < n; k++) {
            double sum = toSmooth[k];
            int m = 1;
            for (int w = 1; w <= halfWindowSize; w++) {
                int belowIndex = k - w;
                int aboveIndex = k + w;
                if (belowIndex >= 0 && aboveIndex < n) { // enforce balance between left and right
                    sum += toSmooth[belowIndex];
                    sum += toSmooth[aboveIndex];
                    m += 2;
                }
            }
            smoothed[k] = sum / (double) m;
            // if (m != halfWindowSize * 2 + 1) {
            // discard edges approach:
            // smoothed[k] = Double.NaN;
            // do not smooth edges approach
            // smoothed[k] = toSmooth[k];
            // }

        }
        return smoothed;
    }

    public static List<double[]> density(double[] sorted, int bins) {
        int n = sorted.length;
        double[] p = Misc.makeGrid(sorted[0], sorted[n - 1], bins + 1);
        double[] values = new double[bins];
        double[] densities = new double[bins];

        for (int k = 0; k < bins; k++) {
            values[k] = p[bins];
            densities[k] = 0;
        }

        int index = 0;
        double lower = p[index];
        double upper = p[index + 1];
        values[index] = (lower + upper) / 2;
        double count = 0;

        for (int k = 0; k < n; k++) {
            if (sorted[k] >= lower && sorted[k] < upper) {
                count++;
            } else {
                double x = (lower + upper) / 2;
                double xRange = upper - lower;
                double y = count / (n * xRange);

                values[index] = x;
                densities[index] = y;
                index++;

                if (index >= bins) {
                    break;
                }

                lower = p[index];
                upper = p[index + 1];
                values[index] = (lower + upper) / 2;
                densities[index] = 0;
                count = 0;
            }
        }

        List<double[]> result = new ArrayList<>();
        result.add(values);
        result.add(densities);
        return result;
    }

    public static double[] zeroes(int n) {
        double[] x = new double[n];
        for (int k = 0; k < n; k++) {
            x[k] = 0;
        }
        return x;
    }

    public static double[] sequence(double from, double to, int n) {
        double[] x = new double[n];
        double step = (to - from) / n;
        for (int k = 0; k < n; k++) {
            x[k] = from + step * k;
        }
        return x;
    }

}
