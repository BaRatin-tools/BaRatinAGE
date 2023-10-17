package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.commons.UncertaintyDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesBand;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.baratinage.utils.DateTime;
import org.jfree.data.time.Second;
import org.json.JSONObject;

public class LimnigraphDataset extends AbstractDataset {

    private final LocalDateTime[] dateTime;
    private final int[] sysErrInd;

    private UncertaintyDataset errorMatrixDataset;

    public LimnigraphDataset(String name,
            LocalDateTime[] dateTime,
            double[] stage,
            double[] nonSysErrStd,
            double[] sysErrStd,
            int[] sysErrInd) {
        super(name,
                new NamedColumn("dateTime", DateTime.dateTimeToDoubleVector(dateTime)),
                new NamedColumn("stage", stage),
                new NamedColumn("nonSysErrStd", nonSysErrStd),
                new NamedColumn("sysErrStd", sysErrStd),
                new NamedColumn("sysErrInd", sysErrInd == null ? null : toDouble(sysErrInd)));

        this.dateTime = dateTime;
        this.sysErrInd = sysErrInd;

    }

    public LimnigraphDataset(JSONObject json) {
        super(json);
        this.dateTime = DateTime.doubleToDateTimeVector(getColumn("dateTime"));
        double[] sysErrIndAsDouble = getColumn("sysErrInd");
        this.sysErrInd = sysErrIndAsDouble == null ? null : toInt(sysErrIndAsDouble);
        if (json.has("errorMatrixDataset")) {
            errorMatrixDataset = new UncertaintyDataset(json.getJSONObject("errorMatrixDataset"));
        }
    }

    public double[] getDateTimeAsDouble() {
        return getColumn("dateTime");
    }

    public LocalDateTime[] getDateTime() {
        return dateTime;
    }

    public double[] getStage() {
        return getColumn("stage");
    }

    public double[] getNonSysErrStd() {
        return getColumn("nonSysErrStd");
    }

    public double[] getSysErrStd() {
        return getColumn("sysErrStd");
    }

    public int[] getSysErrInd() {
        return sysErrInd;
    }

    public List<double[]> getStageErrMatrix() {
        if (!hasStageErrMatrix()) {
            return null;
        }
        return errorMatrixDataset.getMatrix();
    }

    public List<double[]> getStageErrUncertaintyEnvelop() {
        return errorMatrixDataset.getUncertaintyEnvelop();
    }

    public boolean hasNonSysErr() {
        return getNonSysErrStd() != null;
    }

    public boolean hasSysErr() {
        return getSysErrStd() != null && getSysErrInd() != null;
    }

    public boolean hasStageErrMatrix() {
        return errorMatrixDataset != null;
    }

    public PlotItem getPlotLine() {
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        PlotTimeSeriesLine plotLine = new PlotTimeSeriesLine(
                getName(),
                timeVector,
                getStage(),
                Color.BLACK, new BasicStroke(2));

        return plotLine;
    }

    public PlotItem getPlotEnv() {
        // FIXME: timeVector is shared with plotLine... refactoring needed.
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        List<double[]> errEnv = getStageErrUncertaintyEnvelop();
        PlotTimeSeriesBand plotBand = new PlotTimeSeriesBand(
                getName(),
                timeVector,
                errEnv.get(0), errEnv.get(1), Color.YELLOW);
        return plotBand;
    }

    @Override
    public JSONObject toJSON(BamProject project) {
        JSONObject json = super.toJSON(project);
        if (hasStageErrMatrix()) {
            json.put("errorMatrixDataset", errorMatrixDataset.toJSON(project));
        }
        return json;
    }

