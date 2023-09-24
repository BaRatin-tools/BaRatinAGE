package org.baratinage.ui.commons;

import java.awt.BasicStroke;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;
import org.baratinage.utils.Misc;

public class DensityPlotItem {

    private final double[] sorted;
    private final double min;
    private final double max;
    private final double u95lower;
    private final double u95upper;
    private double[] plotX;
    private double[] plotY;
    private double[] plotXenv;
    private double[] plotYenv;
    private double[] plotYenvZeros;

    private PlotLine line;
    private PlotBand u95envelop;

    public DensityPlotItem(double[] values) {
        sorted = Calc.sort(values);
        double[] minmax = Calc.range(values);
        min = minmax[0];
        max = minmax[1];
        double[] u95 = Calc.percentiles(true, sorted, 0.025, 0.975);
        u95lower = u95[0];
        u95upper = u95[1];
    }

    public void update(int nBins, int smoothingWindow) {
        int n = sorted.length;
        double[] p = Misc.makeGrid(min, max, nBins + 1);
        plotX = new double[nBins];
        plotY = new double[nBins];

        // plotXenv = new double[nBins];
        // plotYenv = new double[nBins];
        // plotYenvZeros = new double[nBins];

        for (int k = 0; k < nBins; k++) {
            plotX[k] = p[nBins];
            plotY[k] = 0;
            // plotXenv[k] = Double.NaN;
            // plotYenv[k] = Double.NaN;
            // plotYenvZeros[k] = 0;
        }

        // double d = Double.NaN;

        int index = 0;
        double lower = p[index];
        double upper = p[index + 1];
        plotX[index] = (lower + upper) / 2;
        double count = 0;

        for (int k = 0; k < n; k++) {
            if (sorted[k] >= lower && sorted[k] < upper) {
                count++;
            } else {
                plotX[index] = (lower + upper) / 2;
                plotY[index] = count / n;
                // if (lower >= u95lower && upper <= u95upper) {
                // plotXenv[index] = plotX[index];
                // plotYenv[index] = plotY[index];
                // }

                index++;

                if (index >= nBins) {
                    System.out.println("Err0");
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

        line = new PlotLine(
                "",
                plotX,
                plotY,
                AppConfig.AC.DENSITY_LINE_COLOR,
                new BasicStroke(2));

        create95area();

    }

    private void create95area() {
        int n = plotX.length;
        plotXenv = new double[n];
        plotYenv = new double[n];
        plotYenvZeros = new double[n];
        for (int k = 0; k < n; k++) {
            if (plotX[k] >= u95lower && plotX[k] <= u95upper) {
                plotXenv[k] = plotX[k];
                plotYenv[k] = plotY[k];
                plotYenvZeros[k] = 0;
            } else {
                plotXenv[k] = Double.NaN;
                plotYenv[k] = Double.NaN;
                plotYenvZeros[k] = Double.NaN;
            }
        }
        u95envelop = new PlotBand(
                "",
                plotXenv,
                plotYenv,
                plotYenvZeros,
                AppConfig.AC.DENSITY_ENVELOP_COLOR);
    }

    public PlotLine getPlotLine() {
        return line;
    }

    public PlotBand getPlotBand() {
        return u95envelop;
    }

}
