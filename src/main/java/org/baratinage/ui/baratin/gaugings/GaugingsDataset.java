package org.baratinage.ui.baratin.gaugings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.Misc;

public class GaugingsDataset extends AbstractDataset implements IPlotDataProvider {

    private static final String STAGE = "stage";
    private static final String DISCHARGE = "discharge";
    private static final String DISCHARGE_U = "dischargePercentUncertainty";
    private static final String DISCHARGE_STD = "dischargeStd";
    private static final String DISCHARGE_LOW = "dischargeStd";
    private static final String DISCHARGE_HIGH = "dischargeStd";
    private static final String STATE = "active";
    private static final String DATETIME = "dateTime";

    final private boolean[] state;
    final private LocalDateTime[] datetime;

    private final Map<String, double[]> derived = new HashMap<>();
    private final Map<String, double[]> gaugingsTrue = new HashMap<>();
    private final Map<String, double[]> gaugingsFalse = new HashMap<>();

    public static GaugingsDataset buildGaugingsDataset(
            String name,
            double[] stage,
            double[] discharge,
            double[] dischargePercentUncertainty,
            boolean[] active,
            LocalDateTime[] datetime) {
        if (active == null) {
            active = new boolean[stage.length];
            for (int k = 0; k < stage.length; k++) {
                active[k] = true;
            }
        }
        GaugingsDataset gds = datetime == null
                ? new GaugingsDataset(name, stage, discharge, dischargePercentUncertainty, active)
                : new GaugingsDataset(name, stage, discharge, dischargePercentUncertainty, active, datetime);

        return gds;
    }

    public static GaugingsDataset buildGaugingsDataset(String name, String hashString) {
        return new GaugingsDataset(name, hashString);
    }

    private GaugingsDataset(
            String name,
            double[] stage,
            double[] discharge,
            double[] dischargePercentUncertainty,
            boolean[] active,
            LocalDateTime[] datetime) {
        super(name,
                new String[] {
                        STAGE, DISCHARGE, DISCHARGE_U, STATE, DATETIME
                },
                stage,
                discharge,
                dischargePercentUncertainty,
                toDouble(active),
                DateTime.dateTimeToDoubleVector(datetime));

        // keep track of original data in its original type
        this.state = active;
        this.datetime = datetime;

        updateDerivedValues();
    }

    private GaugingsDataset(
            String name,
            double[] stage,
            double[] discharge,
            double[] dischargePercentUncertainty,
            boolean[] active) {
        super(name,
                new String[] {
                        STAGE, DISCHARGE, DISCHARGE_U, STATE
                },
                stage,
                discharge,
                dischargePercentUncertainty,
                toDouble(active));
        this.state = active;
        this.datetime = null;
        updateDerivedValues();
    }

    private GaugingsDataset(
            String name,
            double[] stage,
            double[] discharge,
            double[] dischargePercentUncertainty) {
        this(
                name,
                stage,
                discharge,
                dischargePercentUncertainty,
                toBoolean(Misc.ones(stage.length)));
    }

    private GaugingsDataset(String name, String hashString) {
        super(
                name,
                hashString);

        double[] state = getColumn(STATE);
        this.state = toBoolean(state);
        double[] datetime = getColumn(DATETIME);
        this.datetime = datetime == null ? null : DateTime.doubleToDateTimeVector(datetime);

        updateDerivedValues();
    }

    private void updateDerivedValues() {

        // build derived arrays
        double[] q = getColumn(DISCHARGE);
        double[] u = getColumn(DISCHARGE_U);
        double[] std = getStd(q, u);
        double[][] env = getEnv(q, std);

        derived.clear();
        derived.put(DISCHARGE_STD, std);
        derived.put(DISCHARGE_LOW, env[0]);
        derived.put(DISCHARGE_HIGH, env[1]);

        // build derived arrays depending on the true/false state
        gaugingsTrue.clear();
        gaugingsFalse.clear();

        int[] tfn = getTrueFalseLengths(this.state);

        for (String h : super.headers) {
            double[] v = getColumn(h);
            double[][] tf = getTrueFalse(this.state, v, tfn[0], tfn[1]);
            gaugingsTrue.put(h, tf[0]);
            gaugingsFalse.put(h, tf[1]);
        }

        for (String h : derived.keySet()) {
            double[] v = derived.get(h);
            double[][] tf = getTrueFalse(this.state, v, tfn[0], tfn[1]);
            gaugingsTrue.put(h, tf[0]);
            gaugingsFalse.put(h, tf[1]);
        }

    }

