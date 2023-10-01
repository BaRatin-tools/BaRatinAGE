package org.baratinage.jbam;

public enum DistributionType {
    GAUSSIAN("Gaussian", new String[] { "mean", "std" }),
    LOG_NORMAL("LogNormal", new String[] { "log_mean", "log_std" }),
    UNIFORM("Uniform", new String[] { "lower_bound", "upper_bound" }),
    EXPONENTIAL("Exponential", new String[] { "location", "scale" }),
    GEV("GEV", new String[] { "location", "scale", "shape" }),
    FIXED("Fixed", new String[] {});

    public final String bamName;
    public final String[] parameterNames;

    private DistributionType(String bamName, String[] parameterNames) {
        this.bamName = bamName;
        this.parameterNames = parameterNames;
    }

    @Override
    public String toString() {
        return name();
    }

    static public DistributionType getDistribFromBamName(String name) {
        for (DistributionType d : DistributionType.values()) {
            if (d.bamName.equals(name)) {
                return d;
            }
        }
        return null;
    }
}
