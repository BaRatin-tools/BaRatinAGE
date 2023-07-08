package org.baratinage.jbam.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

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

    private static final String COMMENT_SEPARATOR = "  ! ";

    private record ValueCommentPair(String value, String comment) {
    }

    private List<ValueCommentPair> items;

    public ConfigFile() {
        items = new ArrayList<>();
    }

    private String[] createFileLines() {
        final int maxSpaces = 50;
        final int minSpaces = 0;
        int maxValueLength = minSpaces;
        int nItems = items.size();
        for (int k = 0; k < nItems; k++) {
            ValueCommentPair item = items.get(k);
            if (item.value().length() > maxValueLength - minSpaces) {
                maxValueLength = item.value().length() + minSpaces;
                if (maxValueLength >= maxSpaces) {
                    maxValueLength = maxSpaces;
                    break;
                }
            }
        }
        String[] lines = new String[nItems];
        for (int k = 0; k < nItems; k++) {
            ValueCommentPair item = items.get(k);
            if (item.comment() != "") {
                int nSpaces = maxValueLength - item.value().length();
                nSpaces = nSpaces <= minSpaces ? minSpaces : nSpaces;
                lines[k] = String.format("%s%s%s%s",
                        item.value(),
                        " ".repeat(nSpaces),
                        COMMENT_SEPARATOR,
                        item.comment());
            } else {
                lines[k] = item.value();
            }
        }
        return lines;
    }

    public void writeToFile(String filePathFirst, String... filePathMore) {
        this.writeToFile(Path.of(filePathFirst, filePathMore));
    }

    public void writeToFile(String filePath) {
        this.writeToFile(Path.of(filePath));
    }

    public void writeToFile(Path filePath) {
        String[] lines = this.createFileLines();
        try {
            Write.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println(String.format("Failed to write configuration \n '%s'...", filePath.toString()));
            e.printStackTrace();
        }
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

    static private String[] splitString(String str) {
        String[] splittedStr = str.split(", ");
        int n = splittedStr.length;
        String[] strArray = new String[n];
        for (int k = 0; k < n; k++) {
            strArray[k] = unquoteString(splittedStr[k]);
        }
        return strArray;
    }

    static private String quoteString(String str) {
        return String.format("\"%s\"", str);
    }

    static private String unquoteString(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) ? str.substring(1, str.length() - 1) : str;
    }

    public static ConfigFile readConfigFile(String filePathFirst, String... filePathMore) {
        String configFilePath = Path.of(filePathFirst, filePathMore).toString();
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = Read.createBufferedReader(configFilePath, false);
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ConfigFile configFile = new ConfigFile();
        for (String line : lines) {
            String[] splittedLine = line.split("!");
            if (splittedLine.length == 2) {
                configFile.items.add(
                        new ValueCommentPair(
                                splittedLine[0].trim(),
                                splittedLine[1].trim()));
            } else {
                System.out.println("Ignoring line '" + line + "' ...");
            }

        }
        return configFile;
    }

    // --------------------------------------------------------------
    // String
    // --------------------------------------------------------------

    public void addItem(String value, String comment) {
        items.add(new ValueCommentPair(value, comment));
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

    public String getString(int index) {
        ValueCommentPair item = items.get(index);
        return unquoteString(item.value);
    }

    public String[] getStringArray(int index) {
        ValueCommentPair item = items.get(index);
        return splitString(item.value);
    }

    // --------------------------------------------------------------
    // Boolean
    // --------------------------------------------------------------

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

    private static boolean toBoolean(String value) {
        if (value.equals(".true.")) {
            return true;
        } else if (value.equals(".false.")) {
            return false;
        } else {
            System.err.println("Value is not one of '.true.' or '.false.' as expected! Returning false.");
            return false;
        }
    }

    public boolean getBoolean(int index) {
        ValueCommentPair item = items.get(index);
        return toBoolean(item.value);
    }

    public boolean[] getBooleanArray(int index) {
        String[] strArray = getStringArray(index);
        int n = strArray.length;
        boolean[] booleanArray = new boolean[n];
        for (int k = 0; k < n; k++) {
            booleanArray[k] = toBoolean(strArray[k]);
        }
        return booleanArray;
    }

    // --------------------------------------------------------------
    // int
    // --------------------------------------------------------------

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

    private static int toInt(String value) {
        int intValue = -9999;
        try {
            intValue = Integer.parseInt(value);
        } catch (Exception e) {
            System.err.println("Value cannot be parsed to integer! Returning -9999.");
        }
        return intValue;
    }

    public int getInt(int index) {
        ValueCommentPair item = items.get(index);
        return toInt(item.value);
    }

    public int[] getIntArray(int index) {
        String[] strArray = getStringArray(index);
        int n = strArray.length;
        int[] intArray = new int[n];
        for (int k = 0; k < n; k++) {
            intArray[k] = toInt(strArray[k]);
        }
        return intArray;
    }

    // --------------------------------------------------------------
    // double
    // --------------------------------------------------------------

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

    private static double toDouble(String value) {
        double doubleValue = -9999;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (Exception e) {
            System.err.println("Value cannot be parsed to double! Returning -9999.");
        }
        return doubleValue;
    }

    public double getDouble(int index) {
        ValueCommentPair item = items.get(index);
        return toDouble(item.value);
    }

    public double[] getDoubleArray(int index) {
        String[] strArray = getStringArray(index);
        int n = strArray.length;
        double[] doubleArray = new double[n];
        for (int k = 0; k < n; k++) {
            doubleArray[k] = toDouble(strArray[k]);
        }
        return doubleArray;
    }

}
