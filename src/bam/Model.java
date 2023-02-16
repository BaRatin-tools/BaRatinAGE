package bam;

import java.io.IOException;
import java.nio.file.Path;

import bam.exe.ConfigFile;
import utils.FileReadWrite;

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

    public void writeConfig(String workspace) {
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
            FileReadWrite.writeLines(Path.of(workspace, ConfigFile.CONFIG_XTRA),
                    new String[] { this.xTra });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log() {
        System.out.println(
                String.format("Model '%s' with %d inputs and %d outputs and the following parameters:", this.modelId,
                        this.nInput, this.nOutput));
        for (Parameter p : this.parameters) {
            p.log();
        }
        System.out.println("--- xTra content start ---");
        System.out.println(this.xTra);
        System.out.println("--- xTra content end ---");

    }
}
