package org.baratinage.jbam;

import org.baratinage.jbam.utils.ConfigFile;

public class RunOptions {
    public final String fileName;
    public final boolean doMcmc;
    public final boolean doSummary;
    public final boolean doResidual;
    public final boolean doPrediction;

    public RunOptions(
            String fileName,
            boolean doMcmc,
            boolean doSummary,
            boolean doResidual,
            boolean doPrediction) {
        this.fileName = fileName;
        this.doMcmc = doMcmc;
        this.doSummary = doSummary;
        this.doResidual = doResidual;
        this.doPrediction = doPrediction;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.doMcmc, "Do MCMC?");
        configFile.addItem(this.doSummary, "Do MCMC summary?");
        configFile.addItem(this.doResidual, "Do Residual diagnostics?");
        configFile.addItem(this.doPrediction, "Do Predictions?");
        configFile.writeToFile(workspace, fileName);
    }

    @Override
    public String toString() {
        return String.format("RunOptions: %b, %b, %b, %b",
                this.doMcmc,
                this.doSummary,
                this.doResidual,
                this.doPrediction);
    }

    public static RunOptions readRunOptions(String workspace, String runOptionConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, runOptionConfigFileName);
        boolean doMcmc = configFile.getBoolean(0);
        boolean doSummary = configFile.getBoolean(1);
        boolean doResidual = configFile.getBoolean(2);
        boolean doPrediction = configFile.getBoolean(3);
        return new RunOptions(
                runOptionConfigFileName,
                doMcmc,
                doSummary,
                doResidual,
                doPrediction);
    }
}
