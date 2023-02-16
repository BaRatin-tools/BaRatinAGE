package bam;

import java.nio.file.Path;

import bam.exe.ConfigFile;

public class StructuralErrorModel {
    private String name;
    private String modelId;
    private Parameter[] parameters;

    public StructuralErrorModel(String name, String modelId, Parameter[] parameters) {
        this.name = name;
        this.modelId = modelId;
        this.parameters = parameters;
    }

    public String getConfigFileName() {
        String configFileName = String.format(ConfigFile.CONFIG_STRUCTURAL_ERRORS, this.name);
        return configFileName;
    }

    public void writeConfig(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.modelId, "Function f used in sdev=f(Qrc) ", true);
        configFile.addItem(this.parameters.length, "Number of parameters gamma for f");

        for (Parameter p : this.parameters) {
            configFile.addItem(p.getName(), "Parameter name -----", true);
            configFile.addItem(p.getInitalGuess(), "Initial guess");
            Distribution d = p.getDistribution();
            configFile.addItem(d.getName(), "Prior distribution", true);
            configFile.addItem(d.getParameterValues(), "Prior parameters");
        }

        String configFileName = this.getConfigFileName();
        configFile.writeToFile(Path.of(workspace, configFileName).toString());
    }

    public void log() {
        System.out.println(String.format("Structural Error Model %s of type '%s':", this.name, this.modelId));
        for (Parameter p : this.parameters) {
            p.log();
        }
    }
}
