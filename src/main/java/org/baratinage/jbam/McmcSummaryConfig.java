package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcSummaryConfig {
    public final String fileName;
    public final String outputFileName;

    // FIXME: see
    // https://github.com/BaM-tools/BaM/commit/1c22189af4cd00d0e8267c2b95e22c38ac56a517
    public McmcSummaryConfig(String fileName, String outputFileName) {
        this.fileName = fileName;
        this.outputFileName = outputFileName;
    }

    public McmcSummaryConfig() {
        this(BamFilesHelpers.CONFIG_MCMC_SUMMARY, BamFilesHelpers.RESULTS_MCMC_SUMMARY);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(outputFileName, "Result file");
        configFile.writeToFile(workspace, fileName);
    }

    @Override
    public String toString() {
        String str = "Config - McmcSummaryConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }

    static public McmcSummaryConfig readMcmcSummaryConfig(String workspace, String mcmcSummaryConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, mcmcSummaryConfigFileName);
        String outputFileName = configFile.getString(0);
        return new McmcSummaryConfig(mcmcSummaryConfigFileName, outputFileName);
    }
}
