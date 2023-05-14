package org.baratinage.ui.baratin.gaugings;

import java.util.List;

import org.baratinage.ui.component.ImportedDataset;

import org.jfree.data.xy.XYIntervalSeries;

public class GaugingsDataset extends ImportedDataset {

    boolean[] gaugingsActiveState;

    public GaugingsDataset(String name, List<double[]> data, String[] headers) {
        super(name, data, headers);
        // WARNING: data must have three columns! Stage, discharge and discharge
        // uncertainty!
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

    public double[] getDischargeUncertaintyValues() {
        return data.get(2);
    }

    public boolean[] getActiveState() {
        return gaugingsActiveState;
    }

    // public double[] getDischargeLowValues() {
    // double[] v = new double[nRow];
    // for (int k = 0; k < nRow; k++) {
    // v[k] = data.get(1)[k] * (1 + data.get(2)[k] / 100);
    // }
    // return v;
    // }

    // public double[] getDischargeHighValues() {
    // double[] v = new double[nRow];
    // for (int k = 0; k < nRow; k++) {
    // v[k] = data.get(1)[k] * (1 - data.get(2)[k] / 100);
    // }
    // return v;
    // }

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
