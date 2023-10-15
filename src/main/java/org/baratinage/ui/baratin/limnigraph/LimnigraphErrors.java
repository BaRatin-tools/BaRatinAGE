package org.baratinage.ui.baratin.limnigraph;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.translation.T;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;

public class LimnigraphErrors extends RowColPanel {

    private LimnigraphDataset dataset;

    private final DataTable errConfigTable;
    private final DataTable errMatrixTable;

    public LimnigraphErrors() {
        super(AXIS.COL);

        errConfigTable = new DataTable();
        errMatrixTable = new DataTable();

        TabContainer tableTabs = new TabContainer(TabContainer.SIDE.LEFT);
        tableTabs.addTab("Error configuration", errConfigTable);
        tableTabs.addTab("Error matrix", errMatrixTable);

        T.updateHierarchy(this, errConfigTable);
        T.updateHierarchy(this, errMatrixTable);

        appendChild(tableTabs, 1);
    }

    public void updateDataset(LimnigraphDataset dataset) {

        this.dataset = dataset;

        T.clear(errConfigTable);
        errConfigTable.clearColumns();

        if (dataset.hasNonSysErr() || dataset.hasSysErr()) {
            errConfigTable.addColumn(dataset.getDateTime());
            errConfigTable.addColumn(dataset.getStage());

            if (dataset.hasNonSysErr()) {
                errConfigTable.addColumn(dataset.getNonSysErrStd());
            }

            if (dataset.hasSysErr()) {
                errConfigTable.addColumn(dataset.getSysErrStd());
                errConfigTable.addColumn(dataset.getSysErrInd());
            }

            errConfigTable.updateData();

            T.t(errConfigTable, () -> {
                errConfigTable.setHeaderWidth(0, 150);
                errConfigTable.setHeader(0, T.text("date_time"));
                errConfigTable.setHeader(1, T.text("stage_level"));
                int colIndex = 2;
                if (dataset.hasNonSysErr()) {
                    errConfigTable.setHeader(colIndex, T.text("stage_non_sys_error_std"));
                    colIndex++;
                }
                if (dataset.hasSysErr()) {
                    errConfigTable.setHeader(colIndex, T.text("stage_sys_error_std"));
                    colIndex++;
                    errConfigTable.setHeader(colIndex, T.text("stage_sys_error_ind"));
                    colIndex++;
                }
                errConfigTable.updateHeader();
            });

            if (!dataset.hasStageErrMatrix()) {
                computeStageErrorMatrix(dataset, 200);
            }
            updateErrMatrixTable();

        }

    }

    private void updateErrMatrixTable() {
        errMatrixTable.clearColumns();
        if (dataset.hasStageErrMatrix()) {
            List<double[]> matrix = dataset.getStageErrMatrix();
            for (int k = 0; k < matrix.size(); k++) {
                errMatrixTable.addColumn(matrix.get(k));
            }
            errMatrixTable.updateData();
            for (int k = 0; k < matrix.size(); k++) {
                errMatrixTable.setHeader(k, "#" + k);
            }
            errMatrixTable.updateHeader();
        }
    }

    public static void computeStageErrorMatrix(LimnigraphDataset dataset, int nCol) {
        int nRow = dataset.getNumberOfRows();
        double[] stage = dataset.getStage();
        // allocating memory
        List<double[]> matrix = new ArrayList<>(nCol);
        try {
            for (int i = 0; i < nCol; i++) {
                double[] column = new double[nRow];
                for (int j = 0; j < nRow; j++) {
                    column[j] = stage[j];
                }
                matrix.add(column);
            }
        } catch (OutOfMemoryError E) {
            System.err.println("Limnigraph Error: cannot create error matrix because memory is insufficient.");
            return;
        }
        if (dataset.hasNonSysErr()) {
            List<double[]> errorMatrix = getErrorMatrix(dataset.getNonSysErrStd(), nCol);
            addToMatrix(matrix, errorMatrix);
        }

        if (dataset.hasSysErr()) {
            double[] sysErrStd = dataset.getSysErrStd();
            int[] sysErrInd = dataset.getSysErrInd();

            HashMap<Integer, double[]> errors = new HashMap<>();

            for (int k = 0; k < nRow; k++) {
                Integer index = sysErrInd[k];
                if (!errors.containsKey(index)) {
                    errors.put(index, getErrors(sysErrStd[k], nCol));
                }
            }

            for (int j = 0; j < nRow; j++) {
                Integer index = sysErrInd[j];
                double[] err = errors.get(index);
                if (err != null && err.length == nCol) {
                    for (int i = 0; i < nCol; i++) {
                        matrix.get(i)[j] = matrix.get(i)[j] + err[i];
                    }
                } else {
                    System.err.println("Limnigraph Error: generated error vector for systematic error index " +
                            index + " is invalid.");
                }
            }

        }

        dataset.addErrorMatrix(matrix);

    }

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

    private static List<double[]> getErrorMatrix(double[] std, int nCol) {

        int nRow = std.length;

        List<double[]> matrix = new ArrayList<>(nCol);

        try {
            for (int i = 0; i < nCol; i++) {
                double[] column = new double[nRow];
                matrix.add(column);
            }

        } catch (OutOfMemoryError E) {
            System.err.println("Limnigraph Error: cannot create error matrix because memory is insufficient.");
            return null;
        }

        HashMap<Double, List<Integer>> rowIndicesPerStd = new HashMap<>();

        for (int k = 0; k < nRow; k++) {
            Double s = std[k];
            if (rowIndicesPerStd.containsKey(s)) {
                List<Integer> rowIndices = rowIndicesPerStd.get(s);
                rowIndices.add(k);
                rowIndicesPerStd.put(s, rowIndices);
            } else {
                rowIndicesPerStd.put(s, new ArrayList<>());
            }
        }

        for (Double s : rowIndicesPerStd.keySet()) {
            List<Integer> rowIndices = rowIndicesPerStd.get(s);

            int m = rowIndices.size();
            int n = m * nCol;

            double[] errors = getErrors(s, n);

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < nCol; j++) {
                    int rowIndex = rowIndices.get(i);
                    matrix.get(j)[rowIndex] = errors[i * nCol + j];
                }
            }
        }

        return matrix;

    }

    private static void addToMatrix(List<double[]> matrixToMutate, List<double[]> matrixToAdd) {
        int nCol = matrixToMutate.size();
        if (nCol != matrixToAdd.size()) {
            System.err.println("Limnigraph Error: matrixToMutate and matrixToAdd have different sizes!");
            return;
        }
        if (nCol < 1) {
            return;
        }
        int nRow = matrixToMutate.get(0).length;

        for (int i = 0; i < nCol; i++) {
            if (matrixToMutate.get(i).length != nRow) {
                System.err.println("Limnigraph Error: column " +
                        i + " of matrixToMutate doesn't have the expected " +
                        nRow + " number of elements!");
                return;
            }
            if (matrixToAdd.get(i).length != nRow) {
                System.err.println("Limnigraph Error: column " +
                        i + " of matrixToAdd doesn't have the expected " +
                        nRow + " number of elements!");
                return;
            }
            for (int j = 0; j < nRow; j++) {
                matrixToMutate.get(i)[j] = matrixToMutate.get(i)[j] + matrixToAdd.get(i)[j];
            }

        }
    }

}
