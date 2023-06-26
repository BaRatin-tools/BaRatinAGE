package org.baratinage.ui.baratin.gaugings;

import java.io.IOException;
import java.util.List;

import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;

public class GaugingsDataset extends ImportedDataset {

    // boolean[] gaugingsActiveState;

    public GaugingsDataset() {
        super();
    }

    public GaugingsDataset(String name, List<double[]> data) {
        // super(name, data, headers);
        // super(name, data, headers);
        // WARNINGS:
        // - data must have three columns! Stage, discharge and discharge uncertainty!
        // - uncertainty is specified in percent and represents extended (+/-)
        // uncertainty

        double[] gaugingsActiveState = new double[nRow];
        for (int k = 0; k < nRow; k++) {
            gaugingsActiveState[k] = 1;
        }
        data.add(gaugingsActiveState);
        // String[] fullHeaders = new String[headers.length];
        // for (int k = 0; k < data.size() - 1; k++) {
        // fullHeaders[k] = headers[k];
        // }
        // fullHeaders[3] = "active";

        setData(data, new String[] { "h", "Q", "uQ_percent", "active" });
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

    public void writeDataFile(String dataFilePath) {
        List<double[]> dataToSave = getData();

        try {
            WriteFile.writeMatrix(
                    dataFilePath,
                    dataToSave,
                    ";",
                    "NA",
                    new String[] { "h", "Q", "uQ_percent", "active" }); // unused on load, just to make it clear when
                                                                        // opening
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

            // setData(dataToLoad.subList(0, 3), new String[] { "h", "Q", "uQ" });
            super.setData(dataToLoad, new String[] { "h", "Q", "uQ_percent", "active" });
            // setActiveStateFromDouble(dataToLoad.get(3));

        } catch (IOException e2) {
            System.out.println("Failed to read data file ...(" + getName() + ")");
            e2.printStackTrace();
        }
    }

}