    public void computeErroMatrix(int nCol) {
        // make memory reclaimable (if no other ref elsewhere)
        errorMatrixDataset = null;

        double[] stage = getStage();

        int nRow = getNumberOfRows();
        List<double[]> matrix = new ArrayList<>(nCol);
        try {
            for (int i = 0; i < nCol; i++) {
                double[] column = new double[nRow];
                // initialize with stage values
                for (int j = 0; j < nRow; j++) {
                    column[j] = stage[j];
                    // column[j] = 0;
                }
                matrix.add(column);
            }
        } catch (OutOfMemoryError E) {
            System.err.println("LimnigraphDataset Error: cannot create error matrix because memory is insufficient.");
            return;
        }

        if (hasNonSysErr()) {
            double[] nsStd = getNonSysErrStd();
            addNonSysError(matrix, nsStd);
        }

        if (hasSysErr()) {
            double[] sStd = getSysErrStd();
            int[] sInd = getSysErrInd();
            addSysError(matrix, sStd, sInd);
        }

        if (hasNonSysErr() || hasSysErr()) {
            errorMatrixDataset = new UncertaintyDataset("stageErrorMatrix", matrix);
        }

    }

    // Note: this is coded to limit the number of calls to get the very slow method
    // getErrors() however, this may be quite inefficient memory wise...
    private static void addSysError(List<double[]> errorMatrix, double[] sStd, int[] sInd) {
        Map<Double, Map<Integer, List<Integer>>> indicesPerSysIndAndSysStd = new HashMap<>();

        int nRow = sStd.length;

        for (int k = 0; k < nRow; k++) {
            Double std = sStd[k];
            Integer ind = sInd[k];
            Map<Integer, List<Integer>> indicesPerSysInd = indicesPerSysIndAndSysStd.containsKey(std)
                    ? indicesPerSysIndAndSysStd.get(std)
                    : new HashMap<>();

            List<Integer> indices = indicesPerSysInd.containsKey(ind) ? indicesPerSysInd.get(ind) : new ArrayList<>();

            indices.add(k);
            indicesPerSysInd.put(ind, indices);
            indicesPerSysIndAndSysStd.put(std, indicesPerSysInd);

        }

        int nCol = errorMatrix.size();
        for (Double std : indicesPerSysIndAndSysStd.keySet()) {
            // generate error vector for each unique std
            Map<Integer, List<Integer>> indicesPerSysInd = indicesPerSysIndAndSysStd.get(std);
            int nInd = indicesPerSysInd.size(); // number of sys resampling indices
            double[] errors = getErrors(std, nInd * nCol);
            int k = 0;
            for (Integer ind : indicesPerSysInd.keySet()) {
                // for each resamplgin index, loop over all its associated row indices
                // and for each column add the appropriate error
                List<Integer> indices = indicesPerSysInd.get(ind);
                for (int index : indices) {
                    for (int i = 0; i < nCol; i++) {
                        errorMatrix.get(i)[index] = errorMatrix.get(i)[index] + errors[k * nCol + i];
                    }
                }
                k++;
            }
        }
    }

    // Note: note for method addSysError() also applies here
    private static void addNonSysError(List<double[]> errorMatrix, double[] nsStd) {
        Map<Double, List<Integer>> indicesPerStd = new HashMap<>();
        int nRow = nsStd.length;
        for (int k = 0; k < nRow; k++) {
            Double std = nsStd[k];
            List<Integer> indices = indicesPerStd.containsKey(std) ? indicesPerStd.get(std) : new ArrayList<>();
            indices.add(k);
            indicesPerStd.put(std, indices);
        }

        int nCol = errorMatrix.size();
        for (Double std : indicesPerStd.keySet()) {
            List<Integer> indices = indicesPerStd.get(std);
            int nInd = indices.size();
            double[] errors = getErrors(std, nInd * nCol);
            for (int i = 0; i < nCol; i++) {
                double[] e = errorMatrix.get(i);
                for (int j = 0; j < nInd; j++) {
                    int index = indices.get(j);
                    e[index] = e[index] + errors[j * nCol + i];
                }
            }
        }
    }

    // Note: very slow since getRandomValues() method actually calls an external exe
    // and requires read / write operations to / from the hard drive
    private static double[] getErrors(double std, int n) {
        if (std == 0) {
            double[] zeros = new double[n];
            for (int k = 0; k < n; k++) {
                zeros[k] = 0;
            }
            return zeros;
        }
        Distribution distribution = new Distribution(
                DistributionType.GAUSSIAN,
                0, std);

        return distribution.getRandomValues(n);
    }

}
