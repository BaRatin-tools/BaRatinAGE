package org.baratinage.ui.baratin.gaugings;

import java.util.List;

import org.baratinage.ui.component.ImportedDataset;

public class GaugingsDataset extends ImportedDataset {

    public GaugingsDataset() {
        super();
    }

    public GaugingsDataset(String name, List<double[]> data) {
        // WARNINGS:
        // - data must have three columns! Stage, discharge and discharge uncertainty!
        // - uncertainty is specified in percent and represents extended (+/-)
        // uncertainty

        int nRow = data.get(0).length;
        double[] gaugingsActiveState = new double[nRow];
        for (int k = 0; k < nRow; k++) {
            gaugingsActiveState[k] = 1;
        }
        data.add(gaugingsActiveState);
        setData(data, new String[] { "h", "Q", "uQ_percent", "active" });
        setDatasetName(name);
    }

    public double[] getStageValues() {
        return data.get(0);
    }

    public double[] getDischargeValues() {
        return data.get(1);
    }

    public double[] getDischargePercentUncertainty() {
        return data.get(2);
    }

    public double[] getDischargeStdUncertainty() {
        double[] u = new double[nRow];
        double[] q = getDischargeValues();
        double[] uqp = getDischargePercentUncertainty();
        for (int k = 0; k < nRow; k++) {
            u[k] = q[k] * uqp[k] / 100 / 2;
        }
        return u;
    }

    public double[] getActiveStateAsDouble() {
        return data.get(3);
    }

    public boolean[] getActiveStateAsBoolean() {
        double[] activeStateAsDouble = getActiveStateAsDouble();
        boolean[] activeStateAsBoolean = new boolean[activeStateAsDouble.length];
        for (int k = 0; k < nRow; k++) {
            activeStateAsBoolean[k] = activeStateAsDouble[k] == 1.0;
        }
        return activeStateAsBoolean;
    }

    public Gauging getGauging(int gaugingIndex) {
        double[] row = getRow(gaugingIndex);
        return new Gauging(
                row[0],
                row[1],
                row[2],
                row[3] == 1.0);
    }

    public void setGauging(int gaugingIndex, Gauging newGauging) {
        data.get(0)[gaugingIndex] = newGauging.stage;
        data.get(1)[gaugingIndex] = newGauging.discharge;
        data.get(2)[gaugingIndex] = newGauging.dischargeUncertainty;
        data.get(3)[gaugingIndex] = newGauging.activeState ? 1 : 0;
    }

}
