package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.jbam.utils.Write;

public class Model {
    public final String modelId;
    public final int nInput;
    public final int nOutput;
    public final Parameter[] parameters;
    public final String xTra;

    public Model(String modelId, int nInput, int nOutput, Parameter[] parameters, String xTra) {
        this.modelId = modelId;
        this.nInput = nInput;
        this.nOutput = nOutput;
        this.parameters = parameters;
        this.xTra = xTra;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.modelId, "Model ID", true);
        configFile.addItem(this.nInput, "nX: number of input variables");
        configFile.addItem(this.nOutput, "nY: number of output variables");
        configFile.addItem(this.parameters.length, "nPar: number of parameters theta");

        for (Parameter p : this.parameters) {
            configFile.addItem(p.getName(), "Parameter name -----", true);
            configFile.addItem(p.getInitialGuess(), "Initial guess");
            Distribution d = p.getDistribution();
            configFile.addItem(d.getName(), "Prior distribution", true);
            configFile.addItem(d.getParameterValues(), "Prior parameters");
        }

        configFile.writeToFile(workspace, BamFilesHelpers.CONFIG_MODEL);

        try {
            Write.writeLines(Path.of(workspace, BamFilesHelpers.CONFIG_XTRA),
                    new String[] { this.xTra });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {

        List<String> str = new ArrayList<>();

        str.add(String.format("Model '%s' with %d inputs and %d outputs and the following parameters:",
                this.modelId, this.nInput, this.nOutput));
        for (Parameter p : this.parameters) {
            str.add(p.toString());
        }
        str.add("\nxTra content associated with the model is:");
        str.add("--- xTra content start ---");
        str.add(this.xTra);
        str.add("--- xTra content end ---");

        return String.join("\n", str);
    }

    public static Model readModel(String workspace, String modelConfigFileName) {
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

        return new Model(modelId, nX, nY, parameters, workspace);
    }
}
