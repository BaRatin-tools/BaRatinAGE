package org.baratinage.utils.fs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.utils.ConsoleLogger;

public class WriteFile {

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
    static public void writeStringContent(String textFilePath, String content) throws IOException {
        File file = new File(textFilePath);
        writeLines(file, new String[] { content });
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
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int k = 0; k < lines.length; k++) {
            bufferedWriter.write(lines[k]);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * Converts a double value into a string using a missing value code for NaN and
     * Infinite values
     * 
     * @param d                  double value to convert
     * @param missingValueString string to use for NaN and infinite values
     * @return string representation of the double
     */
    private static String toString(double d, String missingValueString) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return missingValueString;
        }
        return Double.toString(d);
    }

    /**
     * Convert a double array into a string using a missing value code for NaN and
     * Infinite values and a seperator to seperate values
     * 
     * @param d
     * @param sep
     * @param missingValueString
     * @return a string containing all the double values
     */
    private static String toString(double[] d, String sep, String missingValueString) {
        String[] dStr = new String[d.length];
        for (int k = 0; k < d.length; k++) {
            dStr[k] = toString(d[k], missingValueString);
        }
        return String.join(sep, dStr);
    }

    /**
     * write a matrix of strings to file
     * 
     * @param textFilePath     path to the text file
     * @param matrixColumnWise data as a List of String arrays with all arrays being
     *                         of equal length
     * @param sep              column separator to use
     * @param headers          String arrays for the header (or null); must match
     *                         the number of column (size of matrixColumnWise)
     * @throws IOException
     * @throws IllegalArgumentException
     */
    static public void writeMatrix(
            String textFilePath,
            List<String[]> matrixColumnWise,
            String sep,
            String[] headers) throws IOException, IllegalArgumentException {

        int nCol = matrixColumnWise.size();
        int nRow = nCol <= 0 ? 0 : matrixColumnWise.get(0).length;

        if (headers != null && headers.length != nCol) {
            throw new IllegalArgumentException(String.format(
                    "Mismatch between number of columns (%d) and headers length (%d)!",
                    nCol, headers.length));
        }

        File file = new File(textFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (headers != null) {
            bufferedWriter.write(String.join(sep, headers));
            bufferedWriter.newLine();
        }
        for (int i = 0; i < nRow; i++) {
            String row = "";
            for (int j = 0; j < nCol; j++) {
                String element = matrixColumnWise.get(j)[i];
                if (element.contains(sep)) {
                    ConsoleLogger.error(
                            String.format(
                                    "In row %d and column %d, the string contains the column separator: '%s'",
                                    i, j, element));
                }
                row = row + element;
                if (j != nCol - 1) {
                    row = row + sep;
                }
            }
            bufferedWriter.write(row);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * write a matrix of doubles to file
     * 
     * @param textFilePath       path to the text file
     * @param matrixColumnWise   data as a List of double arrays with all arrays
     *                           being of equal length
     * @param sep                column separator to use
     * @param missingValueString string to use for NaN and Infinite double values
     * @param headers            String arrays for the header (or null); must match
     *                           the number of column (size of matrixColumnWise)
     * @throws IOException
     * @throws IllegalArgumentException
     */
    static public void writeMatrix(
            String textFilePath,
            List<double[]> matrixColumnWise,
            String sep,
            String missingValueString,
            String[] headers) throws IOException, IllegalArgumentException {

        int nCol = matrixColumnWise.size();
        int nRow = nCol <= 0 ? 0 : matrixColumnWise.get(0).length;

        if (headers != null && headers.length != nCol) {
            throw new IllegalArgumentException(String.format(
                    "Mismatch between number of columns (%d) and headers length (%d)!",
                    nCol, headers.length));
        }

        File file = new File(textFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (headers != null) {
            bufferedWriter.write(String.join(sep, headers));
            bufferedWriter.newLine();
        }
        for (int i = 0; i < nRow; i++) {
            String row = "";
            for (int j = 0; j < nCol; j++) {
                String element = toString(matrixColumnWise.get(j)[i], missingValueString);
                row = row + element;
                if (j != nCol - 1) {
                    row = row + sep;
                }
            }
            bufferedWriter.write(row);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * write a matrix of doubles to file
     * 
     * @param textFilePath       path to the text file
     * @param matrix             data as a List of double arrays with all arrays
     *                           being of equal length
     * @param sep                column separator to use
     * @param missingValueString string to use for NaN and Infinite double values
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    static public void writeMatrixHorizontally(
            String textFilePath,
            List<double[]> matrix,
            String sep,
            String missingValueString) throws IOException {

        int nRow = matrix.size();

        File file = new File(textFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        for (int i = 0; i < nRow; i++) {
            String row = toString(matrix.get(i), sep, missingValueString);
            bufferedWriter.write(row);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        fileWriter.close();
    }
}