    private static double[] getStd(double[] v, double[] u) {
        double[] std = new double[v.length];
        for (int k = 0; k < v.length; k++) {
            std[k] = v[k] * u[k] / 100 / 2;
        }
        return std;
    }

    private static double[][] getEnv(double[] v, double[] std) {
        double[][] env = new double[2][v.length];
        for (int k = 0; k < v.length; k++) {
            double halfUncertaintyInterval = std[k] * 2;
            env[0][k] = v[k] - halfUncertaintyInterval;
            env[1][k] = v[k] + halfUncertaintyInterval;
        }
        return env;
    }

    private static double[][] getTrueFalse(boolean[] state, double[] values, int nTrue, int nFalse) {
        double[] trueValues = new double[nTrue];
        double[] falseValues = new double[nFalse];
        for (int k = 0, kTrue = 0, kFalse = 0; k < state.length; k++) {
            if (state[k]) {
                trueValues[kTrue++] = values[k];
            } else {
                falseValues[kFalse++] = values[k];
            }
        }

        return new double[][] {
                trueValues, falseValues
        };
    }

    private static int[] getTrueFalseLengths(boolean[] state) {
        int n = state.length;
        int nTrue = 0;
        for (boolean a : state)
            if (a)
                nTrue++;
        int nFalse = n - nTrue;
        return new int[] { nTrue, nFalse };
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
        updateDerivedValues();
    }

    public double[] getStageValues() {
        return getColumn(STAGE);
    }

    public double[] getDischargeValues() {
        return getColumn(DISCHARGE);
    }

    public double[] getDischargePercentUncertainty() {
        return getColumn(DISCHARGE_U);
    }

    public double[] getDischargeStdUncertainty() {
        return derived.get(DISCHARGE_STD);
    }

    public double[] getStateAsDouble() {
        return getColumn(STATE);
    }

    public double[] getDateTimeAsDouble() {
        return getColumn(DATETIME);
    }

    public boolean[] getStateAsBoolean() {
        return state;
    }

    public LocalDateTime[] getDateTime() {
        return datetime;
    }

    public double[] getActiveStageValues() {
        return gaugingsTrue.get(STAGE);
    }

    public double[] getActiveDischargeValues() {
        return gaugingsTrue.get(DISCHARGE);
    }

    public double[] getActiveDischargeStdUncertainty() {
        return gaugingsTrue.get(DISCHARGE_STD);
    }

    public double[] getActiveDateTimeAsDouble() {
        return gaugingsTrue.get(DATETIME);
    }

    public List<double[]> getDischargeUncertaintyInterval() {
        List<double[]> uncertaintyInterval = new ArrayList<>();
        uncertaintyInterval.add(derived.get(DISCHARGE_LOW));
        uncertaintyInterval.add(derived.get(DISCHARGE_HIGH));
        return uncertaintyInterval;
    }

    @Override
    public HashMap<String, PlotItem> getPlotItems() {

        PlotPoints activeGaugingsPoints = new PlotPoints(
                "active gaugings",
                gaugingsTrue.get(STAGE),
                gaugingsTrue.get(STAGE),
                gaugingsTrue.get(STAGE),
                gaugingsTrue.get(DISCHARGE),
                gaugingsTrue.get(DISCHARGE_LOW),
                gaugingsTrue.get(DISCHARGE_HIGH),
                AppSetup.COLORS.GAUGING);

        PlotPoints inactiveGaugingsPoints = new PlotPoints(
                "inactive gaugings",
                gaugingsFalse.get(STAGE),
                gaugingsFalse.get(STAGE),
                gaugingsFalse.get(STAGE),
                gaugingsFalse.get(DISCHARGE),
                gaugingsFalse.get(DISCHARGE_LOW),
                gaugingsFalse.get(DISCHARGE_HIGH),
                AppSetup.COLORS.DISCARDED_GAUGING);

        HashMap<String, PlotItem> results = new HashMap<>();
        results.put("active_gaugings", activeGaugingsPoints);
        results.put("inactive_gaugings", inactiveGaugingsPoints);
        return results;
    }

}
