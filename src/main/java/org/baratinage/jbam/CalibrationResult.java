package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class CalibrationResult {

    public final CalibrationConfig calibrationConfig;
    public final List<double[]> mcmcValues;
    public final String[] mcmcHeaders;
    public final List<EstimatedParameter> estimatedParameters;
    public final CalibrationDataResiduals calibrationDataResiduals;
    public final int maxpostIndex;
    public final HashMap<String, Double> DIC;

    public CalibrationResult(String workspace, CalibrationConfig calibConfig, RunOptions runOptions) {

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
            estimatedParameters = null;
            calibrationDataResiduals = null;
            return;
        }

        maxpostIndex = retrieveMaxPostIndex(mcmcValues.get(logPostColIndex));

        List<double[]> mcmcSummaryValues = readMcmcSummaryValues(summaryMcmcFilePath);

        Parameter[] allParameterConfigs = calibConfig.model.parameters;
        for (ModelOutput mo : calibConfig.modelOutputs) {
            allParameterConfigs = Stream
                    .concat(
                            Arrays.stream(allParameterConfigs),
                            Arrays.stream(mo.structuralErrorModel.parameters).map(p -> {
                                return new Parameter(
                                        mo.name + "_" + p.name,
                                        p.initalGuess,
                                        p.distribution);
                            }))
                    .toArray(Parameter[]::new);
        }

        estimatedParameters = buildEstimatedParameters(
                mcmcHeaders,
                mcmcValues,
                mcmcSummaryValues,
                maxpostIndex,
                allParameterConfigs);

        calibrationDataResiduals = readCalibrationDataResiduals(calDatResidulatFilePath,
                calibrationConfig.calibrationData);

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

    private List<EstimatedParameter> buildEstimatedParameters(
            String[] headers, List<double[]> mcmc, List<double[]> mcmcSummary, int maxpostIndex,
            Parameter[] parameters) {

        if (mcmcSummary != null && mcmcSummary.size() != mcmc.size() - 1) {
            ConsoleLogger.error(
                    "CalibrationResult Error: Inconsistent sizes between MCMC matrix and MCMC summary matrix!");
        }

        List<EstimatedParameter> estimatedParameters = new ArrayList<>();

        for (int k = 0; k < headers.length; k++) {
            double[] summary = mcmcSummary == null || k >= mcmcSummary.size() ? null : mcmcSummary.get(k);
            Parameter parameterConfig = null;
            for (Parameter p : parameters) {
                if (p.name.equals(headers[k])) {
                    parameterConfig = p;
                    break;
                }
            }
            estimatedParameters.add(new EstimatedParameter(
                    headers[k],
                    mcmc.get(k),
                    summary,
                    maxpostIndex,
                    parameterConfig));

        }

        return estimatedParameters;

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
        if (estimatedParameters != null) {
            str += "- Estimated Parameters: \n";
            for (EstimatedParameter p : estimatedParameters) {
                str += "   > " + p.toString() + "\n";
            }
        }
        str += " - MaxpostIndex: " + maxpostIndex + "\n";
        if (calibrationDataResiduals != null) {
            str += calibrationDataResiduals.toString();
        }
        return str;
    }

}
