package bam;

import bam.exe.ConfigFile;

public class CalDataResidualConfig {
    private String outputFileName;

    public CalDataResidualConfig(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public CalDataResidualConfig() {
        this(ConfigFile.RESULTS_RESIDUALS);
    }

    public void writeConfig(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_RESIDUALS);
    }

    public void log() {
        System.out.print("Config - CalDataResidualConfig: ");
        System.out.print(String.format("%s", this.outputFileName));
        System.out.print(".\n");
    }
}
