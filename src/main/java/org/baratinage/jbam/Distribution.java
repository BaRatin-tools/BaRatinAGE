package org.baratinage.jbam;

public class Distribution {
    public final DISTRIBUTION distribution;
    public final double[] parameterValues;

    public static enum DISTRIBUTION {
        GAUSSIAN("Gaussian", new String[] { "mean", "std" }),
        LOG_NORMAL("LogNormal", new String[] { "log_mean", "log_std" }),
        UNIFORM("Uniform", new String[] { "lower_bound", "upper_bound" }),
        EXPONENTIAL("Exponential", new String[] { "location", "scale" }),
        GEV("GEV", new String[] { "location", "scale", "shape" }),
        FIXED("Fixed", new String[] {});

        public final String bamName;
        public final String[] parameterNames;

        private DISTRIBUTION(String bamName, String[] parameterNames) {
            this.bamName = bamName;
            this.parameterNames = parameterNames;
        }

        @Override
        public String toString() {
            return name();
        }

        static public DISTRIBUTION getDistribFromBamName(String name) {
            for (DISTRIBUTION d : DISTRIBUTION.values()) {
                if (d.bamName.equals(name)) {
                    return d;
                }
            }
            return null;
        }
    };

    public Distribution(DISTRIBUTION distribution, double... parameterValues) {
        int n = distribution.parameterNames.length;
        if (n != parameterValues.length) {
            throw new IllegalArgumentException(
                    "Length of parameterValues must match the length of expected parameters of DISTRIB!");
        }
        this.distribution = distribution;
        this.parameterValues = parameterValues;
    }

    @Override
    public String toString() {
        String str = String.format("'%s' (", distribution.name());
        int n = distribution.parameterNames.length;
        for (int k = 0; k < n; k++) {
            str = str + String.format("%s: %f",
                    distribution.parameterNames[k], parameterValues[k]);
            if (k != n - 1) {
                str = str + ", ";
            }
        }
        str = str + ")";
        return str;
    }
}
