package org.baratinage.jbam;

import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.utils.Arr;

public final class Parameter extends AbstractParameter {
    public final Distribution distribution;

    public Parameter(String name, double initialGuess, Distribution distribution) {
        super(name, initialGuess);
        this.distribution = distribution;
    }

    public double[] getRandomValues(int n) {
        if (distribution.type.equals(DistributionType.FIXED)) {
            return Arr.makeDoubleArray(n, initialGuess);
        }
        return distribution.getRandomValues(n);
    }

    public double[] getPercentiles(double low, double high, int nsteps) {
        if (distribution.type.equals(DistributionType.FIXED)) {
            return Arr.makeDoubleArray(nsteps, initialGuess);
        }
        return distribution.getPercentiles(low, high, nsteps);

    }

    public Double getMedian() {
        if (distribution.type.equals(DistributionType.FIXED)) {
            return initialGuess;
        }
        return distribution.getMedian();
    }

    @Override
    public Parameter getRenamedClone(String name) {
        return new Parameter(name, initialGuess, distribution);
    }

    @Override
    public void toFile(ConfigFile modelConfigFile, String workspace) {
        modelConfigFile.addItem(name, "Parameter name -----", true);
        modelConfigFile.addItem(initialGuess, "Initial guess");
        modelConfigFile.addItem(distribution.type.bamName, "Prior distribution", true);
        modelConfigFile.addItem(distribution.parameterValues, "Prior parameters");
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(super.toString(), this.distribution.toString());
    }
}
