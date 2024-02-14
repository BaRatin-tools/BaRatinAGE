package org.baratinage.jbam;

import java.util.List;
import java.util.function.Consumer;

import org.baratinage.utils.Calc;

public class EstimatedParameter {

    public final String name;
    public final double[] mcmc;
    public final double[] summary;
    public final int maxpostIndex;
    public final Parameter parameterConfig;
    private List<double[]> density;
    private double[] u95;

    private Float validityCheckResult = null;

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

    public Float getValidityCheckEstimate() {
        // 0.5 = valid
        // 0 = underestimated,
        // 1 = overestimated
        if (parameterConfig == null) {
            return null;
        }
        if (validityCheckResult != null) {
            return validityCheckResult;
        }
        int n = mcmc.length;
        double[] priorSamples = parameterConfig.distribution.getRandomValues(n);
        int belowCount = 0;
        for (int k = 0; k < n; k++) {
            if (mcmc[k] < priorSamples[k]) {
                belowCount++;
            }
        }
        float belowFreq = ((float) belowCount / (float) n);
        validityCheckResult = 1f - belowFreq;
        return validityCheckResult;
    }

    public List<double[]> getPriorDensity() {
        if (parameterConfig != null) {
            return parameterConfig.distribution.getDensity();
        }
        return null;
    }

    public void getPriorDensity(Consumer<List<double[]>> onDone) {
        if (parameterConfig != null) {
            parameterConfig.distribution.getDensity(onDone);
        }
    }

    public List<double[]> getPostDensity() {
        if (density != null) {
            return density;
        }
        double[] sorted = Calc.sort(mcmc);
        density = Calc.density(sorted, 20);
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
