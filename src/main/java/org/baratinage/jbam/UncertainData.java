package org.baratinage.jbam;

import org.baratinage.utils.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baratinage.utils.Calc;
import org.baratinage.utils.ConsoleLogger;

public class UncertainData {
    public final String name;
    public final double[] values;
    public final double[] nonSysStd;
    public final double[] sysStd;
    public final int[] sysIndices;

    private Boolean valuesHasMissingValues;
    private Boolean uncertaintyHasMissingValues;

    private Integer nReplicates = 200;
    private double[][] errorMatrix = null;
    private List<double[]> uncertaintyEnvelop = null;

    public UncertainData(String name, double[] values, double[] nonSysStd, double[] sysStd, int[] sysIndices) {
        int n = values.length;
        if (nonSysStd == null) {
            nonSysStd = new double[] {};
        }
        if (sysStd == null) {
            sysStd = new double[] {};
        }
        if (sysIndices == null) {
            sysIndices = new int[] {};
        }
        if (nonSysStd.length != n && nonSysStd.length != 0) {
            ConsoleLogger.error("nonSysStd is not the correct length, set to empty");
            nonSysStd = new double[] {};
        }
        if ((sysStd.length != n && sysStd.length != 0) ||
                (sysIndices.length != n && sysIndices.length != 0)) {
            ConsoleLogger.error("sysStd is not the correct length, set to empty");
            sysStd = new double[] {};
            sysIndices = new int[] {};
        }
        // FIXME should check data consistency

        this.name = name;
        this.values = values;
        this.nonSysStd = nonSysStd;
        this.sysStd = sysStd;
        this.sysIndices = sysIndices;
    }

    public UncertainData(String name, double[] values, double[] nonSysStd) {
        this(name, values, nonSysStd, new double[] {}, new int[] {});
    }

    public UncertainData(String name, double[] values) {
        this(name, values, new double[] {}, new double[] {}, new int[] {});
    }

    public int getNumberOfValues() {
        return this.values.length;
    }

    public double[] getSysIndicesAsDouble() {
        int n = this.sysIndices.length;
        double[] sysIndices = new double[n];
        for (int k = 0; k < n; k++) {
            sysIndices[k] = (double) this.sysIndices[k];
        }
        return sysIndices;
    }

    public boolean hasNonSysError() {
        return this.nonSysStd.length > 0;
    }

    public boolean hasSysError() {
        return this.sysStd.length > 0 && this.sysIndices.length > 0;
    }

    public boolean hasMissingValues() {
        return hasMissingValuesInValues() || hasMissingValuesInUncertainty();
    }

    public boolean hasMissingValuesInValues() {
        if (valuesHasMissingValues != null) {
            return valuesHasMissingValues;
        }
        valuesHasMissingValues = Misc.containsMissingValue(values);
        return valuesHasMissingValues;
    }

    public boolean hasMissingValuesInUncertainty() {
        if (uncertaintyHasMissingValues != null) {
            return uncertaintyHasMissingValues;
        }
        uncertaintyHasMissingValues = Misc.containsMissingValue(nonSysStd) || Misc.containsMissingValue(sysStd);
        return uncertaintyHasMissingValues;
    }

    public int length() {
        return this.values.length;
    }

    public int getNumberOfReplicates() {
        return this.nReplicates;
    }

    public List<double[]> getUncertaintyEnvelop(int nReplicates) {
        buildErrorMatrix(nReplicates);

        return uncertaintyEnvelop;
    }

    public double[][] getErrorMatrix(int nReplicates) {
        buildErrorMatrix(nReplicates);
        return errorMatrix;
    }

    private void buildErrorMatrix(int nReplicates) {
        if (errorMatrix != null && nReplicates == this.nReplicates) {
            return;
        }
        this.nReplicates = nReplicates;

        boolean hasNonSysErr = nonSysStd != null;
        boolean hasSysErr = sysStd != null && sysIndices != null;

        if (!hasNonSysErr && !hasSysErr) {
            errorMatrix = null;
            return;
        }

        int nRow = values.length;
        double[][] matrix = new double[nReplicates][nRow];
        try {
            for (int i = 0; i < nReplicates; i++) {
                // initialize columns
                for (int j = 0; j < nRow; j++) {
                    matrix[i][j] = values[j];
                }
            }
        } catch (OutOfMemoryError E) {
            ConsoleLogger.error("cannot create error matrix because memory is insufficient.");
            return;
        }

        if (hasNonSysErr) {
            addNonSysError(matrix, nonSysStd);
        }

        if (hasSysErr) {
            addSysError(matrix, sysStd, sysIndices);
        }

        errorMatrix = matrix;
        uncertaintyEnvelop = computeUncertaintyEnvelop(matrix, 0.025, 0.975);
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

    private static List<double[]> computeUncertaintyEnvelop(double[][] errorMatrix, double pLow, double pHigh) {
        if (errorMatrix.length == 0) {
            return null;
        }
        int nCol = errorMatrix.length;
        int nRow = errorMatrix[0].length;
        double[] lower = new double[nRow];
        double[] upper = new double[nRow];
        List<double[]> uncertaintyEnvelop = new ArrayList<>();
        for (int k = 0; k < nRow; k++) {
            double[] row = new double[nCol];
            for (int i = 0; i < nCol; i++) {
                row[i] = errorMatrix[i][k];
            }
            double[] perc = Calc.percentiles(row, false, pLow, pHigh);

            lower[k] = perc[0];
            upper[k] = perc[1];
        }
        uncertaintyEnvelop.add(lower);
        uncertaintyEnvelop.add(upper);
        return uncertaintyEnvelop;
    }

    @Override
    public String toString() {
        boolean hasSysError = this.sysStd.length > 0;
        boolean hasNonSysError = this.nonSysStd.length > 0 && this.sysIndices.length > 0;
        int n = this.values.length;
        String str = String.format("Data '%s' contains %d elements with ", this.name, n);
        if (hasNonSysError) {
            str += "non-systematic errors and ";
        } else {
            str += "NO non-systematic errors and ";
        }
        if (hasSysError) {
            str += "systematic errors.\n";
        } else {
            str += "NO systematic errors.";
        }
        return str;
    }

}
