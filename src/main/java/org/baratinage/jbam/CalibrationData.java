package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.jbam.utils.Read;
import org.baratinage.jbam.utils.Write;

public class CalibrationData {
    public final String name;
    public final String fileName;
    public final String dataFileName; // FIXME: should be dataFilePath instead?
    public final UncertainData[] inputs;
    public final UncertainData[] outputs;

    public CalibrationData(
            String name,
            String fileName,
            String dataFileName,
            UncertainData[] inputs,
            UncertainData[] outputs) {
        this.name = name;
        this.fileName = fileName;
        this.dataFileName = dataFileName;
        // FIXME: should check that inputs and outputs have the same number of
        // FIXME: elements/rows
        this.inputs = inputs;
        this.outputs = outputs;

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
    private UncertainDataConfig getUncertainDataConfig(UncertainData[] data, int columnOffset) {
        UncertainDataConfig uDataConfig = new UncertainDataConfig();
        uDataConfig.nCol = 0;
        uDataConfig.nRow = data[0].getNumberOfValues(); // FIXME should check data consistency
        // FIXME: 0 length should be forbidden, makes BaM crash
        uDataConfig.X = new int[data.length];
        uDataConfig.Xu = new int[data.length];
        uDataConfig.Xb = new int[data.length];
        uDataConfig.Xbi = new int[data.length];
        for (int k = 0; k < data.length; k++) {
            uDataConfig.X[k] = uDataConfig.nCol + 1 + columnOffset;
            uDataConfig.nCol++;
            if (data[k].hasNonSysError()) {
                uDataConfig.Xu[k] = uDataConfig.nCol + 1 + columnOffset;
                uDataConfig.nCol++;
            } else {
                uDataConfig.Xu[k] = 0;
            }
            if (data[k].hasSysError()) {
                uDataConfig.Xb[k] = uDataConfig.nCol + 1 + columnOffset;
                uDataConfig.nCol++;
                uDataConfig.Xbi[k] = uDataConfig.nCol + 1 + columnOffset;
                uDataConfig.nCol++;
            } else {
                uDataConfig.Xb[k] = 0;
                uDataConfig.Xbi[k] = 0;
            }

        }
        return uDataConfig;
    }

    public String toDataFile(String workspace) {

        // FIXME: writing data to workspace
        String dataFilePath = Path.of(workspace, dataFileName).toAbsolutePath().toString();

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
        return dataFilePath;
    }

    public void toConfigFile(String workspace, String dataFilePath) {

        UncertainDataConfig inputsDataConfig = this.getUncertainDataConfig(this.inputs, 0);
        UncertainDataConfig outputsDataConfig = this.getUncertainDataConfig(this.outputs, inputsDataConfig.nCol);

        ConfigFile configFile = new ConfigFile();
        configFile.addItem(dataFilePath, "Absolute path to data file", true);
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

        configFile.writeToFile(workspace, fileName);
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

    static public CalibrationData readCalibrationData(String workspace, String calibrationConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, calibrationConfigFileName);
        String rawPathToDataFile = configFile.getString(0);

        // Can data file be found? Absolutelty or relatively to workspace
        String dataFilePath = BamFilesHelpers.findDataFilePath(rawPathToDataFile, workspace);
        if (dataFilePath == null) {
            System.err.println("Cannot find calibration data file '" + rawPathToDataFile + "'!");
            return null;
        }
        String dataFileName = Path.of(dataFilePath).getFileName().toString();

        int nHeaderRows = configFile.getInt(1);
        int nRow = configFile.getInt(2);
        int nColumns = configFile.getInt(3);

        int[] xColumns = configFile.getIntArray(4);
        int[] xuColumns = configFile.getIntArray(5);
        int[] xbColumns = configFile.getIntArray(6);
        int[] xbindxColumns = configFile.getIntArray(7);

        int[] yColumns = configFile.getIntArray(8);
        int[] yuColumns = configFile.getIntArray(9);
        int[] ybColumns = configFile.getIntArray(10);
        int[] ybindxColumns = configFile.getIntArray(11);

        List<double[]> rawData;
        String[] headers;
        try {
            rawData = Read.readMatrix(
                    dataFilePath, nHeaderRows);
            headers = Read.readHeaders(dataFilePath);

        } catch (IOException e) {
            System.err.println("Failed to read data file '" + dataFilePath + "'!");
            e.printStackTrace();
            return null;
        }

        if (rawData.size() != nColumns) {
            System.err.println("Number of columns in data file '" + dataFilePath
                    + "' inconsistant with number of columns in config file!");
            return null;
        }

        if (rawData.get(0).length != nRow) {
            System.err.println("Number of rows in data file '" + dataFilePath
                    + "' inconsistant with number of rows in config file!");
            return null;
        }

        if (headers.length != nColumns) {
            System.err.println("Number of columns in data file '" + dataFilePath
                    + "' doesn't match number of headers!");
            return null;
        }

        int nInputs = xColumns.length;
        UncertainData[] inputs = new UncertainData[nInputs];
        for (int k = 0; k < nInputs; k++) {

            String inputName = headers[xColumns[k] - 1];
            double[] values = rawData.get(xColumns[k] - 1);

            double[] nonSysStd = new double[] {};
            double[] sysStd = new double[] {};
            int[] sysIndices = new int[] {};

            if (xuColumns[0] != 0) {
                nonSysStd = rawData.get(xuColumns[k] - 1);
            }
            if (xbColumns[0] != 0 && xbindxColumns[0] != 0) {
                sysStd = rawData.get(xbColumns[k] - 1);
                double[] sysIndicesAsDouble = rawData.get(xbindxColumns[k] - 1);
                sysIndices = new int[nRow];
                for (int i = 0; i < nRow; i++) {
                    sysIndices[i] = (int) sysIndicesAsDouble[i];
                }
            }
            inputs[k] = new UncertainData(inputName, values, nonSysStd, sysStd, sysIndices);
        }

        int nOutputs = yColumns.length;
        UncertainData[] outputs = new UncertainData[nOutputs];
        for (int k = 0; k < nOutputs; k++) {

            String outputName = headers[yColumns[k] - 1];
            double[] values = rawData.get(yColumns[k] - 1);

            double[] nonSysStd = new double[] {};
            double[] sysStd = new double[] {};
            int[] sysIndices = new int[] {};

            if (yuColumns[0] != 0) {
                nonSysStd = rawData.get(yuColumns[k] - 1);
            }
            if (ybColumns[0] != 0 && ybindxColumns[0] != 0) {
                sysStd = rawData.get(ybColumns[k] - 1);
                double[] sysIndicesAsDouble = rawData.get(ybindxColumns[k] - 1);
                sysIndices = new int[nRow];
                for (int i = 0; i < nRow; i++) {
                    sysIndices[i] = (int) sysIndicesAsDouble[i];
                }
            }
            outputs[k] = new UncertainData(outputName, values, nonSysStd, sysStd, sysIndices);
        }

        return new CalibrationData(dataFileName, calibrationConfigFileName, dataFileName, inputs, outputs);
    }

}
