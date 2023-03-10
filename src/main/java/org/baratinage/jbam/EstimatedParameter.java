package org.baratinage.jbam;

public class EstimatedParameter {
    private String name;
    private double[] mcmc;
    private double[] summary;
    final private static String[] SUMMARY_STATISTICS_NAMES = new String[] {
            "N", "Minimum", "Maximum", "Range", "Mean", "Median", "Q10%", "Q25%",
            "Q75%", "Q90", "St. Dev.", "Variance", "CV", "Skewness", "Kurtosis", "MaxPost"
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
