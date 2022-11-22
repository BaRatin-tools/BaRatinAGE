package moteur;

public class RunOptions {
    private boolean doMCMC;
    private boolean doMcmcSummary;
    private boolean doResidualDiag;
    private boolean doPrediction;

    public RunOptions(boolean doMcmc, boolean doMcmcSummary, boolean doResidualDiag, boolean doPrediction) {
        this.doMCMC = doMcmc;
        this.doMcmcSummary = doMcmcSummary;
        this.doResidualDiag = doResidualDiag;
        this.doPrediction = doPrediction;
    }

    public boolean doMCMC() {
        return doMCMC;
    }

    public boolean doMcmcSummary() {
        return doMcmcSummary;
    }

    public boolean doResidualDiag() {
        return doResidualDiag;
    }

    public boolean doPrediction() {
        return doPrediction;
    }
}
