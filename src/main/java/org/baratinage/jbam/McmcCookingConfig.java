package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFileNames;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcCookingConfig {
    private String outputFileName;
    private double burnFactor;
    private int nSlim;

    public McmcCookingConfig() {
        this.outputFileName = BamFileNames.RESULTS_MCMC_COOKING;
        this.burnFactor = 0.5;
        this.nSlim = 10;
    }

    public int numberOfCookedMcmcSamples(int numberOfMcmcSamples) {
        return (int) (numberOfMcmcSamples * this.burnFactor / this.nSlim);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.addItem(this.burnFactor, "BurnFactor");
        configFile.addItem(this.nSlim, "Nslim");
        configFile.writeToFile(workspace, BamFileNames.CONFIG_MCMC_COOKING);
    }

    @Override
    public String toString() {
        String str = "Config - McmcCookingConfig: ";
        str += String.format("%s; ", this.outputFileName);
        str += String.format("%f; ", this.burnFactor);
        str += String.format("%d.\n", this.nSlim);
        return str;
    }
}
