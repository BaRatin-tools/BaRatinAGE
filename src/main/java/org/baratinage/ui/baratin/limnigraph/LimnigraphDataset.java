package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.jbam.UncertainData;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.DateTime;

public class LimnigraphDataset extends AbstractDataset {

    private static final String DATETIME = "dateTime";
    private static final String STAGE = "stage";
    private static final String NONSYSERR_STD = "nonSysErrStd";
    private static final String SYSERR_STD = "sysErrStd";
    private static final String SYSERR_IND = "sysErrInd";

    private final double[] dateTimeMillis;
    private final LocalDateTime[] dateTime;
    private final int[] sysErrInd;

    private final UncertainData uncertainData;

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
                DateTime.dateTimeToDoubleArray(dateTime),
                stage,
                nonSysErrStd,
                sysErrStd,
                sysErrInd == null ? null : toDouble(sysErrInd));

        this.dateTime = dateTime;
        this.dateTimeMillis = DateTime.dateTimeToDoubleArrayMilliseconds(dateTime);
        this.sysErrInd = sysErrInd;
        this.invalidRowsIndices = getMissingValuesIndices(
                this.dateTime.length,
                getColumn(STAGE),
                getColumn(NONSYSERR_STD),
                getColumn(SYSERR_STD),
                getColumn(SYSERR_IND));

        if (nonSysErrStd != null || (sysErrStd != null && sysErrInd != null)) {
            uncertainData = new UncertainData(
                    name,
                    stage,
                    nonSysErrStd == null ? new double[] {} : nonSysErrStd,
                    sysErrStd == null ? new double[] {} : sysErrStd,
                    sysErrInd == null ? new int[] {} : sysErrInd);
        } else {
            uncertainData = null;
        }
    }

    public LimnigraphDataset(
            String name,
            String hashString) {
        super(name, hashString, NONSYSERR_STD, SYSERR_STD, SYSERR_IND);

        this.dateTime = DateTime.doubleToDateTimeArray(getColumn(DATETIME));
        this.dateTimeMillis = DateTime.dateTimeToDoubleArrayMilliseconds(dateTime);
        double[] sysErrIndAsDouble = getColumn(SYSERR_IND);
        this.sysErrInd = sysErrIndAsDouble == null ? null : toInt(sysErrIndAsDouble);

        double[] nonSysErrStd = getColumn(NONSYSERR_STD);
        double[] sysErrStd = getColumn(SYSERR_STD);
        double[] sysErrInd = getColumn(SYSERR_IND);

        if (nonSysErrStd != null || (sysErrStd != null && sysErrInd != null)) {
            uncertainData = new UncertainData(
                    name,
                    getColumn(STAGE),
                    getColumn(NONSYSERR_STD),
                    getColumn(SYSERR_STD),
                    sysErrInd == null ? null : toInt(getColumn(SYSERR_IND)));

        } else {
            uncertainData = null;
        }

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

    public List<double[]> getStageErrMatrix(boolean removeMissingValues) {
        if (!hasStageErrMatrix()) {
            return null;
        }
        double[][] errorMatrix = uncertainData.getErrorMatrix(AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get());
        List<double[]> errorMatrixList = new ArrayList<>();
        for (int k = 0; k < errorMatrix.length; k++) {
            errorMatrixList.add(errorMatrix[k]);
        }
        if (removeMissingValues) {
            return removeMissingValues(invalidRowsIndices, errorMatrixList);
        } else {
            return errorMatrixList;
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
        return uncertainData.getUncertaintyEnvelop(AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get());
    }

    public boolean hasNonSysErr() {
        return getNonSysErrStd() != null;
    }

    public boolean hasSysErr() {
        return getSysErrStd() != null && getSysErrInd() != null;
    }

    public boolean hasStageErrMatrix() {
        return uncertainData != null;
    }

    public PlotLine getPlotLine() {
        PlotLine plotLine = new PlotLine(
                name,
                dateTimeMillis,
                getStage(),
                AppSetup.COLORS.PLOT_LINE,
                new BasicStroke(2));
        return plotLine;
    }

    public PlotBand getPlotEnv() {
        List<double[]> errEnv = getStageErrUncertaintyEnvelop();
        PlotBand plotBand = new PlotBand(
                name,
                dateTimeMillis,
                errEnv.get(0),
                errEnv.get(1),
                false,
                AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);
        return plotBand;
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
}
