package org.baratinage.jbam.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BamFilesHelpers {

    public static final String OS_SEP = System.getProperty("file.separator");
    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final String EXE_DIR = "./exe/";
    public static final String EXE_NAME = "BaM";

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

    // FIXME: unused, delete?
    public static List<String> parseString(String template, String stringToParse) {
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

    static public String findDataFilePath(String rawFilePath, String workspacePath) {
        Path p = Path.of(rawFilePath);
        if (p.toFile().exists()) { // assume it is absolute (or relative to baratinage instance)
            return p.toAbsolutePath().toString();
        }
        String fileName = p.getFileName().toString();
        p = Path.of(workspacePath, fileName);
        if (p.toFile().exists()) { // assume it is relative to workspace
            return p.toAbsolutePath().toString();
        }
        return null;
    }
}
