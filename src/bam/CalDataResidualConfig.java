package bam;

import bam.utils.ConfigFile;

public class CalDataResidualConfig {
    private String outputFileName;

    public CalDataResidualConfig(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public CalDataResidualConfig() {
        this(ConfigFile.RESULTS_RESIDUALS);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_RESIDUALS);
    }

    public String toString() {
        String str = "Config - CalDataResidualConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }
}
