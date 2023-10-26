package org.baratinage.jbam;

import java.util.List;

import org.baratinage.utils.Calc;

public class EstimatedParameter {

    public final String name;
    public final double[] mcmc;
    public final double[] summary;
    public final int maxpostIndex;
    public final Parameter parameterConfig;
    private List<double[]> density;
    private double[] u95;

    final private static String[] SUMMARY_STATISTICS_NAMES = new String[] {
            "n", "min", "max", "range", "mean", "mediann", "q10", "q25",
            "q75", "q90", "std", "var", "cv", "skewness", "kurtosis", "maxpost"
    };

    public EstimatedParameter(String name, double[] mcmc, double[] summary, int maxpostIndex, Parameter parameter) {
        this.name = name;
        this.mcmc = mcmc;
        this.summary = summary;
        this.maxpostIndex = maxpostIndex;
        this.parameterConfig = parameter;
    }

    public List<double[]> getPriorDensity() {
        if (parameterConfig != null) {
            return parameterConfig.distribution.getDensity();
        }
        return null;
    }

    public List<double[]> getPostDensity() {
        if (density != null) {
            return density;
        }
        double[] sorted = Calc.sort(mcmc);
        density = Calc.density(sorted, 50);
        return density;
    }

    public double[] get95interval() {
        if (u95 != null) {
            return u95;
        }
        u95 = Calc.percentiles(mcmc, false, 0.025, 0.975);
        return u95;
    }

    public double getMaxpost() {
        return mcmc[maxpostIndex];
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
