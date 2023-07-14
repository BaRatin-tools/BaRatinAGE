package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.baratinage.jbam.utils.Read;

public class CalibrationResult {

    // FIXME: using a HashMap may not be required since the parameters order
    // FIXME: are set during configuration
    // FIXME: (+ a list is more consistent with configuration approach)
    private HashMap<String, EstimatedParameter> estimatedParameter;
    private CalibrationDataResiduals calibrationDataResiduals;
    private int maxPostIndex;
    private boolean isValid;

    // FIXME: should there be an instance variable for 'calibrationConfig' ?

    public CalibrationResult(String workspace, CalibrationConfig calibrationConfig) {
        this.isValid = false;

        Path cookedMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcCookingConfig.outputFileName);
        Path summaryMcmcFilePath = Path.of(workspace, calibrationConfig.mcmcSummaryConfig.outputFileName);

        List<double[]> listCookedMcmcResults = null;
        List<double[]> listSummaryMcmcResults = null;
        String[] headers = new String[0];
        try {
            headers = Read.readHeaders(cookedMcmcFilePath.toString());
            listCookedMcmcResults = Read.readMatrix(cookedMcmcFilePath.toString(), 1);
            listSummaryMcmcResults = Read.readMatrix(summaryMcmcFilePath.toString(), "\\s+", 1, 1);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }

        // FIXME: identify all parameters by name!!

        this.estimatedParameter = null; // FIXME: not sure how this case should be handled...
        if (listCookedMcmcResults != null && listSummaryMcmcResults != null) {
            this.estimatedParameter = new HashMap<>();
            if (listSummaryMcmcResults.size() != listCookedMcmcResults.size() - 1) {
                System.err.println("Inconsistent sizes!");
                return;
            }

            // FIXME: should I filter out LogPost and derived values?
            for (int k = 0; k < headers.length; k++) {
                this.estimatedParameter.put(headers[k], new EstimatedParameter(
                        headers[k],
                        listCookedMcmcResults.get(k),
                        k < listSummaryMcmcResults.size() ? listSummaryMcmcResults.get(k) : null));

            }
            EstimatedParameter logPostPar = this.estimatedParameter.get("LogPost");
            if (logPostPar == null) {
                System.err.println("No 'LogPost' column found in MCMC cooked file!");
                return;
            }
            double[] logPost = logPostPar.getMcmc();
            double maxLogPost = Double.NEGATIVE_INFINITY;
            maxPostIndex = -1;
            for (int k = 0; k < logPost.length; k++) {
                if (logPost[k] > maxLogPost) {
                    maxLogPost = logPost[k];
                    maxPostIndex = k;
                }
            }

        }

        this.calibrationDataResiduals = null;
        Path calDataResidualFilePath = Path.of(workspace, calibrationConfig.calDataResidualConfig.outputFileName);
        try {

            List<double[]> residualMatrix = Read.readMatrix(calDataResidualFilePath.toString(), 1);

            this.calibrationDataResiduals = new CalibrationDataResiduals(
                    residualMatrix,
                    calibrationConfig.getCalibrationData());

        } catch (IOException e) {
            System.err.println(e);
            return;
        }

        this.isValid = true;
    }

    public HashMap<String, EstimatedParameter> getEsimatedParameters() {
        return this.estimatedParameter;
    }

    public int getMaxPostIndex() {
        return this.maxPostIndex;
    }

    public boolean getIsValid() {
        return isValid;
    }

    @Override
    public String toString() {
        String str = "Calibration results: \n";
        if (this.estimatedParameter != null) {
            str += "- Estimated Parameters: \n";
            for (EstimatedParameter p : this.estimatedParameter.values()) {
                str += "   > " + p.toString() + "\n";
            }
        }
        if (this.calibrationDataResiduals != null) {
            str += this.calibrationDataResiduals.toString();
        }
        return str;
    }

}
