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

    public BaMconfigFile(String fileName, String workspace) {
        filePath = Path.of(workspace, fileName);
        values = new String[0];
        comments = new String[0];
    }

    static private String[] addItemToArray(String[] arr, String item) {
        String[] newArr = new String[arr.length + 1];
        for (int k = 0; k < arr.length; k++) {
            newArr[k] = arr[k];
        }
        newArr[arr.length] = item;
        return newArr;
    }

    public void addItem(String value, String comment) {
        values = addItemToArray(values, value);
        comments = addItemToArray(comments, comment);
    }

    public void addItem(String value, String comment, Boolean quoted) {
        String v = String.format("'%s'", value);
        addItem(v, comment);
    }

    public void addItem(Boolean value, String comment) {
        String v = value ? ".true." : ".false.";
        addItem(v, comment);
    }

    public void addItem(int value, String comment) {
        String v = Integer.toString(value);
        addItem(v, comment);
    }

    public void addItem(Double value, String comment) {
        String v = Double.toString(value);
        addItem(v, comment);
    }

    public void addItem(String value) {
        addItem(value, "");
    }

    public void addItem(String value, Boolean quoted) {
        addItem(value, "", quoted);
    }

    public void addItem(Boolean value) {
        addItem(value, "");
    }

    public void addItem(int value) {
        addItem(value, "");
    }

    public void addItem(Double value) {
        addItem(value, "");
    }

    // FIXME: should be private
    public String[] createFileLines() {
        int maxValueLength = 0;
        int nItems = values.length;
        for (int k = 0; k < nItems; k++) {
            if (values[k].length() > maxValueLength) {
                maxValueLength = values[k].length();
            }
        }
        String[] lines = new String[nItems];
        for (int k = 0; k < nItems; k++) {
            if (comments[k] != "") {
                int nSpaces = maxValueLength - values[k].length();
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
}

// class ValueCommentPair {
// private String _value;
// private String _comment;
// ValueCommentPair(String value, String comment) {
// _value = value;
// _comment = comment;
// }

// }