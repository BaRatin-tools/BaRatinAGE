package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.jfree.data.time.Second;

public class LimnigraphDataset extends AbstractDataset {

    private static final String DATETIME = "dateTime";
    private static final String STAGE = "stage";
    private static final String NONSYSERR_STD = "nonSysErrStd";
    private static final String SYSERR_STD = "sysErrStd";
    private static final String SYSERR_IND = "sysErrInd";

    private final LocalDateTime[] dateTime;
    private final int[] sysErrInd;

    private final UncertaintyDataset errorMatrixDataset;

    // private final TreeSet<Integer> missingValueIndices;
    private final BitSet invalidRowsIndices;

    public LimnigraphDataset(String name,
            LocalDateTime[] dateTime,
            double[] stage,
            double[] nonSysErrStd,
            double[] sysErrStd,
            int[] sysErrInd) {
        super(name,
                new String[] {
                        DATETIME, STAGE, NONSYSERR_STD, SYSERR_STD, SYSERR_IND
                },
                DateTime.dateTimeToDoubleVector(dateTime),
                stage,
                nonSysErrStd,
                sysErrStd,
                sysErrInd == null ? null : toDouble(sysErrInd));

        this.dateTime = dateTime;
        this.sysErrInd = sysErrInd;
        this.invalidRowsIndices = getMissingValuesIndices(
                this.dateTime.length,
                getColumn(STAGE),
                getColumn(NONSYSERR_STD),
                getColumn(SYSERR_STD),
                getColumn(SYSERR_IND));

        errorMatrixDataset = buildUncertaintyDataset(
                stage,
                AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get(),
                nonSysErrStd,
                sysErrStd,
                sysErrInd);
    }

    public LimnigraphDataset(String name, String hashString) {
        this(name, hashString, null, null);
    }

    public LimnigraphDataset(
            String name,
            String hashString,
            String errMatrixName,
            String errMatrixHashString) {
        super(name, hashString, NONSYSERR_STD, SYSERR_STD, SYSERR_IND);

        this.dateTime = DateTime.doubleToDateTimeArray(getColumn(DATETIME));
        double[] sysErrIndAsDouble = getColumn(SYSERR_IND);
        this.sysErrInd = sysErrIndAsDouble == null ? null : toInt(sysErrIndAsDouble);

        errorMatrixDataset = errMatrixName != null && errMatrixHashString != null
                ? new UncertaintyDataset(errMatrixName, errMatrixHashString)
                : null;

        this.invalidRowsIndices = getMissingValuesIndices(
                this.dateTime.length,
                getColumn(STAGE),
                getColumn(NONSYSERR_STD),
                getColumn(SYSERR_STD),
                getColumn(SYSERR_IND));

    }

    public double[] getDateTimeAsDouble() {
        return getColumn(DATETIME);
    }

    public LocalDateTime[] getDateTime() {
        return dateTime;
    }

    public double[] getStage() {
        return getStage(false);
    }

    public double[] getStage(boolean removeMissingValues) {
        if (removeMissingValues) {
            return removeMissingValues(invalidRowsIndices, getColumn(STAGE)).get(0);
        } else {
            return getColumn(STAGE);
        }
    }

    public double[] getNonSysErrStd() {
        return getColumn(NONSYSERR_STD);
    }

