package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ExeRun;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;

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
        id = UUID.randomUUID().toString().split("-")[0];
    }

    private static final String EXE_DIR = BamFilesHelpers.EXE_DIR;
    private static final String EXE_NAME = "distribution";
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
            List<double[]> result = ReadFile.readMatrix(
                    filePath,
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    0, Integer.MAX_VALUE,
                    BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                    false, true);
            Files.delete(Path.of(filePath));
            return result;
        } catch (IOException e) {
            ConsoleLogger.error(e);
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
            ConsoleLogger.log("using memoized density data");
            return memoizedDensities.get(densityResultKey);
        }

        // ----------------------------------------------------------
        // computing range
        String rangeResFileName = id + "_range.txt";

        String xGridArgRange = DENSITY_RANGE[0] + "," + DENSITY_RANGE[1] + ",2";

        ExeRun rangeRun = new ExeRun();
        rangeRun.setExeDir(EXE_DIR);
        rangeRun.setCommand(EXE_COMMAND,
                "-name", type.bamName,
                "-par", parametersArg,
                "-act", "q",
                "-x", xGridArgRange,
                "-rf", rangeResFileName);

        rangeRun.run();

        List<double[]> rangeRes = getExeRunResult(Path.of(EXE_DIR, rangeResFileName).toString());
        if (rangeRes == null || rangeRes.size() != 2) {
            ConsoleLogger.error("Distribution Error: error while reading quantiles result files! Aborting");
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
                "-name", type.bamName,
                "-par", parametersArg,
                "-act", "d",
                "-x", xGridArgDensity,
                "-rf", densityResFileName);

        densityRun.run();

        List<double[]> densityRes = getExeRunResult(Path.of(EXE_DIR, densityResFileName).toString());
        if (densityRes == null) {
            ConsoleLogger.error("Distribution Error: error while reading density result files! Aborting");
            return null;
        }

        // ----------------------------------------------------------
        // memoizing
        memoizedDensities.put(densityResultKey, densityRes);

        return densityRes;
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

        String randomValuesResFileName = id + "_random.txt";

        String parametersArg = doubleArrToStringArg(parameterValues);

        ExeRun densityRun = new ExeRun();
        densityRun.setExeDir(EXE_DIR);
        densityRun.setCommand(EXE_COMMAND,
                "-name", type.bamName,
                "-par", parametersArg,
                "-act", "r",
                "-n", "" + n,
                "-rf", randomValuesResFileName);

        densityRun.run();

        List<double[]> randomValues = getExeRunResult(Path.of(EXE_DIR, randomValuesResFileName).toString());
        if (randomValues == null || randomValues.size() < 1) {
            ConsoleLogger.error("Distribution Error: error while reading random values result files! Aborting");
            return null;
        }

        return randomValues.get(0);
    }

    private static Map<String, double[]> memoizedPercentiles = new HashMap<>();

    public double[] getPercentiles(double low, double high, int nsteps) {
        String percentilesResultFileName = id + "_percentiles.txt";

        String probsArgs = doubleArrToStringArg(low, high) + "," + nsteps;
        String parametersArg = doubleArrToStringArg(parameterValues);

        String percentilesResultKey = type.bamName + "_" + parametersArg;

        // ----------------------------------------------------------
        // using memoized data
        if (memoizedPercentiles.containsKey(percentilesResultKey)) {
            ConsoleLogger.log("using memoized percentiles data");
            return memoizedPercentiles.get(percentilesResultKey);
        }

        // ----------------------------------------------------------
        // computing percentiles

        ExeRun percentileRun = new ExeRun();
        percentileRun.setExeDir(EXE_DIR);
        percentileRun.setCommand(EXE_COMMAND,
                "-name", type.bamName,
                "-par", parametersArg,
                "-act", "q",
                "-x", probsArgs,
                "-rf", percentilesResultFileName);

        percentileRun.run();

        List<double[]> rangeRes = getExeRunResult(Path.of(EXE_DIR, percentilesResultFileName).toString());
        if (rangeRes == null || rangeRes.size() != 2) {
            ConsoleLogger.error("Distribution Error: error while reading quantiles result files! Aborting");
            return null;
        }

        double[] results = rangeRes.get(1);
        memoizedPercentiles.put(percentilesResultKey, results);
        return results;
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
