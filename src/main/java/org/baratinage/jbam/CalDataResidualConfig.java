package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class CalDataResidualConfig {
    public final String fileName;
    public final String outputFileName;

    public CalDataResidualConfig(String fileName, String outputFileName) {
        this.fileName = fileName;
        this.outputFileName = outputFileName;
    }

    public CalDataResidualConfig() {
        this(
                BamFilesHelpers.CONFIG_RESIDUALS,
                BamFilesHelpers.RESULTS_RESIDUALS);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, fileName);
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
        return new CalDataResidualConfig(calDataResidualConfigFileName, outputFileName);
    }
}
