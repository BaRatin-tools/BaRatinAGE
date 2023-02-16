package bam;

import java.io.IOException;
import java.nio.file.Path;

import bam.exe.ConfigFile;
import utils.FileReadWrite;
import utils.Matrix;
import utils.Vector;

public class CalibrationData {
    private String name;
    private UncertainData[] inputs;
    private UncertainData[] outputs;

    public CalibrationData(String name, UncertainData[] inputs, UncertainData[] outputs) {
        this.name = name;
        // FIXME: should check that inputs and outputs have the same number of
        // elements/rows
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public void writeConfig(String workspace) {

        double[][] data = new double[0][0];
        String[] header = new String[0];
        int colIndex = 1;

        int[] X = new int[this.inputs.length];
        int[] Xu = new int[this.inputs.length];
        int[] Xb = new int[this.inputs.length];
        int[] Xbi = new int[this.inputs.length];

        for (int k = 0; k < this.inputs.length; k++) {
            data = Matrix.concat(data, this.inputs[k].getValues());
            header = Vector.push(header, this.inputs[k].getName());
            X[k] = colIndex;
            colIndex++;
            if (this.inputs[k].hasNonSysError()) {
                data = Matrix.concat(data, this.inputs[k].getNonSysStd());
                header = Vector.push(header, String.format("%s_u", this.inputs[k].getName()));
                Xu[k] = colIndex;

            } else {
                Xu[k] = 0;
            }
            if (this.inputs[k].hasSysError()) {
                data = Matrix.concat(data, this.inputs[k].getNonSysStd());
                header = Vector.push(header, String.format("%s_b", this.inputs[k].getName()));
                Xb[k] = colIndex;
                colIndex++;
                data = Matrix.concat(data, this.inputs[k].getSysIndicesAsDouble());
                header = Vector.push(header, String.format("%s_bi", this.inputs[k].getName()));
                Xbi[k] = colIndex;
                colIndex++;
            } else {
                Xb[k] = 0;
                Xbi[k] = 0;
            }

        }

        int[] Y = new int[this.outputs.length];
        int[] Yu = new int[this.outputs.length];
        int[] Yb = new int[this.outputs.length];
        int[] Ybi = new int[this.outputs.length];

        for (int k = 0; k < this.outputs.length; k++) {
            data = Matrix.concat(data, this.outputs[k].getValues());
            header = Vector.push(header, this.outputs[k].getName());
            Y[k] = colIndex;
            colIndex++;
            if (this.outputs[k].hasNonSysError()) {
                data = Matrix.concat(data, this.outputs[k].getNonSysStd());
                header = Vector.push(header, String.format("%s_u", this.outputs[k].getName()));
                Yu[k] = colIndex;
                colIndex++;

            } else {
                Yu[k] = 0;
            }
            if (this.outputs[k].hasSysError()) {
                data = Matrix.concat(data, this.outputs[k].getNonSysStd());
                header = Vector.push(header, String.format("%s_b", this.outputs[k].getName()));
                Yb[k] = colIndex;
                colIndex++;
                data = Matrix.concat(data, this.outputs[k].getSysIndicesAsDouble());
                header = Vector.push(header, String.format("%s_bi", this.outputs[k].getName()));
                Ybi[k] = colIndex;
                colIndex++;
            } else {
                Yb[k] = 0;
                Ybi[k] = 0;
            }

        }

        Matrix.prettyPrint(data);

        String dataFileName = String.format(ConfigFile.DATA_CALIBRATION, this.name);
        String dataFilePath = Path.of(workspace, dataFileName).toAbsolutePath().toString();
        // double[][] data
        int nCol = data.length;
        data = Matrix.transpose(data);
        int nRow = data.length;
        try {
            FileReadWrite.writeMatrix(
                    dataFilePath,
                    data,
                    " ",
                    "-9999",
                    header);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigFile configFile = new ConfigFile();
        configFile.addItem(dataFilePath, "Absolute path to data file", true);
        configFile.addItem(1, "number of header lines");
        configFile.addItem(nRow, "Nobs, number of rows in data file (excluding header lines)");
        configFile.addItem(nCol, "number of columns in the data file");
        configFile.addItem(X, "columns for X (observed inputs) in data file - comma-separated if several");
        configFile.addItem(Xu,
                "columns for Xu (random uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(Xb,
                "columns for Xb (systematic uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(Xbi,
                "columns for Xb_indx (index of systematic errors in X - use 0 for a no-error assumption)");
        configFile.addItem(Y, "columns for Y (observed outputs) in data file - comma-separated if several");
        configFile.addItem(Yu,
                "columns for Yu (uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(Yb,
                "columns for Yb (systematic uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
        configFile.addItem(Ybi,
                "columns for Yb_indx (index of systematic errors in Y - use 0 for a no-error assumption)");

        String configFileName = ConfigFile.CONFIG_CALIBRATION;
        configFile.writeToFile(workspace, configFileName);
    }

    public void log() {
        System.out.println(String.format("CalibrationData '%s' with:", this.name));
        System.out.println(" - Inputs: ");
        for (UncertainData input : inputs) {
            input.log();
        }
        System.out.println(" - Outputs: ");
        for (UncertainData output : outputs) {
            output.log();
        }
    }
}
