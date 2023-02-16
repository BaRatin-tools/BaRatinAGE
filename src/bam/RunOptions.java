package bam;

import bam.exe.ConfigFile;

public class RunOptions {
    private boolean doMcmc;
    private boolean doSummary;
    private boolean doResidual;
    private boolean doPrediction;

    public RunOptions(
            boolean doMcmc,
            boolean doSummary,
            boolean doResidual,
            boolean doPrediction) {
        this.doMcmc = doMcmc;
        this.doSummary = doSummary;
        this.doResidual = doResidual;
        this.doPrediction = doPrediction;
    }

    public void writeConfig(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.doMcmc, "Do MCMC?");
        configFile.addItem(this.doSummary, "Do MCMC summary?");
        configFile.addItem(this.doResidual, "Do Residual diagnostics?");
        configFile.addItem(this.doPrediction, "Do Predictions?");
        configFile.writeToFile(workspace, ConfigFile.CONFIG_RUN_OPTIONS);
    }

    public void log() {
        System.out.println(String.format("RunOptions: %b, %b, %b, %b",
                this.doMcmc,
                this.doSummary,
                this.doResidual,
                this.doPrediction));
    }
}
