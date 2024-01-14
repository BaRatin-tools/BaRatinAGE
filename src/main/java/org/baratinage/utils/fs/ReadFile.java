package org.baratinage.utils.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.ConsoleLogger;
import org.mozilla.universalchardet.ReaderFactory;

public class ReadFile {

    /**
     * Given a filepath to a text file and wether file encoding should be infered
     * from file returns a BufferedReader object that can be used to read through
     * the text file.
     * 
     * @param filePath       text file path
     * @param detectEncoding wether file encoding should be infered from file
     *                       content
     * @return BufferedReader object that can be used to read through the text file
     * @throws IOException
     */
    static public BufferedReader createBufferedReader(String filePath, boolean detectEncoding) throws IOException {
        if (detectEncoding) {
            File file = new File(filePath);
            return new BufferedReader(ReaderFactory.createBufferedReader(file));
        } else {
            return new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
        }
    }

    /**
     * 
     * Given a filepath to a text file and wether file encoding should be infered
     * from file loop through all the line in the file to get and return the total
     * number of rows the file contains
     * 
     * @param filePath      text file path
     * @param detectCharset wether file encoding should be infered from file content
     * @return total number of rows in file
     * @throws IOException
     */
    private static int getLinesCount(String filePath, boolean detectCharset) throws IOException {
        BufferedReader reader = createBufferedReader(filePath, detectCharset);
        int n = 0;
        while (reader.readLine() != null)
            n++;
        reader.close();
        return n;
    }

    /**
     * Given a filepath to a text file, wether file encoding should be infered and a
     * maximum number of lines to read from the file, returns the file rows as a
     * String array.
     * 
     * @param filePath      text file path
     * @param maxLines      the maximum number of rows/lines to read
     * @param detectCharset wether file encoding should be infered from file content
     * @return String array containing the rows of text of the file
     * @throws IOException
     */
    public static String[] getLines(String filePath, int maxLines, boolean detectCharset) throws IOException {
        int nLines = getLinesCount(filePath, detectCharset);
        String[] lines = new String[nLines];
        BufferedReader reader = createBufferedReader(filePath, detectCharset);
        int n = 0;
        String line = reader.readLine();
        while (line != null) {
            lines[n] = line;
            line = reader.readLine();
            n++;
            if (n > maxLines) {
                break;
            }
        }
        reader.close();
        return lines;
    }

    public static String getStringContent(String filePath, boolean detectCharset) throws IOException {
        String[] lines = getLines(filePath, Integer.MAX_VALUE, detectCharset);
        return String.join("\n", lines);
    }

    /**
     * Get the number of column in a text file given a column separator and a
     * reference row index to use to retrieve the number of columns.
     * 
     * @param filePath      text file path
     * @param detectCharset wether file encoding should be infered from file content
     * @param sep           column separator
     * @param trim          trim heading/trailing spaces
     * @param refRowIndex   reference row index to use to infer the number of
     *                      columns
     * @return the number of columns in the text file
     * @throws IOException
     */
    private static int getColumnCount(String filePath, boolean detectCharset, String sep, boolean trim, int refRowIndex)
            throws IOException {
        BufferedReader reader = createBufferedReader(filePath, detectCharset);
        int n = 0;
        int nCol = 0;
        String line = reader.readLine();
        while (line != null) {
            if (n == refRowIndex) {
                String[] rowElement = parseString(line, sep, trim);
                nCol = rowElement.length;
                break;
            }
            line = reader.readLine();
            n++;
        }
        reader.close();
        return nCol;
    }

    /**
     * Split a string into pieces given a string separator.
     * 
     * @param str  String to split
     * @param sep  separator to use when splitting the string
     * @param trim trim heading/trailing spaces
     * @return String array resulting from the split operation
     */
    public static String[] parseString(String str, String sep, boolean trim) {
        if (trim) {
            return trimStringArray(str.trim().split(sep));
        } else {
            return str.split(sep);
        }
    }

