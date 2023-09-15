package org.baratinage.jbam.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Write {

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
            line = line + currentSep + row[k];
        }
        return line;
    }

    private static String processMatrixRow(double[] row, String sep, String missingValueString) {
        String[] stringRow = new String[row.length];
        for (int k = 0; k < row.length; k++) {
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
            List<double[]> matrixColumnWise,
            String sep,
            String missingValueString,
            String[] headers) throws IOException {

        int nCol = matrixColumnWise.size();
        if (nCol <= 0) {
            System.err.println("Write Error: Cannot write an empty matrix.");
            return;
        }
        int nRow = matrixColumnWise.get(0).length;
        if (nRow <= 0) {
            // FIXME: this condition could be skiped if BaM allowed input data with 0 rows!
            // At least for calibration data, Config_Data.txt with nobs = 0 throws an error
            // FIXME: this error should be handled in CalibrationData class!
            System.err.println("Write Error: Cannot write an empty matrix.");
            // String[] emptyMatrix = new String[nCol];
            // for (int k = 0; k < nCol; k++) {
            // emptyMatrix[k] = "";
            // }
            // writeLines(textFilePath, new String[] { processMatrixRow(emptyMatrix, sep)
            // });
            return;
        }
        int headerOffset = headers == null ? 0 : 1;
        String[] lines = new String[nRow + headerOffset];
        if (headers != null) {
            if (headers.length != nCol) {
                System.err.printf(
                        "Mismatch between number of columns (%d) and headers length (%d)!\n",
                        headers.length, nCol);
                return;
            }
            lines[0] = processMatrixRow(headers, sep);
            headerOffset = 1;
        }
        for (int i = 0; i < nRow; i++) {
            double[] row = new double[nCol];
            for (int j = 0; j < nCol; j++) {
                row[j] = matrixColumnWise.get(j)[i];
            }
            lines[i + headerOffset] = processMatrixRow(row, sep, missingValueString);
        }
        writeLines(textFilePath, lines);
    }

}
