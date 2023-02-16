package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import org.mozilla.universalchardet.UniversalDetector;
import java.util.Scanner;

public class FileReadWrite {

    /**
     * Create a Scanner object from a text file path.
     * 
     * Note that this function does not throw any errors. If any errors occurs while
     * trying to read the files a scanner containing an empty string is returned.
     * However the stack trace will still be printed to the console. This behavior
     * might not be desirable.
     * 
     * @param textFilePath full file path
     * @return Scanner object, may contain an empty string
     *         in case the scanner enconters an error while reading the file
     */
    static private Scanner createScanner(String textFilePath, boolean detectCharset) {

        File file = new File(textFilePath);

        String encoding = StandardCharsets.UTF_8.toString();
        if (detectCharset) {
            try {
                encoding = UniversalDetector.detectCharset(file);
                System.out.println(String.format("Encoding is '%s'", encoding));
            } catch (IOException e) {
                System.err.println("Exception caught: ");
                e.printStackTrace();
                encoding = StandardCharsets.UTF_8.toString();
            }
        }

        Scanner scanner;
        try {
            scanner = new Scanner(file, encoding);
        } catch (IOException e) {
            System.err.println("Exception caught: ");
            e.printStackTrace();
            return new Scanner("");
        }

        return scanner;
    }

    /**
     * Create a string array containing the lines of a text file
     * 
     * @param textFilePath  full file path
     * @param detectCharset whether it should auto-detect the file encoding
     * @return an array of string corresponding to the lines of a text file
     */
    static public String[] readLines(String textFilePath, boolean detectCharset) {
        return readLines(textFilePath, Integer.MAX_VALUE, detectCharset);
    }

    /**
     * 
     * @param textFilePath  full file path to read
     * @param nMax          the maximum number of line to read
     * @param detectCharset whether it should auto-detect the file encoding
     * @return an array of string corresponding to the lines of a text file
     */
    static public String[] readLines(String textFilePath, int nMax, boolean detectCharset) {
        Scanner scanner = FileReadWrite.createScanner(textFilePath, detectCharset);
        ArrayList<String> lines = new ArrayList<String>();
        int n = 0;
        while (scanner.hasNextLine() && n < nMax) {
            lines.add(scanner.nextLine());
            n++;
        }
        scanner.close();
        String[] stringLines = lines.toArray(new String[0]);
        return stringLines;
    }

    /**
     * Parse a string into an array of string given a separator and whether
     * the input string should be trimmed
     * 
     * @param str  string to parse
     * @param sep  separator (can be a regex e.g. "\s+" for unknown number of
     *             spaces)
     * @param trim should be trimmed (i.e. remove heading/trailing spaces)?
     * @return an array string of items found within the string
     */
    static private String[] parseString(String str, String sep, boolean trim) {
        if (trim) {
            return str.trim().split(sep);
        } else {
            return str.split(sep);
        }
    }

