package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.jbam.utils.Write;

public class Model {
    private String modelId;
    private int nInput;
    private int nOutput;
    private Parameter[] parameters;
    private String xTra;

    public Model(String modelId, int nInput, int nOutput, Parameter[] parameters, String xTra) {
        this.modelId = modelId;
        this.nInput = nInput;
        this.nOutput = nOutput;
        this.parameters = parameters;
        this.xTra = xTra;
    }

    public Parameter[] getParameters() {
        return this.parameters;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.modelId, "Model ID", true);
        configFile.addItem(this.nInput, "nX: number of input variables");
        configFile.addItem(this.nOutput, "nY: number of output variables");
        configFile.addItem(this.parameters.length, "nPar: number of parameters theta");

        for (Parameter p : this.parameters) {
            configFile.addItem(p.getName(), "Parameter name -----", true);
            configFile.addItem(p.getInitalGuess(), "Initial guess");
            Distribution d = p.getDistribution();
            configFile.addItem(d.getName(), "Prior distribution", true);
            configFile.addItem(d.getParameterValues(), "Prior parameters");
        }

        configFile.writeToFile(workspace, ConfigFile.CONFIG_MODEL);

        try {
            Write.writeLines(Path.of(workspace, ConfigFile.CONFIG_XTRA),
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
}
