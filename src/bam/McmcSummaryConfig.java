package bam;

import bam.exe.ConfigFile;

public class McmcSummaryConfig {
    private String outputFileName;

    public McmcSummaryConfig() {
        this.outputFileName = ConfigFile.RESULTS_MCMC_SUMMARY;
    }

    public void writeConfig(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_MCMC_SUMMARY);
    }

    public void log() {
        System.out.print("Config - McmcSummaryConfig: ");
        System.out.print(String.format("%s", this.outputFileName));
        System.out.print(".\n");
    }
}