    /**
     * Read a file containing float numbers (double) in a matrix format
     * 
     * @param textFilePath  file path
     * @param sep           separator (can be a regex e.g. "\s+" for unknown number
     *                      of
     * @param nHeah         number of header row to skip
     * @param skipCol       number of columns to skip
     * @param trim          whether rows should be trimmed to remove
     *                      heading/trailing spaces
     * @param detectCharset whether the file encoding should be detected (default is
     *                      using UTF_8)
     * @return A double matrix of size [Rows][Columns]
     * @throws Exception Custom exceptions occuring when trying to parse the rows
     *                   (e.g. if the number of columns found is not consistent)
     */
    static public double[][] readMatrix(String textFilePath, String sep, int nHeah, String missingValueString,
            int skipCol, boolean trim,
            boolean detectCharset)
            throws Exception {
        String[] rows = FileReadWrite.readLines(textFilePath, detectCharset);
        int nRow = rows.length;
        if (nRow <= nHeah) {
            String errMsg = "Cannot read matrix: the files contains no (or not enough) rows.";
            System.err.println(errMsg);
            throw new Exception(errMsg);
        }
        int nCol = 0;
        try {
            nCol = FileReadWrite.parseString(rows[nHeah], sep, trim).length;
            if (nCol <= skipCol) {
                String errMsg = "Cannot read matrix: the files contains no (or not enough) columns.";
                System.err.println(errMsg);
                throw new Exception(errMsg);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Cannot parse first line:\nLine: %s\nSep: %s\nError: %s",
                    rows[0], sep, e.toString());
            System.err.println(errorMessage);
            throw new Exception(e);
        }
        double[][] itemsPerRow = new double[nRow - nHeah][nCol - skipCol];
        int currentRowIndex = 0;
        try {

            for (int rowIndex = nHeah; rowIndex < nRow; rowIndex++) {
                currentRowIndex = rowIndex;
                String[] currentRow = FileReadWrite.parseString(rows[rowIndex], sep, trim);
                for (int colIndex = skipCol; colIndex < nCol; colIndex++) {
                    if (currentRow[colIndex].equals(missingValueString)) {
                        itemsPerRow[rowIndex - nHeah][colIndex - skipCol] = Double.NaN;
                    } else {
                        itemsPerRow[rowIndex - nHeah][colIndex - skipCol] = Double.parseDouble(currentRow[colIndex]);
                    }

                }
            }
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Error while parsing file %s.\n Error occured on line %d containing \"%s\"\nError: %s",
                    textFilePath,
                    currentRowIndex,
                    rows[currentRowIndex],
                    e.toString());
            System.err.println(errorMessage);
            throw new Exception(e);
        }
        return itemsPerRow;
    }

    /**
     * Read a file containing float numbers (double) in a matrix format
     * 
     * @param textFilePath file path
     * @param sep          separator (can be a regex e.g. "\s+" for unknown number
     *                     of spaces).
     * @param nHeah        number of header row to skip
     * @return A double matrix of size [Rows][Columns]
     * @throws Exception Custom exceptions occuring when trying to parse the rows
     *                   (e.g. if the number of columns found is not consistent)
     */
    static public double[][] readMatrix(String textFilePath, String sep, int nHeah, String missingValueString)
            throws Exception {
        return FileReadWrite.readMatrix(textFilePath, sep, nHeah, missingValueString, 0, true, false);
    }

    static public String[] readHeaders(String textFilePath, String sep, int nRowSkip) {
        String[] headingLines = readLines(textFilePath, nRowSkip + 1, false);
        String[] headers = parseString(headingLines[headingLines.length - 1], sep, true);
        return headers;
    }

    /**
     * Given lines of texts in an array and file path, write the lines of text to
     * the file.
     * If the file doesn't exist, it is created.
     * 
     * @param textFilePath text file path as a Path object
     * @param lines        array of strings containing the lines of text to write
     * @throws IOException Errors may occur when creating the file or when trying to
     *                     write to the files
     */
    static public void writeLines(Path textFilePath, String[] lines) throws IOException {
        File file = textFilePath.toFile();
        writeLines(file, lines);
    }

    /**
     * Given lines of texts in an array and file path, write the lines of text to
     * the file.
     * If the file doesn't exist, it is created.
     * 
     * @param textFilePath text file path
     * @param lines        array of strings containing the lines of text to write
     * @throws IOException Errors may occur when creating the file or when trying to
     *                     write to the files
     */
    static public void writeLines(String textFilePath, String[] lines) throws IOException {
        File file = new File(textFilePath);
        writeLines(file, lines);
    }

    /**
     * Given lines of texts in an array and a file object, write the lines of text
     * to
     * the file.
     * If the file doesn't exist, it is created.
     * 
     * @param file  file objecty
     * @param lines array of strings containing the lines of text to write
     * @throws IOException Errors may occur when creating the file or when trying to
     *                     write to the files
     */
    static public void writeLines(File file, String[] lines) throws IOException {
        System.out.println(String.format("Writing to file \"%s\" ...", file.toString()));
        if (!file.exists()) {
            System.out.println("File does not exists");
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int k = 0; k < lines.length; k++) {
            bufferedWriter.write(lines[k]);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static String processMatrixRow(String[] row, String sep) {
        String line = "";
        for (int k = 0; k < row.length; k++) {
            String currentSep = k == 0 ? "" : sep;
            line = String.format("%s%s%s", line, currentSep, row[k]);
        }
        return line;
    }

    private static String processMatrixRow(double[] row, String sep, String missingValueString) {
        String[] stringRow = new String[row.length];
        for (int k = 0; k < row.length; k++) {
            // FIXME: Double.toString should handle null values? and use missing value code
            // of BaM
            if (Double.isNaN(row[k])) {
                stringRow[k] = missingValueString;
            } else {
                stringRow[k] = Double.toString(row[k]);
            }

        }
        return processMatrixRow(stringRow, sep);
    }

    static public void writeMatrix(
            String textFilePath,
            double[][] matrix,
            String sep,
            String missingValueString,
            String[] headers) throws IOException {
        int nRow = matrix.length;
        if (nRow <= 0) {
            System.err.println(String.format("Cannot write a matrix with %d row(s). Aborting.", nRow));
            return;
        }
        int nCol = matrix[0].length;
        if (nCol <= 0) {
            System.err.println(String.format("Cannot write a matrix with %d column(s). Aborting.", nCol));
            return;
        }
        int headerOffset = headers == null ? 0 : 1;
        String[] lines = new String[nRow + headerOffset];
        if (headerOffset == 1) {
            if (headers.length != nCol) {
                System.err.println(String.format(
                        "Cannot write matrix: header array is of length %d but there are %d column(s) in the matrix. Aborting.",
                        headers.length, nCol));
                return;
            }

            lines[0] = processMatrixRow(headers, sep);
            headerOffset = 1;
        }
        for (int k = 0; k < nRow; k++) {
            lines[k + headerOffset] = processMatrixRow(matrix[k], sep, missingValueString);
        }
        writeLines(textFilePath, lines);
    }

    // static public void writeMatrix(String textFilePath, double[][] matrix, String
    // sep, String missingValueString, String[] header)
    // throws IOException {
    // writeMatrix(textFilePath, matrix, missingValueString, null, sep);
    // }

    static public void writeMatrix(String textFilePath, double[][] matrix, String sep, String missingValueString)
            throws IOException {
        writeMatrix(textFilePath, matrix, sep, missingValueString, null);
    }

    static public void writeMatrix(String textFilePath, double[][] matrix, String sep)
            throws IOException {
        writeMatrix(textFilePath, matrix, sep, "-9999", null);
    }

    static public void writeMatrix(String textFilePath, double[][] matrix) throws IOException {
        writeMatrix(textFilePath, matrix, " ", "-9999", null);
    }

    // for debugging purposes
    // static public void printStringArray(String[] arr, String sep) {
    // for (int k = 0; k < arr.length; k++) {
    // System.out.print(String.format("%s%s", arr[k], sep));
    // }
    // }

    // for debugging purposes
    // static public void printStringArray(String[] arr) {
    // printStringArray(arr, ", ");
    // }

    // for debugging purposes
    // static public void printMatrix(double[][] arr) {
    // for (int k = 0; k < arr.length; k++) {
    // for (int i = 0; i < arr[k].length; i++) {
    // System.out.print(String.format("%f\t", arr[k][i]));
    // }
    // System.out.print("\n");
    // if (k > 10) {
    // return;
    // }
    // }
    // }

    // FIXME: not the right location, I think
    // static public double[] getMatrixColumn(double[][] matrix, int columnIndex) {
    // int n = matrix.length;
    // double[] column = new double[n];
    // for (int k = 0; k < n; k++) {
    // column[k] = matrix[k][columnIndex];
    // }
    // return column;
    // }

}
