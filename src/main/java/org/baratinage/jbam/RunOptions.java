package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFileNames;
import org.baratinage.jbam.utils.ConfigFile;

public class RunOptions {
    public final boolean doMcmc;
    public final boolean doSummary;
    public final boolean doResidual;
    public final boolean doPrediction;

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

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.doMcmc, "Do MCMC?");
        configFile.addItem(this.doSummary, "Do MCMC summary?");
        configFile.addItem(this.doResidual, "Do Residual diagnostics?");
        configFile.addItem(this.doPrediction, "Do Predictions?");
        configFile.writeToFile(workspace, BamFileNames.CONFIG_RUN_OPTIONS);
    }

    @Override
    public String toString() {
        return String.format("RunOptions: %b, %b, %b, %b",
                this.doMcmc,
                this.doSummary,
                this.doResidual,
                this.doPrediction);
    }
}
