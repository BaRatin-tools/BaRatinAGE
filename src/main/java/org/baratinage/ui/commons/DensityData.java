package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.Calc;
import org.baratinage.utils.Misc;

public class DensityData {

    private final double[] sorted;
    private final double min;
    private final double max;
    // private final double u95lower;
    // private final double u95upper;
    private double[] plotX;
    private double[] plotY;
    private double[] plotZeros;
    // private double[] plotXenv;
    // private double[] plotYenv;
    // private double[] plotYenvZeros;

    public DensityData(double[] values) {
        sorted = Calc.sort(values);
        double[] minmax = Calc.range(values);
        min = minmax[0];
        max = minmax[1];
        // double[] u95 = Calc.percentiles(true, sorted, 0.025, 0.975);
        // u95lower = u95[0];
        // u95upper = u95[1];
    }

    public void update(int nBins, int smoothingWindow) {
        int n = sorted.length;
        double[] p = Misc.makeGrid(min, max, nBins + 1);
        plotX = new double[nBins];
        plotY = new double[nBins];
        plotZeros = new double[nBins];

        for (int k = 0; k < nBins; k++) {
            plotX[k] = p[nBins];
            plotY[k] = 0;
            plotZeros[k] = 0;
        }

        int index = 0;
        double lower = p[index];
        double upper = p[index + 1];
        plotX[index] = (lower + upper) / 2;
        double count = 0;

        for (int k = 0; k < n; k++) {
            if (sorted[k] >= lower && sorted[k] < upper) {
                count++;
            } else {
                double x = (lower + upper) / 2;
                double xRange = upper - lower;
                double y = count / (n * xRange);

                plotX[index] = x;
                plotY[index] = y;
                index++;

                if (index >= nBins) {
                    break;
                }

                lower = p[index];
                upper = p[index + 1];
                plotX[index] = (lower + upper) / 2;
                plotY[index] = 0;
                count = 0;
            }
        }

        plotY = Calc.smooth(plotY, smoothingWindow);

        // create95area();
    }

    // private void create95area() {
    // int n = plotX.length;
    // plotXenv = new double[n];
    // plotYenv = new double[n];
    // plotYenvZeros = new double[n];
    // for (int k = 0; k < n; k++) {
    // if (plotX[k] >= u95lower && plotX[k] <= u95upper) {
    // plotXenv[k] = plotX[k];
    // plotYenv[k] = plotY[k];
    // plotYenvZeros[k] = 0;
    // } else {
    // plotXenv[k] = Double.NaN;
    // plotYenv[k] = Double.NaN;
    // plotYenvZeros[k] = Double.NaN;
    // }
    // }

    // }

    public List<double[]> getLineData() {
        List<double[]> data = new ArrayList<>();
        data.add(plotX);
        data.add(plotY);
        data.add(plotZeros);
        return data;
    }

    // public List<double[]> getEnvelopData() {
    // List<double[]> data = new ArrayList<>();
    // data.add(plotXenv);
    // data.add(plotYenv);
    // return data;
    // }

    public static double[] buildZeroArray(int n) {
        double[] x = new double[n];
        for (int k = 0; k < n; k++) {
            x[k] = 0;
        }
        return x;
    }
}
