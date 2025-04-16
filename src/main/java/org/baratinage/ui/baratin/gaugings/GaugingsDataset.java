package org.baratinage.ui.baratin.gaugings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.ConsoleLogger;

public class GaugingsDataset extends AbstractDataset implements IPlotDataProvider {

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

    public GaugingsDataset(String name, String hashString) {
        super(name, hashString, "stage", "discharge", "dischargePercentUncertainty", "active");
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

    public void updateActiveStateValues(Boolean[] newValues) {
        if (newValues.length != getNumberOfRows()) {
            ConsoleLogger.error("Cannot update active state values because the numbers of rows don't match!");
            return;
        }
        double[] d = getColumn("active");
        for (int k = 0; k < getNumberOfRows(); k++) {
            d[k] = newValues[k] ? 1d : 0d;
        }
        writeDataFile();
    }

    public double[] getActiveStageValues() {
        return splitArray(getStageValues(), getActiveStateAsBoolean()).get(true);
    }

    public double[] getActiveDischargeValues() {
        return splitArray(getDischargeValues(), getActiveStateAsBoolean()).get(true);
    }

    public double[] getActiveDischargeStdUncertainty() {
        return splitArray(getDischargeStdUncertainty(), getActiveStateAsBoolean()).get(true);
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

    // public

    private static Map<Boolean, double[]> splitArray(double[] array, boolean[] filter) {
        int n = filter.length;
        double[] trueValues = new double[n];
        double[] falseValues = new double[n];
        int tIndex = 0, fIndex = 0;

        for (int i = 0; i < n; i++) {
            if (filter[i]) {
                trueValues[tIndex++] = array[i];
            } else {
                falseValues[fIndex++] = array[i];
            }
        }
        Map<Boolean, double[]> result = new HashMap<>();
        result.put(true, Arrays.copyOf(trueValues, tIndex));
        result.put(false, Arrays.copyOf(falseValues, fIndex));
        return result;
    }

    @Override
    public HashMap<String, PlotItem> getPlotItems() {

        double[] stage = getStageValues();
        double[] discharge = getDischargeValues();
        List<double[]> dischargeUncertainty = getDischargeUncertaintyInterval();
        boolean[] activeGaugings = getActiveStateAsBoolean();

        Map<Boolean, double[]> splitStage = splitArray(stage, activeGaugings);
        Map<Boolean, double[]> splitDischarge = splitArray(discharge, activeGaugings);
        Map<Boolean, double[]> splitDischargeUncertaintyLow = splitArray(dischargeUncertainty.get(0), activeGaugings);
        Map<Boolean, double[]> splitDischargeUncertaintyHight = splitArray(dischargeUncertainty.get(1), activeGaugings);

        PlotPoints activeGaugingsPoints = new PlotPoints(
                "active gaugings",
                splitStage.get(true),
                splitStage.get(true),
                splitStage.get(true),
                splitDischarge.get(true),
                splitDischargeUncertaintyLow.get(true),
                splitDischargeUncertaintyHight.get(true),
                AppSetup.COLORS.GAUGING);

        PlotPoints inactiveGaugingsPoints = new PlotPoints(
                "inactive gaugings",
                splitStage.get(false),
                splitStage.get(false),
                splitStage.get(false),
                splitDischarge.get(false),
                splitDischargeUncertaintyLow.get(false),
                splitDischargeUncertaintyHight.get(false),
                AppSetup.COLORS.DISCARDED_GAUGING);

        HashMap<String, PlotItem> results = new HashMap<>();
        results.put("active_gaugings", activeGaugingsPoints);
        results.put("inactive_gaugings", inactiveGaugingsPoints);
        return results;
    }

}
