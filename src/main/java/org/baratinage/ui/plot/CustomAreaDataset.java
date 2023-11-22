package org.baratinage.ui.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.DefaultXYDataset;

public class CustomAreaDataset extends DefaultXYDataset {

    private List<double[][]> allData = new ArrayList<>();

    public void addHorizontalBandSeries(String label, double[] x, double[] yLow, double[] yHigh) {
        int n = x.length;
        double[][] data = new double[2][n * 2];
        int index = 0;
        for (int k = 0; k < n; k++) {
            data[0][index] = x[k];
            data[1][index] = yLow[k];
            index++;
        }
        for (int k = n - 1; k >= 0; k--) {
            data[0][index] = x[k];
            data[1][index] = yHigh[k];
            index++;
        }
        allData.add(data);
        addSeries(label, data);
    }

    public void addVerticalBandSeries(String label, double[] y, double[] xLow, double[] xHigh) {
        int n = y.length;
        double[][] data = new double[2][n * 2];
        int index = 0;
        for (int k = 0; k < n; k++) {
            data[0][index] = xLow[k];
            data[1][index] = y[k];
            index++;
        }
        for (int k = n - 1; k >= 0; k--) {
            data[0][index] = xHigh[k];
            data[1][index] = y[k];
            index++;
        }
        allData.add(data);
        addSeries(label, data);
    }

    public void addPolygonSeries(String label, double[] x, double[] y) {
        double[][] data = new double[][] { x, y };
        addSeries(label, data);
    }

    public double[] getXValues(int series) {
        return allData.get(series)[0];
    }

    public double[] getYValues(int series) {
        return allData.get(series)[1];
    }

}
