package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFileNames;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcSummaryConfig {
    private String outputFileName;
    // FIXME: see
    // https://github.com/BaM-tools/BaM/commit/1c22189af4cd00d0e8267c2b95e22c38ac56a517

    public McmcSummaryConfig() {
        this.outputFileName = BamFileNames.RESULTS_MCMC_SUMMARY;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, BamFileNames.CONFIG_MCMC_SUMMARY);
    }

    @Override
    public String toString() {
        String str = "Config - McmcSummaryConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }
}
