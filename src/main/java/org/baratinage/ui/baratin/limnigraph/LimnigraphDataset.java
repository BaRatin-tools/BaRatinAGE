package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.commons.UncertaintyDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesBand;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.Misc;
import org.jfree.data.time.Second;

public class LimnigraphDataset extends AbstractDataset {

    private final LocalDateTime[] dateTime;
    private final int[] sysErrInd;

    private UncertaintyDataset errorMatrixDataset;

    private final TreeSet<Integer> missingValueIndices;

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

        missingValueIndices = Misc.getMissingValuesIndices(getColumn("stage"));
        if (getColumn("stage") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("stage")));
        }
        if (getColumn("nonSysErrStd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("nonSysErrStd")));
        }
        if (getColumn("sysErrStd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("sysErrStd")));
        }
        if (getColumn("sysErrInd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("sysErrInd")));
        }
    }

    public LimnigraphDataset(String name, String hashString) {
        this(name, hashString, null, null, -1);
    }

    public LimnigraphDataset(String name, String hashString, String errMatrixName, String errMatrixHashString,
            int nCol) {
        super(name, hashString,
                "dateTime",
                "stage",
                "nonSysErrStd",
                "sysErrStd",
                "sysErrInd");
        this.dateTime = DateTime.doubleToDateTimeVector(getColumn("dateTime"));
        double[] sysErrIndAsDouble = getColumn("sysErrInd");
        this.sysErrInd = sysErrIndAsDouble == null ? null : toInt(sysErrIndAsDouble);
        if (errMatrixName != null && errMatrixHashString != null && nCol > 0) {
            errorMatrixDataset = new UncertaintyDataset(errMatrixName, errMatrixHashString, nCol);
        }

        missingValueIndices = Misc.getMissingValuesIndices(getColumn("stage"));
        if (getColumn("stage") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("stage")));
        }
        if (getColumn("nonSysErrStd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("nonSysErrStd")));
        }
        if (getColumn("sysErrStd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("sysErrStd")));
        }
        if (getColumn("sysErrInd") != null) {
            missingValueIndices.addAll(Misc.getMissingValuesIndices(getColumn("sysErrInd")));
        }
    }

    public double[] getDateTimeAsDouble() {
        return getColumn("dateTime");
    }

    public LocalDateTime[] getDateTime() {
        return dateTime;
    }

    public double[] getStage() {
        return getStage(false);
    }

    public double[] getStage(boolean removeMissingValues) {
        double[] stageVector = getColumn("stage");
        List<double[]> stage = new ArrayList<>();
        stage.add(stageVector);
        if (removeMissingValues) {
            return Misc.removeMissingValues(stage, missingValueIndices).get(0);
        } else {
            return stageVector;
        }
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
        return getStageErrMatrix(false);
    }

    public List<double[]> getStageErrMatrix(boolean removeMissingValues) {
        if (!hasStageErrMatrix()) {
            return null;
        }
        if (removeMissingValues) {
            return Misc.removeMissingValues(errorMatrixDataset.getMatrix(), missingValueIndices);
        } else {
            return errorMatrixDataset.getMatrix();
        }
    }

    public double[] getMissingValueIndicesAsDouble() {
        int n = missingValueIndices.size();
        double[] mvIndicesAsDouble = new double[n];
        int k = 0;
        for (Integer i : missingValueIndices) {
            mvIndicesAsDouble[k++] = i.doubleValue();
        }
        return mvIndicesAsDouble;
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
                AppSetup.COLORS.PLOT_LINE,
                new BasicStroke(2));

        return plotLine;
    }

    public PlotItem getPlotEnv() {
        // FIXME: timeVector is shared with plotLine... refactoring needed.
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        List<double[]> errEnv = getStageErrUncertaintyEnvelop();
        PlotTimeSeriesBand plotBand = new PlotTimeSeriesBand(
                getName(),
                timeVector,
                errEnv.get(0), errEnv.get(1), AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);
        return plotBand;
    }

    @Override
    public DatasetConfig save(boolean writeToFile) {
        DatasetConfig adcr = super.save(writeToFile);
        if (hasStageErrMatrix()) {
            DatasetConfig errMatrixSaveRecord = errorMatrixDataset.save(writeToFile);
            adcr.nested.add(errMatrixSaveRecord);
        }
        return adcr;
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
                }
                matrix.add(column);
            }
        } catch (OutOfMemoryError E) {
            ConsoleLogger.error("cannot create error matrix because memory is insufficient.");
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
        if (Double.isNaN(std)) {
            double[] nans = new double[n];
            for (int k = 0; k < n; k++) {
                nans[k] = Double.NaN;
            }
            return nans;
        }
        Distribution distribution = new Distribution(
                DistributionType.GAUSSIAN,
                0, std);

        return distribution.getRandomValues(n);
    }

}
