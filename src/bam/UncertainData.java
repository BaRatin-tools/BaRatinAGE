package bam;

// import utils.Matrix;

public class UncertainData {
    private String name;
    private double[] values;
    private double[] nonSysStd;
    private double[] sysStd;
    private int[] sysIndices;

    public UncertainData(String name, double[] values, double[] nonSysStd, double[] sysStd, int[] sysIndices) {
        int n = values.length;
        if (nonSysStd.length != n && nonSysStd.length != 0) {
            System.err.println("nonSysStd is not the correct length, set to empty");
            nonSysStd = new double[] {};
        }
        if ((sysStd.length != n && sysStd.length != 0) ||
                (sysIndices.length != n && sysIndices.length != 0)) {
            System.err.println("sysStd is not the correct length, set to empty");
            sysStd = new double[] {};
            sysIndices = new int[] {};
        }

        this.name = name;
        this.values = values;
        this.nonSysStd = nonSysStd;
        this.sysStd = sysStd;
        this.sysStd = sysStd;
    }

    public UncertainData(String name, double[] values, double[] nonSysStd) {
        this(name, values, nonSysStd, new double[] {}, new int[] {});
    }

    public UncertainData(String name, double[] values) {
        this(name, values, new double[] {}, new double[] {}, new int[] {});
    }

    public String getName() {
        return this.name;
    }

    public double[] getValues() {
        return this.values;
    }

    public double[] getNonSysStd() {
        return this.nonSysStd;
    }

    public double[] getSysStd() {
        return this.sysStd;
    }

    public int[] getSysIndices() {
        return this.sysIndices;
    }

    public double[] getSysIndicesAsDouble() {
        int n = this.sysIndices.length;
        double[] sysIndices = new double[n];
        for (int k = 0; k < n; k++) {
            sysIndices[k] = (double) this.sysIndices[k];
        }
        return sysIndices;
    }

    // public double[][] getMatrix() {
    // if (this.hasSysError()) {
    // double[] sysIndicesAsDouble = new double[this.length()];
    // for (int k = 0; k < this.length(); k++) {
    // sysIndicesAsDouble[k] = (double) this.sysIndices[k];
    // }
    // if (this.hasNonSysError()) {
    // return Matrix.concat(this.values, this.nonSysStd, this.sysStd,
    // sysIndicesAsDouble);
    // } else {
    // return Matrix.concat(this.values, this.sysStd, sysIndicesAsDouble);
    // }
    // } else {
    // if (this.hasNonSysError()) {
    // return Matrix.concat(this.values, this.nonSysStd);
    // } else {
    // return Matrix.concat(this.values);
    // }
    // }
    // // return new double[0][0];
    // }

    public boolean hasNonSysError() {
        return this.nonSysStd.length > 0;
    }

    public boolean hasSysError() {
        return this.sysStd.length > 0 && this.sysIndices.length > 0;
    }

    public int length() {
        return this.values.length;
    }

    public void log() {
        boolean hasSysError = this.sysStd.length > 0;
        boolean hasNonSysError = this.nonSysStd.length > 0 && this.sysIndices.length > 0;
        int n = this.values.length;
        System.out.print(String.format("Data '%s' contains %d elements with ", this.name, n));
        if (hasNonSysError) {
            System.out.print("non-systematic errors and ");
        } else {
            System.out.print("NO non-systematic errors and ");
        }
        if (hasSysError) {
            System.out.print("systematic errors.\n");
        } else {
            System.out.print("NO systematic errors.\n");
        }
    }
}
