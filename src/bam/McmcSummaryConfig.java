package bam;

import bam.utils.ConfigFile;

public class McmcSummaryConfig {
    private String outputFileName;

    public McmcSummaryConfig() {
        this.outputFileName = ConfigFile.RESULTS_MCMC_SUMMARY;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_MCMC_SUMMARY);
    }

    @Override
    public String toString() {
        String str = "Config - McmcSummaryConfig: ";
        str += String.format("%s.\n", this.outputFileName);
        return str;
    }
}
