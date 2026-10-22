package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.utils.Arr;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.ParameterSymbol;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class CalibrationResult {

    public final CalibrationConfig calibrationConfig;
    public final List<double[]> mcmcValues;
    private final List<double[]> mcmcSummaryValues;
    public final String[] mcmcHeaders;

    public final List<ResultColumn> resultColumns;

    public final CalibrationDataResiduals calibrationDataResiduals;
    public final int maxpostIndex;
    public final int nSamples;
    public final HashMap<String, Double> DIC;

    public CalibrationResult(String workspace, CalibrationConfig calibConfig, RunOptions runOptions) {

        resultColumns = new ArrayList<>();

        calibrationConfig = calibConfig;

        Path cookedMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcCookingConfig.outputFileName);
        Path summaryMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcSummaryConfig.summaryFileName);
        Path calDatResidulatFilePath = Path.of(workspace, calibrationConfig.calDataResidualConfig.outputFileName);

        mcmcValues = readMcmcValues(cookedMcmcFilePath);
        mcmcHeaders = readMcmcHeaders(cookedMcmcFilePath);

        DIC = calibrationConfig.mcmcSummaryConfig.DICFileName != null
                ? readDICValues(Path.of(workspace, calibrationConfig.mcmcSummaryConfig.DICFileName))
                : new HashMap<>();

        int logPostColIndex = -1;
        for (int k = 0; k < mcmcHeaders.length; k++) {
            if (mcmcHeaders[k].equals("LogPost")) {
                logPostColIndex = k;
                break;
            }
        }
        if (logPostColIndex == -1) {
            ConsoleLogger.error("CalibrationResult Error: Cannot find 'LogPost' column in MCMC results!");
            maxpostIndex = -1;
            calibrationDataResiduals = null;
            mcmcSummaryValues = null;
            nSamples = 0;
            return;
        }

        maxpostIndex = retrieveMaxPostIndex(mcmcValues.get(logPostColIndex));
        nSamples = mcmcValues.get(logPostColIndex).length;

        mcmcSummaryValues = readMcmcSummaryValues(summaryMcmcFilePath);

        // make header maps for quick lookups
        Map<String, Integer> headerLookup = new HashMap<>();
        Set<String> unmatchedHeaders = new HashSet<>();
        for (int k = 0; k < mcmcHeaders.length; k++) {
            headerLookup.put(mcmcHeaders[k], k);
            unmatchedHeaders.add(mcmcHeaders[k]);
        }

        // get LogPost
        unmatchedHeaders.remove("LogPost");
        resultColumns.add(new ResultColumn(
                ResultColumn.KIND.LOGPOST,
                "LogPost",
                mcmcValues.get(logPostColIndex),
                null,
                maxpostIndex,
                null));

        // retrieve simple parameters and sub parameters of variable parameters
        for (AbstractParameter ap : calibConfig.model.parameters) {
            switch (ap) {
                case Parameter p:
                    if (p.distribution.type.equals(DistributionType.FIXED)) {
                        resultColumns.add(new ResultColumn(
                                ResultColumn.KIND.SYNTHETIC,
                                p.name,
                                Arr.makeDoubleArray(nSamples, p.initialGuess),
                                null,
                                maxpostIndex,
                                p));
                    } else {
                        if (headerLookup.containsKey(p.name)) {
                            unmatchedHeaders.remove(p.name);
                            int index = headerLookup.get(p.name);
                            resultColumns.add(new ResultColumn(
                                    ResultColumn.KIND.ESTIMATED,
                                    p.name,
                                    mcmcValues.get(index),
                                    mcmcSummaryValues == null ? null : mcmcSummaryValues.get(index),
                                    maxpostIndex,
                                    p));
                        } else {
                            ConsoleLogger.error(
                                    "Parameter '%s' not found in MCMC results. Should not happen.".formatted(p.name));

                        }
                    }
                    // case VariableParameter vp: // FIXME: to implement
            }
        }

        // retrieve parameters of the structural error models
        for (int outputIndex = 0; outputIndex < calibConfig.modelOutputs.length; outputIndex++) {
            ModelOutput mo = calibConfig.modelOutputs[outputIndex];
            for (Integer gammaIndex = 0; gammaIndex < mo.structuralErrorModel.parameters.length; gammaIndex++) {
                Parameter p = mo.structuralErrorModel.parameters[gammaIndex];
                String gammaName = mo.name + "_" + p.name;
                ParameterSymbol pSymbol = ParameterSymbol.ofIndices("γ", mo.outputIndex + 1, gammaIndex + 1);
                Parameter renamedParamater = p.getRenamedClone(gammaName);
                if (p.distribution.type.equals(DistributionType.FIXED)) {
                    ConsoleLogger.error("Structural error parameter do not support FIX distribution!");
                    continue; // should not happend
                }
                if (headerLookup.containsKey(gammaName)) {
                    unmatchedHeaders.remove(gammaName);
                    int index = headerLookup.get(gammaName);
                    resultColumns.add(new ResultColumn(
                            ResultColumn.KIND.GAMMA,
                            gammaName,
                            mcmcValues.get(index),
                            mcmcSummaryValues == null ? null : mcmcSummaryValues.get(index),
                            maxpostIndex,
                            renamedParamater,
                            pSymbol));
                } else {
                    String msg = "Parameter '%s' not found in MCMC results. Should not happen.".formatted(gammaName);
                    ConsoleLogger.error(msg);
                }
            }
        }

        // unmatched columns are derived parameters
        for (String h : mcmcHeaders) {
            if (!unmatchedHeaders.contains(h)) {
                continue;
            }
            int index = headerLookup.get(h);
            resultColumns.add(new ResultColumn(
                    ResultColumn.KIND.DERIVED,
                    h,
                    mcmcValues.get(index),
                    // compensate lack of LogPost column in summary table
                    mcmcSummaryValues == null ? null : mcmcSummaryValues.get(index - 1),
                    maxpostIndex,
                    null));

        }

        calibrationDataResiduals = readCalibrationDataResiduals(calDatResidulatFilePath,
                calibrationConfig.calibrationData);

    }

    public ResultColumn getResultColumn(String header) {
        return resultColumns.stream()
                .filter(rc -> rc.header.equals(header))
                .findFirst()
                .orElseThrow(() -> {
                    return new NoSuchElementException(
                            String.format("Result column named \"%s\" not found!", header));
                });
    }

    private String[] readMcmcHeaders(Path filePath) {
        String[] headers = new String[] {};
        try {
            headers = ReadFile.getHeaderRow(
                    filePath.toString(),
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    0, false, true);
        } catch (IOException e) {
            ConsoleLogger.error("CalibrationResult Error: Failed to read MCMC headers from file '" +
                    filePath.getFileName() + "'");
            return headers;
        }

        return headers;
    }

    private List<double[]> readMcmcValues(Path filePath) {
        List<double[]> mcmc = new ArrayList<>();
        try {
            mcmc = ReadFile.readMatrix(
                    filePath.toString(),
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    1,
                    Integer.MAX_VALUE,
                    BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                    false, true);

        } catch (IOException e) {
            ConsoleLogger.error("CalibrationResult Error: Failed to read MCMC values from file '" +
                    filePath.getFileName() + "'");
            return mcmc;
        }
        return mcmc;

    }

    private List<double[]> readMcmcSummaryValues(Path filePath) {
        List<double[]> mcmc = null;
        try {
            List<double[]> allColumns = ReadFile.readMatrix(
                    filePath.toString(),
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    1,
                    Integer.MAX_VALUE,
                    BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                    false, true);
            mcmc = allColumns.subList(1, allColumns.size());
        } catch (IOException e) {
            ConsoleLogger.error("CalibrationResult Error: Failed to read MCMC summary values from file '" +
                    filePath.getFileName() + "'");
            return mcmc;
        }
        return mcmc;
    }

    private HashMap<String, Double> readDICValues(Path filePath) {
        HashMap<String, Double> results = new HashMap<>();
        try {
            List<String[]> res = ReadFile.readStringMatrix(filePath.toString(), BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    0,
                    false, true);
            if (res.size() == 2) {
                for (int k = 0; k < res.get(0).length; k++) {
                    try {
                        results.put(
                                res.get(0)[k],
                                Double.parseDouble(res.get(1)[k]));
                    } catch (Exception e) {
                        ConsoleLogger.error(
                                "Value cannot be parsed to double! Returning '" + Double.NaN + "'");
                    }
                }
            } else {
                ConsoleLogger.error("CalibrationResult Error:  DIC results file '" +
                        filePath.getFileName() + "' has an unexpected number of columns");
            }

        } catch (IOException e) {
            ConsoleLogger.error("CalibrationResult Error: Failed to read DIC results file '" +
                    filePath.getFileName() + "'");
        }
        return results;
    }

    private int retrieveMaxPostIndex(double[] logPost) {
        double maxLogPost = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int k = 0; k < logPost.length; k++) {
            if (logPost[k] > maxLogPost) {
                maxLogPost = logPost[k];
                index = k;
            }
        }
        return index;
    }

    private CalibrationDataResiduals readCalibrationDataResiduals(Path filePath, CalibrationData calibrationData) {

        CalibrationDataResiduals calDataResiduals = null;

        try {

            List<double[]> residualMatrix = ReadFile.readMatrix(
                    filePath.toString(),
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    1, Integer.MAX_VALUE,
                    BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                    false, true);

            if (residualMatrix.size() == 0) {
                String[] headerRow = ReadFile.getHeaderRow(filePath.toString(), BamFilesHelpers.BAM_COLUMN_SEPARATOR, 0,
                        false, true);
                residualMatrix = new ArrayList<>();
                for (int k = 0; k < headerRow.length; k++) {
                    residualMatrix.add(new double[0]);
                }
            }

            calDataResiduals = new CalibrationDataResiduals(
                    residualMatrix,
                    calibrationData);

        } catch (IOException e) {
            ConsoleLogger.error("CalibrationResult Error: Failed to read Calibration Data Residuals from file '" +
                    filePath.getFileName() + "'");
            return calDataResiduals;
        }

        return calDataResiduals;
    }

    public void toFiles(String workspace) {
        // calibrationConfig.toFiles(workspace);
        if (mcmcValues != null) {
            Path filePath = Path.of(workspace, calibrationConfig.mcmcCookingConfig.outputFileName);
            try {
                WriteFile.writeMatrix(
                        filePath.toString(),
                        mcmcValues,
                        " ",
                        BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                        mcmcHeaders);
            } catch (IOException e) {
                ConsoleLogger.error(e);
            }
        }

    }

    @Override
    public String toString() {
        String str = "Calibration results: \n";
        if (resultColumns != null) {
            str += "- Result Columns: \n";
            for (ResultColumn rc : resultColumns) {
                str += " > " + rc.toString() + "\n";
            }
        }
        str += " - MaxpostIndex: " + maxpostIndex + "\n";
        if (calibrationDataResiduals != null) {
            str += calibrationDataResiduals.toString();
        }
        return str;
    }

}
