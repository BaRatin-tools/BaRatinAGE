package org.baratinage.jbam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ExeRun;
import org.baratinage.jbam.utils.Read;

public class Distribution {

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

    public final DISTRIBUTION distribution;
    public final double[] parameterValues;
    private List<double[]> density;

    private final String id;

    public Distribution(DISTRIBUTION distribution, double... parameterValues) {
        int n = distribution.parameterNames.length;
        if (n != parameterValues.length) {
            throw new IllegalArgumentException(
                    "Length of parameterValues must match the length of expected parameters of DISTRIB!");
        }
        this.distribution = distribution;
        this.parameterValues = parameterValues;
        id = UUID.randomUUID().toString();
    }

    public static final String EXE_DIR = BamFilesHelpers.EXE_DIR;
    public static final String EXE_NAME = "Distribution";

    public static final String EXE_COMMAND = BamFilesHelpers.OS.startsWith("windows")
            ? Path.of(BamFilesHelpers.EXE_DIR, String.format("%s.exe", EXE_NAME)).toString()
            : String.format("./%s", EXE_NAME);

    private static List<double[]> getExeRunResult(String filePath) {
        try {
            List<double[]> result = Read.readMatrix(filePath, "\\s+", 0, 0);
            File f = new File(filePath);
            f.delete();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String doubleArrToStringArg(double... values) {
        int n = values.length;
        String[] valuesString = new String[n];
        for (int k = 0; k < n; k++) {
            valuesString[k] = Double.toString(values[k]);
        }
        return String.join(",", valuesString);
    }

    public List<double[]> getDensity() {

        if (density != null) {
            return density;
        }

        String parametersArg = doubleArrToStringArg(parameterValues);

        String rangeResFileName = id + "_range.txt";

        ExeRun rangeRun = new ExeRun();
        rangeRun.setExeDir(EXE_DIR);
        rangeRun.setCommand(EXE_COMMAND,
                "--name", distribution.bamName,
                "--parameters", parametersArg,
                "--action", "q",
                "--xgrid", "0.00001,0.99999,2",
                "--result", rangeResFileName);

        rangeRun.run();

        List<double[]> rangeRes = getExeRunResult(Path.of(EXE_DIR, rangeResFileName).toString());
        if (rangeRes == null) {
            System.err.println("Distribution Error: error while reading quantiles result files! Aborting");
            return null;
        }
        String gridArg = doubleArrToStringArg(rangeRes.get(1)[0], rangeRes.get(1)[1]);
        gridArg += ",500";

        String densityResFileName = id + "_density.txt";

        ExeRun densityRun = new ExeRun();
        densityRun.setExeDir(EXE_DIR);
        densityRun.setCommand(EXE_COMMAND,
                "--name", distribution.bamName,
                "--parameters", parametersArg,
                "--action", "d",
                "--xgrid", gridArg,
                "--result", densityResFileName);

        densityRun.run();

        List<double[]> densityRes = getExeRunResult(Path.of(EXE_DIR, densityResFileName).toString());
        if (densityRes == null) {
            System.err.println("Distribution Error: error while reading density result files! Aborting");
            return null;
        }

        return densityRes;

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
