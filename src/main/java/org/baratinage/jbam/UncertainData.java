package org.baratinage.jbam;

public class UncertainData {
    public final String name;
    public final double[] values;
    public final double[] nonSysStd;
    public final double[] sysStd;
    public final int[] sysIndices;

    public UncertainData(String name, double[] values, double[] nonSysStd, double[] sysStd, int[] sysIndices) {
        int n = values.length;
        if (nonSysStd.length != n && nonSysStd.length != 0) {
            System.err.println("UncertainData Error: nonSysStd is not the correct length, set to empty");
            nonSysStd = new double[] {};
        }
        if ((sysStd.length != n && sysStd.length != 0) ||
                (sysIndices.length != n && sysIndices.length != 0)) {
            System.err.println("UncertainData Error: sysStd is not the correct length, set to empty");
            sysStd = new double[] {};
            sysIndices = new int[] {};
        }
        // FIXME should check data consistency

        this.name = name;
        this.values = values;
        this.nonSysStd = nonSysStd;
        this.sysStd = sysStd;
        this.sysIndices = sysIndices;
    }

    public UncertainData(String name, double[] values, double[] nonSysStd) {
        this(name, values, nonSysStd, new double[] {}, new int[] {});
    }

    public UncertainData(String name, double[] values) {
        this(name, values, new double[] {}, new double[] {}, new int[] {});
    }

    public int getNumberOfValues() {
        return this.values.length;
    }

    public double[] getSysIndicesAsDouble() {
        int n = this.sysIndices.length;
        double[] sysIndices = new double[n];
        for (int k = 0; k < n; k++) {
            sysIndices[k] = (double) this.sysIndices[k];
        }
        return sysIndices;
    }

    public boolean hasNonSysError() {
        return this.nonSysStd.length > 0;
    }

    public boolean hasSysError() {
        return this.sysStd.length > 0 && this.sysIndices.length > 0;
    }

    public int length() {
        return this.values.length;
    }

    @Override
    public String toString() {
        boolean hasSysError = this.sysStd.length > 0;
        boolean hasNonSysError = this.nonSysStd.length > 0 && this.sysIndices.length > 0;
        int n = this.values.length;
        String str = String.format("Data '%s' contains %d elements with ", this.name, n);
        if (hasNonSysError) {
            str += "non-systematic errors and ";
        } else {
            str += "NO non-systematic errors and ";
        }
        if (hasSysError) {
            str += "systematic errors.\n";
        } else {
            str += "NO systematic errors.";
        }
        return str;
    }
}
