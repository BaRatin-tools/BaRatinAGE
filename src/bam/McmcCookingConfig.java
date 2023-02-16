package bam;

import bam.exe.ConfigFile;

public class McmcCookingConfig {
    private String outputFileName;
    private double burnFactor;
    private int nSlim;

    public McmcCookingConfig() {
        this.outputFileName = ConfigFile.RESULTS_MCMC_COOKING;
        this.burnFactor = 0.5;
        this.nSlim = 10;
    }

    public void writeConfig(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.addItem(this.burnFactor, "BurnFactor");
        configFile.addItem(this.nSlim, "Nslim");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_MCMC_COOKING);
    }

    public void log() {
        System.out.print("Config - McmcCookingConfig: ");
        System.out.print(String.format("%s; ", this.outputFileName));
        System.out.print(String.format("%f; ", this.burnFactor));
        System.out.print(String.format("%d; ", this.nSlim));
        System.out.print(".\n");
    }
}
