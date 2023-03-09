package jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jbam.utils.ConfigFile;
import jbam.utils.Write;

public class CalibrationData {
    private String name;
    private UncertainData[] inputs;
    private UncertainData[] outputs;

    public CalibrationData(String name, UncertainData[] inputs, UncertainData[] outputs) {
        this.name = name;
        // FIXME: should check that inputs and outputs have the same number of
        // FIXME: elements/rows
        this.inputs = inputs;
        this.outputs = outputs;

    }

    public UncertainData[] getInputs() {
        return this.inputs;
    }

    public UncertainData[] getOutputs() {
        return this.outputs;
    }

    private class UncertainDataConfig {
        public int nCol;
        public int nRow;
        public int[] X;
        public int[] Xu;
        public int[] Xb;
        public int[] Xbi;
    }

    // FIXME: better implementation? it feels like this should be a static method...
    // However it cannot be because of the UncertainDataConfig class
    private UncertainDataConfig getUncertainDataConfig(UncertainData[] data) {
        UncertainDataConfig uDataConfig = new UncertainDataConfig();
        uDataConfig.nCol = 0;
        uDataConfig.nRow = data[0].getNumberOfValues(); // FIXME should check data consistency
        uDataConfig.X = new int[data.length];
        uDataConfig.Xu = new int[data.length];
        uDataConfig.Xb = new int[data.length];
        uDataConfig.Xbi = new int[data.length];
        for (int k = 0; k < data.length; k++) {
            uDataConfig.X[k] = uDataConfig.nCol + 1;
            uDataConfig.nCol++;
            if (data[k].hasNonSysError()) {
                uDataConfig.Xu[k] = uDataConfig.nCol + 1;
                uDataConfig.nCol++;
            } else {
                uDataConfig.Xu[k] = 0;
            }
            if (data[k].hasSysError()) {
                uDataConfig.Xb[k] = uDataConfig.nCol + 1;
                uDataConfig.nCol++;
                uDataConfig.Xbi[k] = uDataConfig.nCol + 1;
                uDataConfig.nCol++;
            } else {
                uDataConfig.Xb[k] = 0;
                uDataConfig.Xbi[k] = 0;
            }

        }
        return uDataConfig;
    }

    public String getDataFilePath(String workspace) {
        String dataFileName = String.format(ConfigFile.DATA_CALIBRATION, this.name);
        String dataFilePath = Path.of(workspace, dataFileName).toAbsolutePath().toString();
        return dataFilePath;
    }

    public void toDataFile(String workspace) {

        List<double[]> dataColumns = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        for (UncertainData i : this.inputs) {
            dataColumns.add(i.getValues());
            headers.add("X_" + i.getName());
            if (i.hasNonSysError()) {
                dataColumns.add(i.getNonSysStd());
                headers.add("Xu_" + i.getName());
            }
            if (i.hasSysError()) {
                dataColumns.add(i.getSysStd());
                headers.add("Xb_" + i.getName());
                dataColumns.add(i.getSysIndicesAsDouble());
                headers.add("Xbi_" + i.getName());
            }
        }

        for (UncertainData o : this.outputs) {
            dataColumns.add(o.getValues());
            headers.add("X_" + o.getName());
            if (o.hasNonSysError()) {
                dataColumns.add(o.getNonSysStd());
                headers.add("Xu_" + o.getName());
            }
            if (o.hasSysError()) {
                dataColumns.add(o.getSysStd());
                headers.add("Xb_" + o.getName());
                dataColumns.add(o.getSysIndicesAsDouble());
                headers.add("Xbi_" + o.getName());
            }
        }

        String dataFilePath = this.getDataFilePath(workspace);
        try {
            Write.writeMatrix(
                    dataFilePath,
                    dataColumns,
                    " ",
                    "-9999",
                    headers.toArray(new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toConfigFile(String workspace) {

        UncertainDataConfig inputsDataConfig = this.getUncertainDataConfig(this.inputs);
        UncertainDataConfig outputsDataConfig = this.getUncertainDataConfig(this.outputs);

        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.getDataFilePath(workspace), "Absolute path to data file", true);
        configFile.addItem(1, "number of header lines");
        configFile.addItem(inputsDataConfig.nRow, "Nobs, number of rows in data file (excluding header lines)");
        configFile.addItem(inputsDataConfig.nCol + outputsDataConfig.nCol, "number of columns in the data file");
        configFile.addItem(inputsDataConfig.X,
                "columns for X (observed inputs) in data file - comma-separated if several");
        configFile.addItem(inputsDataConfig.Xu,
                "columns for Xu (random uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(inputsDataConfig.Xb,
                "columns for Xb (systematic uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(inputsDataConfig.Xbi,
                "columns for Xb_indx (index of systematic errors in X - use 0 for a no-error assumption)");
        configFile.addItem(outputsDataConfig.X,
                "columns for Y (observed outputs) in data file - comma-separated if several");
        configFile.addItem(outputsDataConfig.Xu,
                "columns for Yu (uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(outputsDataConfig.Xb,
                "columns for Yb (systematic uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(outputsDataConfig.Xbi,
                "columns for Yb_indx (index of systematic errors in Y - use 0 for a no-error assumption)");

        String configFileName = ConfigFile.CONFIG_CALIBRATION;
        configFile.writeToFile(workspace, configFileName);
    }

    @Override
    public String toString() {

        List<String> str = new ArrayList<>();

        str.add(String.format("CalibrationData '%s' with:", this.name));
        str.add(" Inputs: ");
        for (UncertainData input : inputs) {
            str.add(input.toString());
        }
        str.add(" Outputs: ");
        for (UncertainData output : outputs) {
            str.add(output.toString());
        }

        return String.join("\n", str);
    }
}
