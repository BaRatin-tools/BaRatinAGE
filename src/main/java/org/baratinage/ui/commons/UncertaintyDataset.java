package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.Calc;

public class UncertaintyDataset extends AbstractDataset {

    private final List<double[]> uncertaintyEnvelop;

    public UncertaintyDataset(String name, double[][] matrix) {
        super(name, matrix);
        uncertaintyEnvelop = computeUncertaintyEnvelop(0.025, 0.975);
    }

    public UncertaintyDataset(String name, String hashString) {
        super(name, hashString);
        uncertaintyEnvelop = computeUncertaintyEnvelop(0.025, 0.975);
    }

    public double[] getColumn(int index) {
        return getColumn(index);
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
