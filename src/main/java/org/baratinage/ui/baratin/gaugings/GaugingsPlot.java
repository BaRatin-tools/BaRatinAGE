package org.baratinage.ui.baratin.gaugings;

import java.awt.Color;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.ui.plot.PlotItem.SHAPE;

public class GaugingsPlot extends Plot {

    PlotPoints gaugingPoints;
    PlotPoints gaugingPointsInactive;

    public GaugingsPlot(String xAxisLabel, String yAxisLabel, boolean includeLegend, GaugingsDataset gaugingDataset) {
        super(xAxisLabel, yAxisLabel, includeLegend);

        double[] x = gaugingDataset.getStageValues();
        double[] y = gaugingDataset.getDischargeValues();
        double[] u = gaugingDataset.getDischargePercentUncertainty();
        boolean[] v = gaugingDataset.getActiveStateAsBoolean();

        int nTotal = x.length;
        int nActive = 0;
        for (int k = 0; k < nTotal; k++) {
            if (v[k])
                nActive++;
        }

        double[] x0 = new double[nActive];
        double[] y0 = new double[nActive];
        double[] yLow0 = new double[nActive];
        double[] yHigh0 = new double[nActive];
        double[] x1 = new double[nTotal - nActive];
        double[] y1 = new double[nTotal - nActive];
        double[] yLow1 = new double[nTotal - nActive];
        double[] yHigh1 = new double[nTotal - nActive];
        int i = 0;
        int j = 0;
        for (int k = 0; k < nTotal; k++) {
            double yu = y[k] * u[k] / 100;
            if (v[k]) {
                x0[i] = x[k];
                y0[i] = y[k];
                yLow0[i] = y[k] - yu;
                yHigh0[i] = y[k] + yu;
                i++;
            } else {
                x1[j] = x[k];
                y1[j] = y[k];
                yLow1[j] = y[k] - yu;
                yHigh1[j] = y[k] + yu;
                j++;
            }
        }
        if (nActive != 0) {
            gaugingPoints = new PlotPoints("Jaugeages (actif)",
                    x0, x0, x0,
                    y0, yLow0, yHigh0,
                    Color.BLUE, SHAPE.CIRCLE, 5, 1);
            addXYItem(gaugingPoints);
        }

        if (nActive != nTotal) {
            gaugingPointsInactive = new PlotPoints("Jaugeages (inactif)",
                    x1, x1, x1,
                    y1, yLow1, yHigh1,
                    Color.RED, SHAPE.CIRCLE, 5, 1);
            addXYItem(gaugingPointsInactive);
        }
    }

    public PlotPoints getGaugingsPoints() {
        return gaugingPoints;
    }

    public PlotPoints getGaugingsPointsInactive() {
        return gaugingPointsInactive;
    }

}
