package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class CalDataResidualConfig {
    private String outputFileName;

    public CalDataResidualConfig(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public CalDataResidualConfig() {
        this(BamFilesHelpers.RESULTS_RESIDUALS);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, BamFilesHelpers.CONFIG_RESIDUALS);
    }

    @Override
    public String toString() {
        String str = "Config - CalDataResidualConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }

    static public CalDataResidualConfig readCalDataResidualConfig(String workspace,
            String calDataResidualConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, calDataResidualConfigFileName);
        String outputFileName = configFile.getString(0);
        return new CalDataResidualConfig(outputFileName);
    }
}
