package org.baratinage.jbam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ExeRun;
import org.baratinage.jbam.utils.Read;

public class Distribution {

    public final DistributionType type;
    public final double[] parameterValues;
    private List<double[]> density;

    private final String id;

    public static Distribution buildDistributionFromBamName(String bamName, double... parameterValues) {
        return new Distribution(DistributionType.getDistribFromBamName(bamName), parameterValues);
    }

    public Distribution(DistributionType type, double... parameterValues) {
        int n = type.parameterNames.length;
        if (n != parameterValues.length) {
            throw new IllegalArgumentException(
                    "Length of parameterValues must match the length of expected parameters of DISTRIB!");
        }
        this.type = type;
        this.parameterValues = parameterValues;
        id = UUID.randomUUID().toString();
    }

    private static final String EXE_DIR = BamFilesHelpers.EXE_DIR;
    private static final String EXE_NAME = "Distribution";
    private static final int DENSITY_SAMPLES = 100;
    private static final double DENSITY_RANGE_EDGE = 1e-5;
    private static final double[] DENSITY_RANGE = new double[] {
            0d + DENSITY_RANGE_EDGE, 1d - DENSITY_RANGE_EDGE
    };

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

    private static Map<String, List<double[]>> memoizedDensities = new HashMap<>();

    public List<double[]> getDensity() {

        if (density != null) {
            return density;
        }
        String parametersArg = doubleArrToStringArg(parameterValues);

        String densityResultKey = type.bamName + "_" + parametersArg;

        // ----------------------------------------------------------
        // using memoized data
        if (memoizedDensities.containsKey(densityResultKey)) {
            System.out.println("Using memoized data");
            return memoizedDensities.get(densityResultKey);
        }

        // ----------------------------------------------------------
        // computing range
        String rangeResFileName = id + "_range.txt";

        String xGridArgRange = DENSITY_RANGE[0] + "," + DENSITY_RANGE[1] + ",2";
        System.out.println(xGridArgRange);

        ExeRun rangeRun = new ExeRun();
        rangeRun.setExeDir(EXE_DIR);
        rangeRun.setCommand(EXE_COMMAND,
                "--name", type.bamName,
                "--parameters", parametersArg,
                "--action", "q",
                "--xgrid", xGridArgRange,
                "--result", rangeResFileName);

        rangeRun.run();

        List<double[]> rangeRes = getExeRunResult(Path.of(EXE_DIR, rangeResFileName).toString());
        if (rangeRes == null) {
            System.err.println("Distribution Error: error while reading quantiles result files! Aborting");
            return null;
        }
        String xGridArgDensity = doubleArrToStringArg(rangeRes.get(1)[0], rangeRes.get(1)[1]);
        xGridArgDensity = xGridArgDensity + "," + DENSITY_SAMPLES;

        // ----------------------------------------------------------
        // computing density
        String densityResFileName = id + "_density.txt";

        ExeRun densityRun = new ExeRun();
        densityRun.setExeDir(EXE_DIR);
        densityRun.setCommand(EXE_COMMAND,
                "--name", type.bamName,
                "--parameters", parametersArg,
                "--action", "d",
                "--xgrid", xGridArgDensity,
                "--result", densityResFileName);

        densityRun.run();

        List<double[]> densityRes = getExeRunResult(Path.of(EXE_DIR, densityResFileName).toString());
        if (densityRes == null) {
            System.err.println("Distribution Error: error while reading density result files! Aborting");
            return null;
        }

        // ----------------------------------------------------------
        // memoizing
        memoizedDensities.put(densityResultKey, densityRes);

        return densityRes;
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
