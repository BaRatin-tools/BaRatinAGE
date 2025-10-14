package org.baratinage.ui.baratin.gaugings;

import java.awt.Color;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;

public class GaugingsDataset extends AbstractDataset implements IPlotDataProvider {

    private static final String STAGE = "stage";
    private static final String DISCHARGE = "discharge";
    private static final String DISCHARGE_U = "dischargePercentUncertainty";
    private static final String DISCHARGE_STD = "dischargeStd";
    private static final String DISCHARGE_LOW = "dischargeLow";
    private static final String DISCHARGE_HIGH = "dischargeHigh";
    private static final String STAGE_U = "stageAbsoluteUncertainty";
    private static final String STAGE_STD = "stageStd";
    private static final String STAGE_LOW = "stageLow";
    private static final String STAGE_HIGH = "stageHigh";
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
            LocalDateTime[] datetime,
            double[] stageAbsoluteUncertainty) {
        if (active == null) {
            active = new boolean[stage.length];
            for (int k = 0; k < stage.length; k++) {
                active[k] = true;
            }
        }
        String[] headers = new String[] {
                STAGE, DISCHARGE, DISCHARGE_U, STATE,
                datetime == null ? null : DATETIME,
                stageAbsoluteUncertainty == null ? null : STAGE_U
        };
        double[][] columns = new double[][] {
                stage, discharge, dischargePercentUncertainty, toDouble(active),
                datetime == null ? null : DateTime.dateTimeToDoubleArray(datetime),
                stageAbsoluteUncertainty == null ? null : stageAbsoluteUncertainty,
        };
        headers = Arrays.stream(headers)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        columns = Arrays.stream(columns)
                .filter(Objects::nonNull)
                .toArray(double[][]::new);

        GaugingsDataset gds = new GaugingsDataset(name, active, datetime, headers, columns);

        return gds;
    }

    public static GaugingsDataset buildGaugingsDataset(String name, String hashString) {
        return new GaugingsDataset(name, hashString);
    }

    private GaugingsDataset(
            String name,
            boolean[] active,
            LocalDateTime[] datetime,
            String[] headers,
            double[]... columns) {
        super(name, headers, columns);
        this.state = active;
        this.datetime = datetime;
        updateDerivedValues();
    }

    private GaugingsDataset(String name, String hashString) {
        super(
                name,
                hashString);

        double[] state = getColumn(STATE);
        this.state = toBoolean(state);
        double[] datetime = getColumn(DATETIME);
        this.datetime = datetime == null ? null : DateTime.doubleToDateTimeArray(datetime);

        // for back compatibility
        super.renameColumn("stagePercentUncertainty", STAGE_U);

        updateDerivedValues();
    }

    private void updateDerivedValues() {

        // build derived arrays
        double[] q = getColumn(DISCHARGE);
        double[] u = getColumn(DISCHARGE_U);
        double[] std = getStdRelative(q, u);
        double[][] env = getEnv(q, std);

        derived.clear();
        derived.put(DISCHARGE_STD, std);
        derived.put(DISCHARGE_LOW, env[0]);
        derived.put(DISCHARGE_HIGH, env[1]);

        double[] uh = getColumn(STAGE_U);
        if (uh != null) {
            double[] h = getColumn(STAGE);
            double[] stdh = getStdAbsolute(uh);
            double[][] envh = getEnv(h, stdh);
            derived.put(STAGE_STD, stdh);
            derived.put(STAGE_LOW, envh[0]);
            derived.put(STAGE_HIGH, envh[1]);
        }

        // build derived arrays depending on the true/false state
        gaugingsTrue.clear();
        gaugingsFalse.clear();

        int[] tfn = getTrueFalseLengths(this.state);

        for (String header : getHeaders()) {
            double[] v = getColumn(header);
            double[][] tf = getTrueFalse(this.state, v, tfn[0], tfn[1]);
            gaugingsTrue.put(header, tf[0]);
            gaugingsFalse.put(header, tf[1]);
        }

        for (String header : derived.keySet()) {
            double[] v = derived.get(header);
            double[][] tf = getTrueFalse(this.state, v, tfn[0], tfn[1]);
            gaugingsTrue.put(header, tf[0]);
            gaugingsFalse.put(header, tf[1]);
        }

    }

    private static double[] getStdAbsolute(double[] u) {
        double[] std = new double[u.length];
        for (int k = 0; k < u.length; k++) {
            std[k] = u[k] / 2;
        }
        return std;
    }

    private static double[] getStdRelative(double[] v, double[] u) {
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

    public boolean containsMissingValues() {
        for (String columnName : gaugingsTrue.keySet()) {
            double[] column = gaugingsTrue.get(columnName);
            for (int row = 0; row < column.length; row++) {
                if (Double.isNaN(column[row])) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateActiveStateValues(Boolean[] newValues) {
        if (newValues.length != getNumberOfRows()) {
            ConsoleLogger.error("Cannot update active state values because the numbers of rows don't match!");
            return;
        }
        double[] d = getColumn("active");
        for (int k = 0; k < getNumberOfRows(); k++) {
            d[k] = newValues[k] ? 1d : 0d;
            this.state[k] = newValues[k];
        }
        writeDataFile();
        updateDerivedValues();
    }

    public double[] getStageValues() {
        return getColumn(STAGE);
    }

    public double[] getStagePercentUncertainty() {
        return getColumn(STAGE_U);
    }

    public double[] getStageStdUncertainty() {
        return derived.containsKey(STAGE_STD) ? derived.get(STAGE_STD) : null;
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

    public double[] getActiveStageStdUncertainty() {
        return gaugingsTrue.containsKey(STAGE_STD) ? gaugingsTrue.get(STAGE_STD) : null;
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

    public List<double[]> getStageUncertaintyInterval() {
        List<double[]> uncertaintyInterval = new ArrayList<>();
        uncertaintyInterval.add(derived.get(DISCHARGE_LOW));
        uncertaintyInterval.add(derived.get(DISCHARGE_HIGH));
        return uncertaintyInterval;
    }

    public List<double[]> getDischargeUncertaintyInterval() {
        List<double[]> uncertaintyInterval = new ArrayList<>();
        uncertaintyInterval.add(derived.get(DISCHARGE_LOW));
        uncertaintyInterval.add(derived.get(DISCHARGE_HIGH));
        return uncertaintyInterval;
    }

    @Override
    @Deprecated
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

    public static enum PlotType {
        Qh, hQ, Qt, ht
    }

    public PlotPoints getPlotPoints(PlotType plotType) {
        return getPlotPoints(plotType, null);
    }

    public PlotPoints getPlotPoints(PlotType plotType, Boolean active) {

        boolean hasStageUncertainty = getColumn(STAGE_U) != null;

        double[] time = getColumn(DATETIME);
        double[] stage = getColumn(STAGE);
        double[] stageLow = hasStageUncertainty ? derived.get(STAGE_LOW) : stage;
        double[] stageHigh = hasStageUncertainty ? derived.get(STAGE_HIGH) : stage;
        double[] discharge = getColumn(DISCHARGE);
        double[] dischargeLow = derived.get(DISCHARGE_LOW);
        double[] dischargeHigh = derived.get(DISCHARGE_HIGH);
        String lgd = "gaugings";
        Color clr = AppSetup.COLORS.GAUGING;

        if (active != null) {
            Map<String, double[]> data = active ? gaugingsTrue : gaugingsFalse;
            clr = active ? AppSetup.COLORS.GAUGING : AppSetup.COLORS.DISCARDED_GAUGING;
            lgd = active ? "active gaugings" : "inactive gaugings";
            time = data.get(DATETIME);
            stage = data.get(STAGE);
            stageLow = hasStageUncertainty ? data.get(STAGE_LOW) : stage;
            stageHigh = hasStageUncertainty ? data.get(STAGE_HIGH) : stage;
            discharge = data.get(DISCHARGE);
            dischargeLow = data.get(DISCHARGE_LOW);
            dischargeHigh = data.get(DISCHARGE_HIGH);
        }

        double[] x = stage;
        double[] xL = stageLow;
        double[] xH = stageHigh;
        double[] y = discharge;
        double[] yL = dischargeLow;
        double[] yH = dischargeHigh;

        if (plotType.equals(PlotType.hQ)) {
            double[] z = x;
            double[] zL = xL;
            double[] zH = xH;
            x = y;
            xL = yL;
            xH = yH;
            y = z;
            yL = zL;
            yH = zH;
        } else if (plotType.equals(PlotType.Qt) || plotType.equals(PlotType.ht)) {
            x = DateTime.dateTimeDoubleToSecondsDouble(time);
            xL = x;
            xH = x;
            if (plotType.equals(PlotType.ht)) {
                y = stage;
                yL = y;
                yH = y;
            }
        }

        return new PlotPoints(lgd,
                x, xL, xH,
                y, yL, yH,
                clr);
    }
}
