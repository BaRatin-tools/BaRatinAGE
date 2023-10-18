package org.baratinage.ui.baratin.gaugings;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.plot.PlotPoints;
import org.json.JSONObject;

public class GaugingsDataset extends AbstractDataset {

    private static double[] ones(int n) {
        double[] d = new double[n];
        for (int k = 0; k < n; k++) {
            d[k] = 1;
        }
        return d;
    }

    public GaugingsDataset(String name, double[] stage, double[] discharge, double[] dischargePercentUncertainty) {
        super(name,
                new NamedColumn("stage", stage),
                new NamedColumn("discharge", discharge),
                new NamedColumn("dischargePercentUncertainty",
                        dischargePercentUncertainty),
                new NamedColumn("active", ones(stage.length)));
    }

    public GaugingsDataset(JSONObject json) {
        super(json);
    }

    public double[] getStageValues() {
        return getColumn("stage");
    }

    public double[] getDischargeValues() {
        return getColumn("discharge");
    }

    public double[] getDischargePercentUncertainty() {
        return getColumn("dischargePercentUncertainty");
    }

    public double[] getDischargeStdUncertainty() {
        int nRow = getNumberOfRows();
        double[] u = new double[nRow];
        double[] q = getDischargeValues();
        double[] uqp = getDischargePercentUncertainty();
        for (int k = 0; k < nRow; k++) {
            u[k] = q[k] * uqp[k] / 100 / 2;
        }
        return u;
    }

    /**
     * Compute and return the 95% uncertainty interval of each gaugings
     * 
     * @return a list with two elements: the lower and upper limit of the
     *         uncertainty interval
     */
    public List<double[]> getDischargeUncertaintyInterval() {
        double[] q = getDischargeValues();
        double[] u = getDischargePercentUncertainty();
        int n = q.length;
        double[] lowerLimit = new double[n];
        double[] upperLimit = new double[n];
        for (int k = 0; k < n; k++) {
            // FIXME: check!
            double uHalfInterval = u[k] / 100 * q[k];
            lowerLimit[k] = q[k] - uHalfInterval;
            upperLimit[k] = q[k] + uHalfInterval;
        }
        List<double[]> uncertaintyInterval = new ArrayList<>();
        uncertaintyInterval.add(lowerLimit);
        uncertaintyInterval.add(upperLimit);
        return uncertaintyInterval;
    }

    public double[] getActiveStateAsDouble() {
        return getColumn("active");
    }

    public boolean[] getActiveStateAsBoolean() {
        return toBoolean(getActiveStateAsDouble());
    }

    private static List<List<double[]>> splitMatrix(List<double[]> data,
            boolean[] filter) {
        List<List<double[]>> res = new ArrayList<>();
        int nCol = data.size();
        if (nCol == 0) {
            System.err.println("GaugingsDataset Error: Empty matrix!");
            res.add(data);
            res.add(new ArrayList<>());
            return res;
        }
        int nRow = data.get(0).length;
        if (filter.length != nRow) {
            System.err.println("GaugingsDataset Error: Inconsistent filter array length!");
            res.add(data);
            res.add(new ArrayList<>());
            return res;
        }
        int nRowWith = 0;
        for (int k = 0; k < nRow; k++) {
            if (filter[k]) {
                nRowWith++;
            }
        }
        int nRowWithout = nRow - nRowWith;

        List<double[]> withData = new ArrayList<>();
        List<double[]> withoutData = new ArrayList<>();
        for (int k = 0; k < nCol; k++) {
            withData.add(new double[nRowWith]);
            withoutData.add(new double[nRowWithout]);
        }

        int iWith = 0;
        int iWithout = 0;
        for (int i = 0; i < nRow; i++) {
            if (filter[i]) {
                for (int j = 0; j < nCol; j++) {
                    withData.get(j)[iWith] = data.get(j)[i];
                }
                iWith++;
            } else {
                for (int j = 0; j < nCol; j++) {
                    withoutData.get(j)[iWithout] = data.get(j)[i];
                }
                iWithout++;
            }
        }

        res.add(withData);
        res.add(withoutData);
        return res;
    }

    public List<PlotPoints> getPlotPointsItems() {

        double[] stage = getStageValues();
        double[] discharge = getDischargeValues();
        List<double[]> dischargeUncertainty = getDischargeUncertaintyInterval();

        List<double[]> toSplitData = new ArrayList<>();
        toSplitData.add(stage);
        toSplitData.add(discharge);
        toSplitData.add(dischargeUncertainty.get(0));
        toSplitData.add(dischargeUncertainty.get(1));
        List<List<double[]>> splitData = splitMatrix(toSplitData, getActiveStateAsBoolean());

        PlotPoints activeGaugingsPoints = new PlotPoints(
                "active gaugings",
                splitData.get(0).get(0),
                splitData.get(0).get(0),
                splitData.get(0).get(0),
                splitData.get(0).get(1),
                splitData.get(0).get(2),
                splitData.get(0).get(3),
                AppConfig.AC.GAUGING_COLOR);

        PlotPoints inactiveGaugingsPoints = new PlotPoints(
                "inactive gaugings",
                splitData.get(1).get(0),
                splitData.get(1).get(0),
                splitData.get(1).get(0),
                splitData.get(1).get(1),
                splitData.get(1).get(2),
                splitData.get(1).get(3),
                AppConfig.AC.DISCARDED_GAUGING_COLOR);

        List<PlotPoints> results = new ArrayList<>();
        results.add(activeGaugingsPoints);
        results.add(inactiveGaugingsPoints);
        return results;
    }

}
