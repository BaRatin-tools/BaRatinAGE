package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.Read;
import org.baratinage.jbam.utils.Write;

public class CalibrationResult {

    // FIXME: using a HashMap may not be required since the parameters order
    // FIXME: are set during configuration
    // FIXME: (+ a list is more consistent with configuration approach)

    public final CalibrationConfig calibrationConfig;
    public final List<double[]> mcmcValues;
    public final String[] mcmcHeaders;
    public final HashMap<String, EstimatedParameter> estimatedParameters;
    public final CalibrationDataResiduals calibrationDataResiduals;
    public final int maxpostIndex;

    public CalibrationResult(String workspace, CalibrationConfig calibConfig, RunOptions runOptions) {

        calibrationConfig = calibConfig;

        Path cookedMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcCookingConfig.outputFileName);
        Path summaryMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcSummaryConfig.outputFileName);
        Path calDatResidulatFilePath = Path.of(workspace, calibrationConfig.calDataResidualConfig.outputFileName);

        mcmcValues = readMcmcValues(cookedMcmcFilePath);
        mcmcHeaders = readMcmcHeaders(cookedMcmcFilePath);
        List<double[]> mcmcSummaryValues = readMcmcSummaryValues(summaryMcmcFilePath);

        estimatedParameters = buildEstimatedParameters(mcmcHeaders, mcmcValues, mcmcSummaryValues);

        calibrationDataResiduals = readCalibrationDataResiduals(calDatResidulatFilePath,
                calibrationConfig.calibrationData);

        maxpostIndex = retrieveMaxPostIndex(estimatedParameters.get("LogPost"));

    }

    private String[] readMcmcHeaders(Path filePath) {
        String[] headers = new String[] {};
        try {
            headers = Read.readHeaders(filePath.toString());

        } catch (IOException e) {
            System.err.println("CalibrationResult Error: Failed to read MCMC headers from file '" +
                    filePath.getFileName() + "'");
            return headers;
        }

        return headers;
    }

    private List<double[]> readMcmcValues(Path filePath) {
        List<double[]> mcmc = new ArrayList<>();
        try {
            mcmc = Read.readMatrix(filePath.toString(), 1);

        } catch (IOException e) {
            System.err.println("CalibrationResult Error: Failed to read MCMC values from file '" +
                    filePath.getFileName() + "'");
            return mcmc;
        }
        return mcmc;

    }

    private List<double[]> readMcmcSummaryValues(Path filePath) {
        List<double[]> mcmc = null;
        try {
            mcmc = Read.readMatrix(filePath.toString(), "\\s+", 1, 1);

        } catch (IOException e) {
            System.err.println("CalibrationResult Error: Failed to read MCMC summary values from file '" +
                    filePath.getFileName() + "'");
            return mcmc;
        }
        return mcmc;

    }

    private HashMap<String, EstimatedParameter> buildEstimatedParameters(
            String[] headers, List<double[]> mcmc, List<double[]> mcmcSummary) {

        if (mcmcSummary != null && mcmcSummary.size() != mcmc.size() - 1) {
            System.err.println(
                    "CalibrationResult Error: Inconsistent sizes between MCMC matrix and MCMC summary matrix!");
        }

        HashMap<String, EstimatedParameter> estimatedParameters = new HashMap<>();

        for (int k = 0; k < headers.length; k++) {
            double[] summary = mcmcSummary == null || k >= mcmcSummary.size() ? null : mcmcSummary.get(k);
            estimatedParameters.put(headers[k], new EstimatedParameter(
                    headers[k],
                    mcmc.get(k),
                    summary));

        }

        return estimatedParameters;

    }

    private int retrieveMaxPostIndex(EstimatedParameter logPostPar) {

        if (logPostPar == null) {
            System.err.println("CalibrationResult Error: No 'LogPost' column found in MCMC cooked file!");
            return -1;
        }
        double[] logPost = logPostPar.mcmc;
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

            List<double[]> residualMatrix = Read.readMatrix(filePath.toString(), 1);

            calDataResiduals = new CalibrationDataResiduals(
                    residualMatrix,
                    calibrationData);

        } catch (IOException e) {
            System.err.println("CalibrationResult Error: Failed to read Calibration Data Residuals from file '" +
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
                Write.writeMatrix(
                        filePath.toString(),
                        mcmcValues,
                        " ",
                        BamFilesHelpers.BAM_MISSING_VALUE_CODE,
                        mcmcHeaders);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String toString() {
        String str = "Calibration results: \n";
        if (estimatedParameters != null) {
            str += "- Estimated Parameters: \n";
            for (EstimatedParameter p : estimatedParameters.values()) {
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
