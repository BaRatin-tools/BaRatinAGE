package org.baratinage.jbam.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

public class DistributionCLI {

  private static final String EXE_DIR = BamFilesHelpers.EXE_DIR;
  private static final String EXE_NAME = "distribution";
  private static final String EXE_COMMAND = BamFilesHelpers.OS.startsWith("windows")
      ? Path.of(BamFilesHelpers.EXE_DIR, String.format("%s.exe", EXE_NAME)).toString()
      : String.format("./%s", EXE_NAME);

  private static String doubleArrToStringArg(double... values) {
    int n = values.length;
    String[] valuesString = new String[n];
    for (int k = 0; k < n; k++) {
      valuesString[k] = Double.toString(values[k]);
    }
    return String.join(",", valuesString);
  }

  private static enum ACTION {
    PDF("d"),
    CDF("p"),
    QUANTILE("q"),
    RANDOM("r");

    public final String actionKey;

    private ACTION(String actionKey) {
      this.actionKey = actionKey;
    }
  }

  private static ExeRun runDistributionCommand(String distributionName, double[] distributionParameters, ACTION action,
      Double gridMin, Double gridMax, Integer nGrid, Integer nSim) {

    String parametersArg = doubleArrToStringArg(distributionParameters);

    ExeRun exeRun = new ExeRun();

    exeRun.setExeDir(EXE_DIR);
    exeRun.setCommand(EXE_COMMAND,
        "-name", distributionName,
        "-par", parametersArg,
        "-act", action.actionKey,
        action == ACTION.RANDOM ? "-n" : "-x",
        action == ACTION.RANDOM ? "" + nSim : gridMin + "," + gridMax + "," + nGrid,
        "-con");
    exeRun.run();
    return exeRun;
  }

  private static Map<String, List<double[]>> memoizedDPQ = new HashMap<>();

  private static Optional<List<double[]>> runDistributionCommand_DPQ(String distributionName,
      double[] distributionParameters,
      ACTION action,
      Double low, Double high, Integer n) {

    List<String> runOut = new ArrayList<>();
    try {

      String memoizationKey = String.format("%s_%s_%s_%s",
          distributionName,
          doubleArrToStringArg(distributionParameters),
          action.actionKey,
          doubleArrToStringArg(low, high, (double) n));
      if (memoizedDPQ.containsKey(memoizationKey)) {
        ConsoleLogger.log(String.format("Using memoized distribution CLI result (%s)", memoizationKey));
        return Optional.of(memoizedDPQ.get(memoizationKey));
      }

      ExeRun run = runDistributionCommand(distributionName,
          distributionParameters,
          action,
          low,
          high,
          n,
          null);
      runOut = run.getLastRunConsoleOutputs();
      List<double[]> runValues = Misc.transposeDoubleMatrix(
          Misc.stringToDoubleMatrix(
              runOut,
              BamFilesHelpers.BAM_COLUMN_SEPARATOR));

      ConsoleLogger.log(String.format("Memoizing distribution CLI result (%s)", memoizationKey));
      memoizedDPQ.put(memoizationKey, runValues);
      return Optional.of(runValues);

    } catch (Exception e) {
      String msg = "";
      for (String l : runOut) {
        msg += l + "\n";
      }
      ConsoleLogger.error(e);
      ConsoleLogger.error(msg);
      // List<double[]> defaultResult = new ArrayList<>();
      // defaultResult.add(new double[n]);
      // defaultResult.add(new double[n]);
      // return defaultResult;
      return Optional.empty();
    }
  }

  public static Optional<List<double[]>> getQuantiles(String distributionName, double[] distributionParameters,
      double qLow,
      double qHigh, int nQuantiles) {
    return runDistributionCommand_DPQ(distributionName,
        distributionParameters,
        ACTION.QUANTILE,
        qLow,
        qHigh,
        nQuantiles);
  }

  public static Optional<List<double[]>> getDensity(
      String distributionName,
      double[] distributionParameters,
      double valueLow,
      double valueHigh,
      int nValues) {
    return runDistributionCommand_DPQ(distributionName,
        distributionParameters,
        ACTION.PDF,
        valueLow,
        valueHigh,
        nValues);
  }

  public static Optional<double[]> getRandom(
      String distributionName,
      double[] distributionParameters,
      int nValues) {
    try {

      ExeRun run = runDistributionCommand(distributionName,
          distributionParameters,
          ACTION.RANDOM,
          null,
          null,
          null,
          nValues);
      List<String> runOut = run.getLastRunConsoleOutputs();
      List<double[]> runValues = Misc.transposeDoubleMatrix(
          Misc.stringToDoubleMatrix(
              runOut,
              BamFilesHelpers.BAM_COLUMN_SEPARATOR));
      return Optional.of(runValues.get(0));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

}
