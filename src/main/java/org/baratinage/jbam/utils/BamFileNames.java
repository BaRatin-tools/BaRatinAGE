package org.baratinage.jbam.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BamFileNames {
    public static final String DATA_CALIBRATION = "Data_%s.txt";
    public static final String DATA_PREDICTION = "Data_%s.txt";

    public static final String CONFIG_BAM = "Config_BaM.txt";
    public static final String CONFIG_CALIBRATION = "Config_Data.txt";
    public static final String CONFIG_RESIDUALS = "Config_Data_Residuals.txt";
    public static final String CONFIG_MCMC = "Config_MCMC.txt";
    public static final String CONFIG_MCMC_COOKING = "Config_MCMC_Cooking.txt";
    public static final String CONFIG_MCMC_SUMMARY = "Config_MCMC_Summary.txt";
    public static final String CONFIG_MODEL = "Config_Model.txt";
    public static final String CONFIG_XTRA = "Config_xTra.txt";
    public static final String CONFIG_RUN_OPTIONS = "Config_RunOptions.txt";
    public static final String CONFIG_STRUCTURAL_ERRORS = "Config_StructuralError_%s.txt";
    public static final String CONFIG_PREDICTION = "Config_Pred_%s.txt";
    public static final String CONFIG_PREDICTION_MASTER = "Config_Prediction_Master.txt";

    public static final String RESULTS_RESIDUALS = "Results_Residuals.txt";
    public static final String RESULTS_MCMC = "Results_MCMC.txt";
    public static final String RESULTS_MCMC_COOKING = "Results_MCMC_Cooked.txt";
    public static final String RESULTS_MCMC_SUMMARY = "Results_Summary.txt";
    public static final String RESULTS_OUTPUT_SPAG = "output_%s_%s.spag";
    public static final String RESULTS_OUTPUT_ENV = "output_%s_%s.env";
    public static final String RESULTS_STATE_SPAG = "state_%s_%s.spag";
    public static final String RESULTS_STATE_ENV = "state_%s_%s.env";

    public static String buildPredictionDataFileName(String predictionName) {
        return String.format(DATA_PREDICTION, predictionName);
    }

    public static String getPredictionNameFromInputDataFileName(String predictionDataFileName) {
        List<String> parsed = parseString(DATA_PREDICTION, predictionDataFileName);
        return parsed.size() == 1 ? parsed.get(0) : null;
    }

    public static String buildPredictionConfigFileName(String predictionName) {
        return String.format(CONFIG_PREDICTION, predictionName);
    }

    public static String getPredictionName(String predictionDataFileName) {
        List<String> parsed = parseString(DATA_PREDICTION, predictionDataFileName);
        return parsed.size() == 1 ? parsed.get(0) : null;
    }

    public static String buildSpagOutputFileName(String predictionName, String outputName) {
        return String.format(RESULTS_OUTPUT_SPAG, predictionName, outputName);
    }

    public static String buildEnvOutputFileName(String predictionName, String outputName) {
        return String.format(RESULTS_OUTPUT_ENV, predictionName, outputName);
    }

    public static String buildSpagStateFileName(String predictionName, String stateName) {
        return String.format(RESULTS_STATE_SPAG, predictionName, stateName);
    }

    public static String buildEnvStateFileName(String predictionName, String stateName) {
        return String.format(RESULTS_STATE_ENV, predictionName, stateName);
    }

    public static String getOutputNameFromSpagResultFile(String spagOutputFileName) {
        List<String> parsed = parseString(RESULTS_OUTPUT_SPAG, spagOutputFileName);
        return parsed.size() == 2 ? parsed.get(1) : null;
    }

    public static String getStateNameFromSpagResultFile(String spagStateFileName) {
        List<String> parsed = parseString(RESULTS_STATE_SPAG, spagStateFileName);
        return parsed.size() == 2 ? parsed.get(1) : null;
    }

    private static List<String> parseString(String template, String stringToParse) {
        String regex = template.replaceAll("%s", "(\\\\w*)");
        Matcher m = Pattern.compile(regex).matcher(stringToParse);
        List<String> output = new ArrayList<>();
        while (m.find()) {
            for (int k = 0; k < m.groupCount(); k++) {
                output.add(m.group(k + 1));
            }
        }
        return output;
    }

}
