package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcCookingConfig {
    private String outputFileName;
    private double burnFactor;
    private int nSlim;

    public McmcCookingConfig() {
        this(BamFilesHelpers.RESULTS_MCMC_COOKING, 0.5, 10);
    }

    public McmcCookingConfig(
            String outputFileName,
            double burnFactor,
            int nSlim) {
        this.outputFileName = outputFileName;
        this.burnFactor = burnFactor;
        this.nSlim = nSlim;
    }

    public int numberOfCookedMcmcSamples(int numberOfMcmcSamples) {
        return (int) (numberOfMcmcSamples * this.burnFactor / this.nSlim);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "Result file");
        configFile.addItem(this.burnFactor, "BurnFactor");
        configFile.addItem(this.nSlim, "Nslim");
        configFile.writeToFile(workspace, BamFilesHelpers.CONFIG_MCMC_COOKING);
    }

    @Override
    public String toString() {
        String str = "Config - McmcCookingConfig: ";
        str += String.format("%s; ", this.outputFileName);
        str += String.format("%f; ", this.burnFactor);
        str += String.format("%d.\n", this.nSlim);
        return str;
    }

    static public McmcCookingConfig readMcmcCookingConfig(String workspace, String mcmcCookingConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, mcmcCookingConfigFileName);
        String outputFileName = configFile.getString(0);
        double burnFactor = configFile.getDouble(1);
        int nSlim = configFile.getInt(2);
        return new McmcCookingConfig(outputFileName, burnFactor, nSlim);
    }
}
