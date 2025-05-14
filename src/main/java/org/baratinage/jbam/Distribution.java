package org.baratinage.jbam;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.baratinage.jbam.utils.DistributionCLI;

import org.baratinage.utils.ConsoleLogger;

public class Distribution {

    public final DistributionType type;
    public final double[] parameterValues;
    private List<double[]> density;

    public static Distribution buildDistributionFromBamName(String bamName, double... parameterValues) {
        return new Distribution(DistributionType.getDistribFromBamName(bamName), parameterValues);
    }

    public Distribution(DistributionType type, double... parameterValues) {
        int n = type.parameterNames.length;
        int m = parameterValues.length;
        if (n != m) {
            if (m < n) {
                throw new IllegalArgumentException(
                        "Length of parameterValues must match the length of expected parameters of DISTRIB!");
            } else {
                ConsoleLogger.error("Too many parameter values for '" +
                        type + "'... Only using first " + n + " (out of " + m + ") parameter values.");
                double[] truncatedParameterValues = new double[n];
                for (int k = 0; k < n; k++) {
                    truncatedParameterValues[k] = parameterValues[k];
                }
                parameterValues = truncatedParameterValues;
            }

        }
        this.type = type;
        this.parameterValues = parameterValues;
    }

    private static final int DENSITY_SAMPLES = 100;
    private static final double DENSITY_RANGE_EDGE = 1e-5;
    private static final double[] DENSITY_RANGE = new double[] {
            0d + DENSITY_RANGE_EDGE, 1d - DENSITY_RANGE_EDGE
    };

    public List<double[]> getDensity() {
        if (density != null) {
            return null;
        }

        Optional<List<double[]>> quantilesOpt = DistributionCLI.getQuantiles(
                type.bamName,
                parameterValues,
                DENSITY_RANGE[0],
                DENSITY_RANGE[1],
                2);

        if (quantilesOpt.isEmpty()) {
            return null;
        }
        double[] rangeRes = quantilesOpt.get().get(1);

        Optional<List<double[]>> densityOpt = DistributionCLI.getDensity(
                type.bamName,
                parameterValues,
                rangeRes[0],
                rangeRes[1],
                DENSITY_SAMPLES);

        if (densityOpt.isEmpty()) {
            return null;
        } else {
            density = densityOpt.get();
            return density;
        }
    }

    public void getDensity(Consumer<List<double[]>> onDone) {
        Thread thread = new Thread(
                () -> {
                    List<double[]> result = getDensity();
                    onDone.accept(result);
                });
        thread.start();
    }

    public double[] getRandomValues(int n) {
        Optional<double[]> randomValsOpt = DistributionCLI.getRandom(
                type.bamName,
                parameterValues,
                n);
        return randomValsOpt.isEmpty() ? null : randomValsOpt.get();
    }

    public double[] getPercentiles(double low, double high, int nsteps) {
        Optional<List<double[]>> quantilesOpt = DistributionCLI.getQuantiles(type.bamName,
                parameterValues, low, high, nsteps);
        if (quantilesOpt.isEmpty()) {
            return null;
        } else {
            return quantilesOpt.get().get(1);
        }
    }

    public Double getMedian() {
        double[] quantiles = getPercentiles(0.5, 0.5, 1);
        if (quantiles == null) {
            return null;
        }
        if (quantiles.length != 1) {
            return null;
        }
        return quantiles[0];
    }

    @Override
    public String toString() {
        String str = String.format("'%s' (", type.name());
        int n = type.parameterNames.length;
        for (int k = 0; k < n; k++) {
            str = str + String.format("%s: %f",
                    type.parameterNames[k], parameterValues[k]);
            if (k != n - 1) {
                str = str + ", ";
            }
        }
        str = str + ")";
        return str;
    }
}
