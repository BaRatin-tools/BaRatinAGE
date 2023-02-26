package bam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import bam.utils.ConfigFile;
import bam.utils.Read;

public class CalibrationResult {

    private HashMap<String, EstimatedParameter> estimatedParameter;
    private CalibrationDataResiduals calibrationDataResiduals;

    public CalibrationResult(String workspace, CalibrationConfig calibrationConfig) {

        Path cookedMcmcFilePath = Path.of(workspace, ConfigFile.RESULTS_MCMC_COOKING);
        Path summaryMcmcFilePath = Path.of(workspace, ConfigFile.RESULTS_MCMC_SUMMARY);

        List<double[]> listCookedMcmcResults = null;
        List<double[]> listSummaryMcmcResults = null;
        String[] headers = new String[0];
        String[] headers2 = new String[0];
        try {
            headers = Read.readHeaders(cookedMcmcFilePath.toString());
            headers2 = Read.readHeaders(summaryMcmcFilePath.toString());

            System.out.println("---");
            for (String s : headers)
                System.out.println(s);
            System.out.println("---");
            for (String s : headers2)
                System.out.println(s);
            System.out.println("---");

            listCookedMcmcResults = Read.readMatrix(cookedMcmcFilePath.toString(), 1);
            listSummaryMcmcResults = Read.readMatrix(summaryMcmcFilePath.toString(), "\\s+", 1, 1);
            Read.prettyPrintMatrix(listSummaryMcmcResults);
        } catch (IOException e) {
            System.err.println(e);
        }

        this.estimatedParameter = null; // FIXME: not sure how this case should be handled...
        if (listCookedMcmcResults != null && listSummaryMcmcResults != null) {
            this.estimatedParameter = new HashMap<>();
            if (listSummaryMcmcResults.size() != listCookedMcmcResults.size() - 1) {
                System.err.println("Inconsistent sizes!");
            }

            for (int k = 0; k < headers.length; k++) {
                this.estimatedParameter.put(headers[k], new EstimatedParameter(
                        headers[k],
                        listCookedMcmcResults.get(k),
                        k < listSummaryMcmcResults.size() ? listSummaryMcmcResults.get(k) : null));
                // Here we assume LogPost values are always the last column in the MCMC cooked
                // file.
            }

        }

        this.calibrationDataResiduals = null;
        Path calDataResidualFilePath = Path.of(workspace, ConfigFile.RESULTS_RESIDUALS);
        try {
            String[] headers3 = Read.readHeaders(calDataResidualFilePath.toString());
            System.out.println("---");
            for (String s : headers3)
                System.out.println(s);
            System.out.println("---");

            List<double[]> residualMatrix = Read.readMatrix(calDataResidualFilePath.toString(), 1);

            this.calibrationDataResiduals = new CalibrationDataResiduals(
                    residualMatrix,
                    calibrationConfig.getCalibrationData());

        } catch (IOException e) {
            System.err.println(e);
        }

    }

    public HashMap<String, EstimatedParameter> getEsimatedParameters() {
        return this.estimatedParameter;
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
