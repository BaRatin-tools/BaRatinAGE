package org.baratinage.jbam;

public class Distribution {
    private DISTRIB distrib;
    private double[] parameterValues;

    public static enum DISTRIB {
        GAUSSIAN("dist_gaussian", new String[] { "mean", "std" }),
        LOG_NORMAL("dist_log_normal", new String[] { "log_mean", "log_std" }),
        UNIFORM("dist_uniform", new String[] { "lower_bound", "upper_bound" }),
        EXPONENTIAL("dist_exponential", new String[] { "location", "scale" }),
        GEV("dist_gev", new String[] { "location", "scale", "shape" }),
        FIXED("dist_fixed", new String[] {});

        public String name;
        public String[] parameterNames;

        private DISTRIB(String name, String[] parameterNames) {
            this.name = name;
            this.parameterNames = parameterNames;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public Distribution(DISTRIB distrib, double... parameterValues) {
        int n = distrib.parameterNames.length;
        if (n != parameterValues.length) {
            throw new IllegalArgumentException(
                    "Length of parameterValues must match the length of expected parameters of DISTRIB!");
        }
        this.distrib = distrib;
        this.parameterValues = parameterValues;
    }

    public DISTRIB getDistrib() {
        return this.distrib;
    }

    public String getName() {
        return this.distrib.name;
    }

    public String[] getParameterNames() {
        return this.distrib.parameterNames;
    }

    public double[] getParameterValues() {
        return this.parameterValues;
    }

    @Override
    public String toString() {
        String str = String.format("'%s' (", this.distrib.name);
        int n = this.distrib.parameterNames.length;
        for (int k = 0; k < n; k++) {
            str = str + String.format("%s: %f",
                    this.distrib.parameterNames[k], this.parameterValues[k]);
            if (k != n - 1) {
                str = str + ", ";
            }
        }
        str = str + ")";
        return str;
    }
}
