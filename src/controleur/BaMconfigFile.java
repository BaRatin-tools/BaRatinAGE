package controleur;

import java.io.IOException;
import java.nio.file.Path;

import Utils.FileReadWrite;

public class BaMconfigFile {
    // private String fileName;
    // private String workspace;
    private static final String commentSeparator = "  ! "; // FIXME: to be defined in Default class
    private Path filePath;
    private String[] values;
    private String[] comments;

    public BaMconfigFile(String configFilePath) {
        filePath = Path.of(configFilePath);
        values = new String[0];
        comments = new String[0];
    }

    public BaMconfigFile(String workspacePath, String configFileName) {
        filePath = Path.of(workspacePath, configFileName);
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

    public void writeToFile() throws IOException {
        String[] lines = createFileLines();
        FileReadWrite.writeLines(filePath, lines);
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

    public void addItem(Double value, String comment) {
        String v = Double.toString(value);
        addItem(v, comment);
    }

    public void addItem(Double[] value, String comment) {
        String[] strValue = new String[value.length];
        for (int k = 0; k < value.length; k++) {
            strValue[k] = Double.toString(value[k]);
        }
        addItem(strValue, comment);
    }

    public void addItem(Double value) {
        addItem(value, "");
    }

}

// class ValueCommentPair {
// private String _value;
// private String _comment;
// ValueCommentPair(String value, String comment) {
// _value = value;
// _comment = comment;
// }

// }