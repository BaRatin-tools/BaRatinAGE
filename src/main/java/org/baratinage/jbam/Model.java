package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.jbam.utils.Write;

public class Model {
    public final String fileName;
    public final String modelId;
    public final int nInput;
    public final int nOutput;
    public final Parameter[] parameters;
    public final String xTra;
    public final String xTraFileName;

    public Model(
            String fileName,
            String modelId,
            int nInput,
            int nOutput,
            Parameter[] parameters,
            String xTra,
            String xTraFileName) {
        this.fileName = fileName;
        this.modelId = modelId;
        this.nInput = nInput;
        this.nOutput = nOutput;
        this.parameters = parameters;
        this.xTra = xTra;
        this.xTraFileName = xTraFileName;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(modelId, "Model ID", true);
        configFile.addItem(nInput, "nX: number of input variables");
        configFile.addItem(nOutput, "nY: number of output variables");
        configFile.addItem(parameters.length, "nPar: number of parameters theta");

        for (Parameter p : parameters) {
            configFile.addItem(p.getName(), "Parameter name -----", true);
            configFile.addItem(p.getInitialGuess(), "Initial guess");
            Distribution d = p.getDistribution();
            configFile.addItem(d.getName(), "Prior distribution", true);
            configFile.addItem(d.getParameterValues(), "Prior parameters");
        }

        configFile.writeToFile(workspace, fileName);

        try {
            Write.writeLines(Path.of(workspace, xTraFileName),
                    new String[] { xTra });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {

        List<String> str = new ArrayList<>();

        str.add(String.format("Model '%s' with %d inputs and %d outputs and the following parameters:",
                modelId, nInput, nOutput));
        for (Parameter p : parameters) {
            str.add(p.toString());
        }
        str.add("\nxTra content associated with the model is:");
        str.add("--- xTra content start ---");
        str.add(xTra);
        str.add("--- xTra content end ---");

        return String.join("\n", str);
    }

    public static Model readModel(String workspace, String modelConfigFileName, String xTraFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, modelConfigFileName);

        String modelId = configFile.getString(0);
        int nX = configFile.getInt(1);
        int nY = configFile.getInt(2);
        int nPar = configFile.getInt(3);

        Parameter[] parameters = new Parameter[nPar];
        for (int k = 0; k < nPar; k++) {
            String distribName = configFile.getString(3 + k * 4 + 3);
            double[] distribParams = configFile.getDoubleArray(3 + k * 4 + 4);
            Distribution distribution = new Distribution(
                    DISTRIB.getDistribFromName(distribName),
                    distribParams);
            parameters[k] = new Parameter(
                    configFile.getString(3 + k * 4 + 1),
                    configFile.getDouble(3 + k * 4 + 2),
                    distribution);
        }

        String xTraString;
        try {
            xTraString = Files.readString(Path.of(workspace, xTraFileName));
        } catch (IOException e) {
            System.err.println("Model Error: An error occured while reading xTra file'" + xTraFileName + "'!");
            e.printStackTrace();
            return null;
        }

        return new Model(modelConfigFileName, modelId, nX, nY, parameters, xTraString, xTraFileName);
    }
}
