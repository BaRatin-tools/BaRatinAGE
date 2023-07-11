package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFileNames;
import org.baratinage.jbam.utils.ConfigFile;

public class CalDataResidualConfig {
    private String outputFileName;

    public CalDataResidualConfig(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public CalDataResidualConfig() {
        this(BamFileNames.RESULTS_RESIDUALS);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, BamFileNames.CONFIG_RESIDUALS);
    }

    @Override
    public String toString() {
        String str = "Config - CalDataResidualConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }
}
