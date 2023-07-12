package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcSummaryConfig {
    private String outputFileName;
    // FIXME: see
    // https://github.com/BaM-tools/BaM/commit/1c22189af4cd00d0e8267c2b95e22c38ac56a517

    public McmcSummaryConfig() {
        this(BamFilesHelpers.RESULTS_MCMC_SUMMARY);
    }

    public McmcSummaryConfig(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, BamFilesHelpers.CONFIG_MCMC_SUMMARY);
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
        return new McmcSummaryConfig(outputFileName);
    }
}
