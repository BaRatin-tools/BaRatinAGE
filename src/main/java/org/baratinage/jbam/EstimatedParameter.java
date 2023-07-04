package org.baratinage.jbam;

public class EstimatedParameter {
    private String name;
    private double[] mcmc;
    private double[] summary;
    final private static String[] SUMMARY_STATISTICS_NAMES = new String[] {
            "n", "min", "max", "range", "mean", "mediann", "q10", "q25",
            "q75", "q90", "std", "var", "cv", "skewness", "kurtosis", "maxpost"
    };

    public EstimatedParameter(String name, double[] mcmc, double[] summary) {
        this.name = name;
        this.mcmc = mcmc;
        this.summary = summary;
    }

    public String getName() {
        return this.name;
    }

    public double[] getMcmc() {
        return this.mcmc;
    }

    public double[] getSummary() {
        return this.summary;
    }

    @Override
    public String toString() {
        String str = String.format("Parameter results '%s' with %d samples",
                this.name, this.mcmc.length);
        if (this.summary != null) {
            String[] summaryStr = new String[this.summary.length];
            for (int k = 0; k < this.summary.length; k++) {
                summaryStr[k] = String.format("%s: %.2e", SUMMARY_STATISTICS_NAMES[k], this.summary[k]);
            }
            str += " (" + String.join(", ", summaryStr) + ")";
        }
        return str;
    }
}
