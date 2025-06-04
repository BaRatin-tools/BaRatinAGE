package org.baratinage.jbam.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;

public class ConfigFile {

    private static final String COMMENT_SEPARATOR = "  ! ";

    private record ValueCommentPair(String value, String comment) {
    }

    private List<ValueCommentPair> items;

    public ConfigFile() {
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        String[] lines = createFileLines();
        String str = String.join("\n", lines);
        return str;
    }

    public String[] createFileLines() {
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
            WriteFile.writeLines(filePath, lines);
        } catch (IOException e) {
            ConsoleLogger.error(
                    String.format("ConfigFile Error: Failed to write configuration \n '%s'... \n%e",
                            filePath.toString(), e.toString()));
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
        Path configFilePath = Path.of(filePathFirst, filePathMore);
        String content = "";
        try {
            byte[] bytes = Files.readAllBytes(configFilePath);
            content = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ConsoleLogger.error(e);
            return null;
        }
        return parseConfigFileString(content);
    }

    public static ConfigFile parseConfigFileString(String content) {
        ConfigFile configFile = new ConfigFile();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String[] splittedLine = line.split("! ");
            if (splittedLine.length > 1) {
                configFile.items.add(
                        new ValueCommentPair(
                                splittedLine[0].trim(),
                                splittedLine[1].trim()));
            } else {
                ConsoleLogger.log("Ignoring line '" + line + "' ...");
            }

        }
        return configFile;
    }

    public int getNumberOfItems() {
        return items.size();
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
        return item.value.equals("") ? new String[0] : splitString(item.value);
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
            ConsoleLogger.error(
                    "ConfigFile Error: Value is not one of '.true.' or '.false.' as expected! Returning false.");
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
        int intValue = -9999; // FIXME: should not have it hardcoded
        try {
            intValue = Integer.parseInt(value);
        } catch (Exception e) {
            ConsoleLogger.error(
                    "Value cannot be parsed to integer! Returning '" + "-9999" + "'");
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
        double doubleValue = Double.NaN;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (Exception e) {
            ConsoleLogger.error(
                    "Value cannot be parsed to double! Returning '" + Double.NaN + "'");
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
