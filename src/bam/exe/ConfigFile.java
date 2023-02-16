package bam.exe;

import java.io.IOException;
// import java.nio.file.Path;
// import java.nio.file.Path;
import java.nio.file.Path;

import utils.FileReadWrite;

public class ConfigFile {

    public static final String DATA_CALIBRATION = "Data_%s.txt";
    public static final String DATA_PREDICTION = "Data_%s.txt";

    // public static final String CONFIG_CALIBRATION = "Config_Data_%s.txt";
    public static final String CONFIG_CALIBRATION = "Config_Data.txt";
    public static final String CONFIG_RESIDUALS = "Config_Residuals.txt";
    public static final String CONFIG_MCMC = "Config_MCMC.txt";
    public static final String CONFIG_MCMC_COOKING = "Config_MCMC_Cooking.txt";
    public static final String CONFIG_MCMC_SUMMARY = "Results_Summary.txt";
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
    public static final String RESULTS_OUTPUT_ENV = "output_%s_%s.ENV";
    public static final String RESULTS_STATE_SPAG = "state_%s_%s.spag";
    public static final String RESULTS_STATE_ENV = "state_%s_%s.ENV";

    private static final String commentSeparator = "  ! "; // FIXME: to be defined in Default class

    private String[] values;
    private String[] comments;

    public ConfigFile() {
        values = new String[0];
        comments = new String[0];
    }

    private String[] createFileLines() {
        final int maxSpaces = 50;
        final int minSpaces = 0;
        int maxValueLength = minSpaces;
        int nItems = values.length;
        for (int k = 0; k < nItems; k++) {
            if (values[k].length() > maxValueLength - minSpaces) {
                maxValueLength = values[k].length() + minSpaces;
                if (maxValueLength >= maxSpaces) {
                    maxValueLength = maxSpaces;
                    break;
                }
            }
        }
        String[] lines = new String[nItems];
        for (int k = 0; k < nItems; k++) {
            if (comments[k] != "") {
                int nSpaces = maxValueLength - values[k].length();
                nSpaces = nSpaces <= minSpaces ? minSpaces : nSpaces;
                lines[k] = String.format("%s%s%s%s", values[k], " ".repeat(nSpaces), commentSeparator, comments[k]);
            } else {
                lines[k] = values[k];
            }
        }
        return lines;
    }

    public void writeToFile(String filePathFirst, String... filePathMore) {
        String[] lines = createFileLines();
        Path filePath = Path.of(filePathFirst, filePathMore);
        try {
            FileReadWrite.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println(String.format("Failed to write configuration \n '%s'...", filePath.toString()));
            e.printStackTrace();
        }
    }

    public void writeToFile(String filePath) {
        String[] lines = createFileLines();
        try {
            FileReadWrite.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println(String.format("Failed to write configuration \n '%s'...", filePath));
            e.printStackTrace();
        }
    }

    public void writeToFile(Path filePath) {
        String[] lines = createFileLines();
        try {
            FileReadWrite.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println(String.format("Failed to write configuration \n '%s'...", filePath.toString()));
            e.printStackTrace();
        }
    }

    static private String[] addItemToArray(String[] arr, String item) {
        String[] newArr = new String[arr.length + 1];
        for (int k = 0; k < arr.length; k++) {
            newArr[k] = arr[k];
        }
        newArr[arr.length] = item;
        return newArr;
    }

    static private String mergeStrings(String[] arr, boolean quoted) {
        String output = "";
        if (quoted) {
            for (int k = 0; k < arr.length; k++) {
                output = String.format("%s, %s", output, quoteString(arr[k]));
            }
        } else {
            for (int k = 0; k < arr.length; k++) {
                output = String.format("%s, %s", output, arr[k]);
            }
        }
        if (output.length() > 2) {
            output = output.substring(2);
        }
        return output;
    }

    static private String quoteString(String str) {
        return String.format("\"%s\"", str);
    }

    // --------------------------------------------------------------
    // String

    public void addItem(String value, String comment) {
        values = addItemToArray(values, value);
        comments = addItemToArray(comments, comment);
    }

    public void addItem(String value, String comment, Boolean quoted) {
        String v = quoteString(value);
        addItem(v, comment);
    }

    public void addItem(String value) {
        addItem(value, "");
    }

    public void addItem(String value, Boolean quoted) {
        addItem(value, "", quoted);
    }

    public void addItem(String[] value, String comment, Boolean quoted) {
        String v = mergeStrings(value, quoted);
        addItem(v, comment);
    }

    public void addItem(String[] value, String comment) {
        addItem(value, comment, false);
    }

    // --------------------------------------------------------------
    // Boolean

    public void addItem(boolean value, String comment) {
        String v = value ? ".true." : ".false.";
        addItem(v, comment);
    }

    public void addItem(boolean value) {
        addItem(value, "");
    }

    public void addItem(boolean[] value, String comment) {
        String[] strValue = new String[value.length];
        for (int k = 0; k < value.length; k++) {
            strValue[k] = value[k] ? ".true." : ".false.";
        }
        addItem(strValue, comment);
    }

    public void addItem(boolean[] value) {
        addItem(value, "");
    }

    // --------------------------------------------------------------
    // int

    public void addItem(int value, String comment) {
        String v = Integer.toString(value);
        addItem(v, comment);
    }

    public void addItem(int value) {
        addItem(value, "");
    }

    public void addItem(int[] value, String comment) {
        String[] strValue = new String[value.length];
        for (int k = 0; k < value.length; k++) {
            strValue[k] = Integer.toString(value[k]);
        }
        addItem(strValue, comment);
    }

    public void addItem(int[] value) {
        addItem(value, "");
    }

    // --------------------------------------------------------------
    // Double

    public void addItem(double value, String comment) {
        String v = Double.toString(value);
        addItem(v, comment);
    }

    public void addItem(double[] value, String comment) {
        String[] strValue = new String[value.length];
        for (int k = 0; k < value.length; k++) {
            strValue[k] = Double.toString(value[k]);
        }
        addItem(strValue, comment);
    }

    public void addItem(double value) {
        addItem(value, "");
    }

}