    public double[] getSysErrStd() {
        return getColumn(SYSERR_STD);
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
            return removeMissingValues(invalidRowsIndices, errorMatrixDataset.getMatrix());
        } else {
            return errorMatrixDataset.getMatrix();
        }
    }

    public double[] getMissingValueIndicesAsDouble() {
        List<Integer> mvIndices = invalidRowsIndices.stream().boxed().toList();
        int n = mvIndices.size();
        double[] mvIndicesAsDouble = new double[n];
        int k = 0;
        for (Integer i : mvIndices) {
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

    // **************************************************************
    // Missing Values Related Methods

    private static BitSet getMissingValuesIndices(int nRows, double[]... columns) {
        List<double[]> values = new ArrayList<>();
        for (int k = 0; k < columns.length; k++) {
            if (columns[k] != null) {
                values.add(columns[k]);
            }
        }
        BitSet mvIndices = new BitSet();
        for (int i = 0; i < nRows; i++) {
            boolean missing = false;
            for (int j = 0; j < values.size(); j++) {
                if (Double.isNaN(values.get(j)[i])) {
                    missing = true;
                    break;
                }
            }
            if (missing) {
                mvIndices.set(i);
            }
        }
        return mvIndices;
    }

    private static List<double[]> removeMissingValues(
            BitSet invalidRowFlags,
            double[]... columns) {
        List<double[]> fullMatrix = new ArrayList<>();
        for (int k = 0; k < columns.length; k++) {
            fullMatrix.add(columns[k]);
        }
        return removeMissingValues(invalidRowFlags, fullMatrix);
    }

    private static List<double[]> removeMissingValues(
            BitSet invalidRowFlags,
            List<double[]> fullMatrix) {
        if (fullMatrix.size() == 0) {
            return new ArrayList<>();
        }
        int rowCount = fullMatrix.get(0).length;
        int colCount = fullMatrix.size();
        int validRowCount = rowCount - invalidRowFlags.cardinality();

        List<double[]> mvFreeMatrix = new ArrayList<>(colCount);
        for (int col = 0; col < colCount; col++) {
            double[] cleanCol = new double[validRowCount];
            int idx = 0;
            for (int row = 0; row < rowCount; row++) {
                if (!invalidRowFlags.get(row)) {
                    cleanCol[idx++] = fullMatrix.get(col)[row];
                }
            }
            mvFreeMatrix.add(cleanCol);
        }

        return mvFreeMatrix;
    }

    // **************************************************************
    // Uncertainty Related Methods

    private static UncertaintyDataset buildUncertaintyDataset(
            double[] stage,
            int nCol,
            double[] nonSysErrStd,
            double[] sysErrStd,
            int[] sysErrInd) {
        boolean hasNonSysErr = nonSysErrStd != null;
        boolean hasSysErr = sysErrStd != null && sysErrInd != null;

        if (!hasNonSysErr && !hasSysErr) {
            return null;
        }

        int nRow = stage.length;
        double[][] matrix = new double[nCol][nRow];
        try {
            for (int i = 0; i < nCol; i++) {
                // initialize with stage values
                for (int j = 0; j < nRow; j++) {
                    matrix[i][j] = stage[j];
                }
            }
        } catch (OutOfMemoryError E) {
            ConsoleLogger.error("cannot create error matrix because memory is insufficient.");
            return null;
        }

        if (hasNonSysErr) {
            addNonSysError(matrix, nonSysErrStd);
        }

        if (hasSysErr) {
            addSysError(matrix, sysErrStd, sysErrInd);
        }

        return new UncertaintyDataset("stageErrorMatrix", matrix);
    }

    // Note: this is coded to limit the number of calls to get the very slow method
    // getErrors() however, this may be quite inefficient memory wise...
    private static void addSysError(double[][] errorMatrix, double[] sStd, int[] sInd) {
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

        int nCol = errorMatrix.length;
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
                        errorMatrix[i][index] = errorMatrix[i][index] + errors[k * nCol + i];
                    }
                }
                k++;
            }
        }
    }

    // Note: the note for method addSysError() also applies here
    private static void addNonSysError(double[][] errorMatrix, double[] nsStd) {
        Map<Double, List<Integer>> indicesPerStd = new HashMap<>();
        int nRow = nsStd.length;
        for (int k = 0; k < nRow; k++) {
            Double std = nsStd[k];
            List<Integer> indices = indicesPerStd.containsKey(std) ? indicesPerStd.get(std) : new ArrayList<>();
            indices.add(k);
            indicesPerStd.put(std, indices);
        }

        int nCol = errorMatrix.length;
        for (Double std : indicesPerStd.keySet()) {
            List<Integer> indices = indicesPerStd.get(std);
            int nInd = indices.size();
            double[] errors = getErrors(std, nInd * nCol);
            for (int i = 0; i < nCol; i++) {
                double[] e = errorMatrix[i];
                for (int j = 0; j < nInd; j++) {
                    int index = indices.get(j);
                    e[index] = e[index] + errors[j * nCol + i];
                }
            }
        }
    }

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
