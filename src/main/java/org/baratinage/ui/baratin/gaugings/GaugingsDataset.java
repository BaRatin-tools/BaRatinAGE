package org.baratinage.ui.baratin.gaugings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;

public class GaugingsDataset extends ImportedDataset {

    boolean[] gaugingsActiveState;

    public GaugingsDataset() {
        super();
    }

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

    private double[] getActiveStateAsDouble() {
        double[] activeStateAsDouble = new double[nRow];
        for (int k = 0; k < nRow; k++) {
            activeStateAsDouble[k] = gaugingsActiveState[k] ? 1 : 0;
        }
        return activeStateAsDouble;
    }

    private void setActiveStateFromDouble(double[] activeStateAsDouble) {
        gaugingsActiveState = new boolean[nRow];
        for (int k = 0; k < nRow; k++) {
            gaugingsActiveState[k] = activeStateAsDouble[k] == 1;
        }
    }

    public void writeDataFile(String dataFilePath) {
        List<double[]> dataToSave = new ArrayList<>();
        for (double[] column : data) {
            dataToSave.add(column);
        }
        dataToSave.add(getActiveStateAsDouble());
        try {
            WriteFile.writeMatrix(
                    dataFilePath,
                    dataToSave,
                    ";",
                    "NA",
                    new String[] { "h", "Q", "uQ", "active" }); // unused on load, just to make it clear when opening
                                                                // file outside of BaRatinage
        } catch (IOException e) {
            System.err.println("Failed to write data to file... (" + getName() + ")");
            e.printStackTrace();
        }
    }

    public void setDataFromFile(String dataFilePath) {
        try {
            List<double[]> dataToLoad = ReadFile.readMatrix(
                    dataFilePath,
                    ";",
                    1,
                    Integer.MAX_VALUE,
                    "NA",
                    false,
                    false);

            setData(dataToLoad.subList(0, 3), new String[] { "h", "Q", "uQ" });
            setActiveStateFromDouble(dataToLoad.get(3));

        } catch (IOException e2) {
            System.out.println("Failed to read data file ...(" + getName() + ")");
            e2.printStackTrace();
        }
    }

}
