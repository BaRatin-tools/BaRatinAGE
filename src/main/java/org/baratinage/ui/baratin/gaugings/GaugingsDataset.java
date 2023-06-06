package org.baratinage.ui.baratin.gaugings;

import java.util.List;

import org.baratinage.ui.component.ImportedDataset;

public class GaugingsDataset extends ImportedDataset {

    boolean[] gaugingsActiveState;

    public GaugingsDataset(String name, List<double[]> data, String[] headers) {
        super(name, data, headers);
        // WARNINGS:
        // - data must have three columns! Stage, discharge and discharge uncertainty!
        // - uncertainty is specified in percent and represents extended (+/-)
        // uncertainty
        gaugingsActiveState = new boolean[nRow];
        for (int k = 0; k < nRow; k++) {
            gaugingsActiveState[k] = true;
        }
    }

    public double[] getStageValues() {
        return data.get(0);
    }

    public double[] getDischargeValues() {
        return data.get(1);
    }

    // FIXME: change name to a more appropriate one
    public double[] getDischargePercentUncertainty() {
        return data.get(2);
    }

    // FIXME: change name to a more appropriate one
    public double[] getDischargeStdUncertainty() {
        double[] u = new double[nRow];
        double[] q = getDischargeValues();
        double[] uqp = getDischargePercentUncertainty();
        for (int k = 0; k < nRow; k++) {
            u[k] = q[k] * uqp[k] / 100 / 2;
        }
        return u;
    }

    public boolean[] getActiveState() {
        return gaugingsActiveState;
    }

    public Gauging getGauging(int gaugingIndex) {
        double[] row = getRow(gaugingIndex);
        return new Gauging(
                row[0],
                row[1],
                row[2],
                gaugingsActiveState[gaugingIndex]);
    }

    public void setGauging(int gaugingIndex, Gauging newGauging) {
        data.get(0)[gaugingIndex] = newGauging.stage;
        data.get(1)[gaugingIndex] = newGauging.discharge;
        data.get(2)[gaugingIndex] = newGauging.dischargeUncertainty;
        gaugingsActiveState[gaugingIndex] = newGauging.activeState;
    }

}
