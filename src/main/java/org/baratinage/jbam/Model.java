package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.ConfigFile;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;

public class Model {
    public final String fileName;
    public final String modelId;
    public final int nInput;
    public final int nOutput;
    public final AbstractParameter[] parameters;
    public final String xTra;
    public final String xTraFileName;

    public Model(
            String fileName,
            String modelId,
            int nInput,
            int nOutput,
            AbstractParameter[] parameters,
            String xTra,
            String xTraFileName) {
        this.fileName = fileName;
        this.modelId = modelId;
        this.nInput = nInput;
        this.nOutput = nOutput;
        this.parameters = parameters;
        this.xTra = xTra;
        this.xTraFileName = xTraFileName;

        for (AbstractParameter p : parameters) {
            if (p.name.equals("LogPost")) {
                throw new IllegalArgumentException("No parameters can be named LogPost, it is reserved by BaM!");
            }
        }
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(modelId, "Model ID", true);
        configFile.addItem(nInput, "nX: number of input variables");
        configFile.addItem(nOutput, "nY: number of output variables");
        configFile.addItem(parameters.length, "nPar: number of parameters theta");

        for (AbstractParameter p : parameters) {
            p.toFile(configFile, workspace);
        }

        configFile.writeToFile(workspace, fileName);

        try {
            WriteFile.writeLines(Path.of(workspace, xTraFileName),
                    new String[] { xTra });
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    @Override
    public String toString() {

        List<String> str = new ArrayList<>();

        str.add(String.format("Model '%s' with %d inputs and %d outputs and the following parameters:",
                modelId, nInput, nOutput));
        for (AbstractParameter p : parameters) {
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

        AbstractParameter[] parameters = new AbstractParameter[nPar];
        for (int k = 0; k < nPar; k++) {

            String distribName = configFile.getString(3 + k * 4 + 3);

            if (distribName.equals("VAR")) {
                throw new UnsupportedOperationException("'VAR' parameter are not supported");
            } else {
                DistributionType distType = DistributionType.getDistribFromBamName(distribName);
                if (distType == null) {
                    throw new UnsupportedOperationException("Unknown '%s' distribution".formatted(distribName));
                }
                double[] distribParams = configFile.getDoubleArray(3 + k * 4 + 4);
                Distribution distribution = new Distribution(
                        DistributionType.getDistribFromBamName(distribName),
                        distribParams);
                parameters[k] = new Parameter(
                        configFile.getString(3 + k * 4 + 1),
                        configFile.getDouble(3 + k * 4 + 2),
                        distribution);
            }
        }

        String xTraString;
        try {
            xTraString = Files.readString(Path.of(workspace, xTraFileName));
            xTraString = xTraString.trim();
        } catch (IOException e) {
            ConsoleLogger.error("Model Error: An error occured while reading xTra file'" + xTraFileName + "'!\n" + e);
            return null;
        }

        return new Model(modelConfigFileName, modelId, nX, nY, parameters, xTraString, xTraFileName);
    }
}