    /**
     * Remove heading/trailing spaces from each element of a string array
     * 
     * @param str string array to trim
     * @return trimmed string array
     */
    public static String[] trimStringArray(String[] str) {
        int n = str.length;
        String[] trimmedStr = new String[n];
        for (int k = 0; k < n; k++) {
            trimmedStr[k] = str[k].trim();
        }
        return trimmedStr;
    }

    /**
     * converts a string into a double.
     * 
     * @param str              string to convert
     * @param missingValueCode string to use as a missing value code (Double.NaN is
     *                         used to indicate missing values)
     * @return double value resulting from the conversion.
     */
    private static double toDouble(String str, String missingValueCode) {
        if (str.equals(missingValueCode)) {
            return Double.NaN;
        }
        try {
            Double d = Double.parseDouble(str);
            return d;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /**
     * Converts a array of String into a String Matrix (List of String arrays) by
     * splitting each string using a given separator. This function is used to turn
     * rows into an actual matrix.
     * 
     * @param lines    String array to process
     * @param sep      String separator to use when splitting each row
     * @param nRowSkip number of rows to skip
     * @param nRowMax  maximum number of rows to process
     * @param trim     wether heading/trailing spaces should be removed
     * @return String Matrix (List of String arrays)
     */
    public static List<String[]> linesToStringMatrix(String[] lines, String sep, int nRowSkip, int nRowMax,
            boolean trim) {
        int nLines = lines.length;
        if (nRowSkip >= nLines) {
            ConsoleLogger.error("nRowSkip greater than number of lines!");
            return null;
        }
        int nRow = Math.min(nLines - nRowSkip, nRowMax);
        int nCol = parseString(lines[nRowSkip], sep, trim).length;

        List<String[]> columns = new ArrayList<>();
        for (int i = 0; i < nCol; i++) {
            String[] column = new String[nRow];
            columns.add(column);
        }

        for (int i = 0; i < nRow; i++) {
            int k = nRowSkip + i;
            String[] row = parseString(lines[k], sep, trim);
            if (row.length != nCol) {
                ConsoleLogger.error("Error while parsing line " + k + "...");
                break;
            }
            for (int j = 0; j < nCol; j++) {
                columns.get(j)[i] = row[j];
            }
        }

        return columns;
    }

    /**
     * Return one specific rows from a String Matrix (List of String arrays)
     * 
     * @param columns String Matrix (List of String arrays)
     * @param index   rows to extract
     * @return String array of the extracted row
     */
    public static String[] getStringRow(List<String[]> columns, int index) {
        int nCol = columns.size();
        String[] row = new String[nCol];
        for (int k = 0; k < nCol; k++) {
            row[k] = columns.get(k)[index];
        }
        return row;
    }

    /**
     * Get a single row as a string array
     * 
     * @param textFilePath  text file path
     * @param sep           column separator
     * @param nRowSkip      number of rows to skip
     * @param detectCharset wether file encoding should be infered from the file
     * @param trim          wether heading/trailing spaces should be discared
     * @return a string array of the header row
     * @throws IOException
     */
    public static String[] getHeaderRow(String textFilePath,
            String sep,
            int nRowSkip,
            boolean detectCharset,
            boolean trim) throws IOException {
        String[] lines = getLines(textFilePath, nRowSkip + 1, detectCharset);
        if (lines.length <= nRowSkip) {
            ConsoleLogger.error("Not enough rows in file to extract header row given the number of rows to skip!");
            return null;
        }
        String[] headerRow = parseString(lines[nRowSkip], sep, trim);
        return headerRow;
    }

    /**
     * Read a text file containg data and return a double Matrix (List of double
     * arrays).
     * 
     * @param textFilePath     text file path
     * @param sep              column separator
     * @param nRowSkip         number of rows to skip
     * @param nRowMax          maximum number of rows to read from the file
     * @param missingValueCode code to identify missing values
     * @param detectCharset    wether file encoding should be infered from the file
     * @param trim             wether heading/trailing spaces should be discared
     * @return a double Matrix (List of double arrays)
     * @throws IOException
     */
    static public List<double[]> readMatrix(
            String textFilePath,
            String sep,
            int nRowSkip,
            int nRowMax,
            String missingValueCode,
            boolean detectCharset,
            boolean trim) throws IOException {

        int nLines = getLinesCount(textFilePath, detectCharset);
        int nRow = Math.min(nLines - nRowSkip, nRowMax);
        int nCol = getColumnCount(textFilePath, detectCharset, sep, trim, nRowSkip);

        List<double[]> columns = new ArrayList<>();
        for (int i = 0; i < nCol; i++) {
            double[] column = new double[nRow];
            columns.add(column);
        }

        BufferedReader reader = new BufferedReader(createBufferedReader(textFilePath,
                detectCharset));
        int k = 0;
        int i = 0;
        String line = reader.readLine();
        while (line != null) {
            if (i >= nRow)
                break;
            if (k >= nRowSkip) {
                String[] row = parseString(line, sep, trim);
                if (row.length != nCol) {
                    ConsoleLogger.error(
                            String.format("Row %d has %d columns but %d columns were expected. Row skipped.",
                                    k, row.length, nCol));
                    for (int j = 0; j < nCol; j++) {
                        columns.get(j)[i] = Double.NaN;
                    }
                } else {
                    for (int j = 0; j < nCol; j++) {
                        columns.get(j)[i] = toDouble(row[j], missingValueCode);
                    }
                }
                i++;
            }
            line = reader.readLine();
            k++;
        }
        reader.close();

        return columns;
    }

    /**
     * Read a text file containg data and return a double Matrix (List of double
     * arrays).
     * 
     * @param textFilePath  text file path
     * @param sep           column separator
     * @param nRowSkip      number of rows to skip
     * @param detectCharset wether file encoding should be infered from the file
     * @param trim          wether heading/trailing spaces should be discared
     * @return a double Matrix (List of double arrays)
     * @throws IOException
     */
    static public List<String[]> readStringMatrix(
            String textFilePath,
            String sep,
            int nRowSkip,
            boolean detectCharset,
            boolean trim) throws IOException {

        int nLines = getLinesCount(textFilePath, detectCharset);
        int nRow = nLines - nRowSkip;
        int nCol = getColumnCount(textFilePath, detectCharset, sep, trim, nRowSkip);

        List<String[]> columns = new ArrayList<>();
        for (int i = 0; i < nCol; i++) {
            String[] column = new String[nRow];
            columns.add(column);
        }

        BufferedReader reader = new BufferedReader(createBufferedReader(textFilePath,
                detectCharset));
        int k = 0;
        int i = 0;
        String line = reader.readLine();
        while (line != null) {
            if (i >= nRow)
                break;
            if (k >= nRowSkip) {
                String[] row = line.split(sep, -1);
                if (row.length != nCol) {
                    ConsoleLogger.error(
                            String.format("Row %d has %d columns but %d columns were expected. Row skipped.",
                                    k, row.length, nCol));
                    for (int j = 0; j < nCol; j++) {
                        columns.get(j)[i] = null;
                    }
                } else {
                    if (trim) {
                        for (int j = 0; j < nCol; j++) {
                            columns.get(j)[i] = row[j].trim();
                        }
                    } else {
                        for (int j = 0; j < nCol; j++) {
                            columns.get(j)[i] = row[j];
                        }
                    }
                }
                i++;
            }
            line = reader.readLine();
            k++;
        }
        reader.close();

        return columns;
    }

    /**
     * Read an UTF8 encoded text file and return content as one String object
     * 
     * @param textFilePath text file path
     * @return file content as a String
     * @throws IOException
     */
    public static String readTextFile(String textFilePath) throws IOException {
        return readTextFile(Path.of(textFilePath));
    }

    /**
     * Read an UTF8 encoded text file and return content as one String object
     * 
     * @param textFilePath text file path
     * @return file content as a String
     * @throws IOException
     */
    public static String readTextFile(Path textFilePath) throws IOException {
        String text = new String(
                Files.readAllBytes(textFilePath),
                StandardCharsets.UTF_8);
        return text;
    }

}
