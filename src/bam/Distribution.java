package bam;

public class Distribution {
    private String name;
    private double[] parameterValues;
    private String[] parameterNames;

    private Distribution(String name, String[] parameterNames, double[] parameterValues) {
        this.name = name;
        this.parameterNames = parameterNames;
        this.parameterValues = parameterValues;
    }

    public static Distribution Gaussian(double mean, double std) {
        String[] parameterNames = new String[] { "mean", "std" };
        double[] parameterValues = { mean, std };
        return new Distribution("Gaussian", parameterNames, parameterValues);
    }

    public static Distribution LogNormal(double logMean, double logStd) {
        String[] parameterNames = new String[] { "logMean", "logStd" };
        double[] parameterValues = { logMean, logStd };
        return new Distribution("LogNormal", parameterNames, parameterValues);
    }

    public static Distribution Uniform(double lowerBound, double upperBound) {
        String[] parameterNames = new String[] { "lowerBound", "upperBound" };
        double[] parameterValues = { lowerBound, upperBound };
        return new Distribution("Uniform", parameterNames, parameterValues);
    }

    public static Distribution Fixed() {
        String[] parameterNames = new String[] {};
        double[] parameterValues = {};
        return new Distribution("Fixed", parameterNames, parameterValues);
    }

    public String getName() {
        return this.name;
    }

    public double[] getParameterValues() {
        return this.parameterValues;
    }

    // public String[] getParameterNames() {
    // return this.parameterNames;
    // }

    // public int getNumberOfParameters() {
    // return this.parameterNames.length;
    // }

    public void log() {
        System.out.print(String.format("'%s' (", this.name));
        int n = this.parameterNames.length;
        for (int k = 0; k < n; k++) {
            System.out.print(String.format("%s: %f", this.parameterNames[k], this.parameterValues[k]));
            if (k != n - 1) {
                System.out.print(", ");
            }
        }
        System.out.print(")\n");
    }
}
