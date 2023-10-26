package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.Calc;

public class UncertaintyDataset extends AbstractDataset {

    private static NamedColumn[] toNamedColumnArray(List<double[]> matrix) {
        int n = matrix.size();
        NamedColumn[] namedColumns = new NamedColumn[n];
        for (int k = 0; k < n; k++) {
            namedColumns[k] = new NamedColumn("" + k, matrix.get(k));
        }
        return namedColumns;
    }

    private static String[] toHeaders(int n) {
        String[] headers = new String[n];
        for (int k = 0; k < n; k++) {
            headers[k] = "" + k;
        }
        return headers;
    }

    private final List<double[]> uncertaintyEnvelop;

    public UncertaintyDataset(String name, List<double[]> matrix) {
        super(name, toNamedColumnArray(matrix));
        uncertaintyEnvelop = computeUncertaintyEnvelop(0.025, 0.975);
    }

    public UncertaintyDataset(String name, String hashString, int nCol) {
        super(name, hashString, toHeaders(nCol));
        uncertaintyEnvelop = computeUncertaintyEnvelop(0.025, 0.975);
    }

    public double[] getColumn(int index) {
        return getColumn("" + index);
    }

    public double getValue(int colIndex, int rowIndex) {
        return getValue("" + colIndex, rowIndex);
    }

    public List<double[]> getUncertaintyEnvelop() {
        return uncertaintyEnvelop;
    }

    private List<double[]> computeUncertaintyEnvelop(double pLow, double pHigh) {
        List<double[]> errorMatrix = getMatrix();
        int nCol = getNumberOfColumns();
        int nRow = getNumberOfRows();
        List<double[]> uncertaintyEnvelop = new ArrayList<>();
        double[] lower = new double[nRow];
        double[] upper = new double[nRow];
        for (int k = 0; k < nRow; k++) {
            double[] row = new double[nCol];
            for (int i = 0; i < nCol; i++) {
                row[i] = errorMatrix.get(i)[k];
            }
            double[] perc = Calc.percentiles(row, false, pLow, pHigh);

            lower[k] = perc[0];
            upper[k] = perc[1];
        }
        uncertaintyEnvelop.add(lower);
        uncertaintyEnvelop.add(upper);
        return uncertaintyEnvelop;
    }

}
