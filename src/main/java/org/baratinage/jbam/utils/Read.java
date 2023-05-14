package org.baratinage.jbam.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

// FIXME: the bam librairy should not take care of encoding issues; 
// FIXME: it should be a matter for the ui where the user can import random files
import org.mozilla.universalchardet.ReaderFactory;

public class Read {

    // FIXME: missing value code should be final constant defined here

    static BufferedReader createBufferedReader(String filePath, boolean detectEncoding) throws IOException {
        if (detectEncoding) {
            File file = new File(filePath);
            return new BufferedReader(ReaderFactory.createBufferedReader(file));
        } else {
            return new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
        }
    }

    static public int getLinesCount(String filePath, boolean detectCharset) throws IOException {
        BufferedReader reader = createBufferedReader(filePath, detectCharset);
        int n = 0;
        while (reader.readLine() != null)
            n++;
        reader.close();
        return n;
    }

    static public int getColumnCount(String filePath, boolean detectCharset, String sep, boolean trim, int refRowIndex)
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

    static public List<double[]> readMatrix(
            String textFilePath,
            String sep,
            int nRowSkip,
            int nColSkip) throws IOException {
        int nRowMax = Integer.MAX_VALUE;
        int nColMax = Integer.MAX_VALUE;
        return readMatrix(
                textFilePath,
                sep,
                nRowSkip,
                nRowMax,
                nColSkip,
                nColMax,
                "-9999",
                false,
                true);
    }

    static public List<double[]> readMatrix(
            String textFilePath,
            int nRowSkip) throws IOException {
        int nRowMax = Integer.MAX_VALUE;
        int nColMax = Integer.MAX_VALUE;
        return readMatrix(
                textFilePath,
                "\\s+",
                nRowSkip,
                nRowMax,
                0,
                nColMax,
                "-9999",
                false,
                true);
    }

    static public List<double[]> readMatrix(
            String textFilePath,
            String sep,
            int nRowSkip,
            int nColSkip,
            String missingValueCode,
            boolean detectCharset) throws IOException {
        int nRowMax = Integer.MAX_VALUE;
        int nColMax = Integer.MAX_VALUE;
        return readMatrix(
                textFilePath,
                sep,
                nRowSkip,
                nRowMax,
                nColSkip,
                nColMax,
                missingValueCode,
                detectCharset,
                true);
    }

    static public List<double[]> readMatrix(
            String textFilePath,
            String sep,
            int nRowSkip,
            int nRowMax,
            int nColSkip,
            int nColMax,
            String missingValueString,
            boolean detectCharset,
            boolean trim) throws IOException {

        int nLines = getLinesCount(textFilePath, detectCharset);
        int nRow = Math.min(nLines - nRowSkip, nRowMax);
        int nCol = getColumnCount(textFilePath, detectCharset, sep, trim, nRowSkip) - nColSkip;
        nCol = Math.min(nColMax, nCol);

        List<double[]> colMatrix = new ArrayList<>();
        for (int i = 0; i < nCol; i++) {
            double[] column = new double[nRow];
            colMatrix.add(column);
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
                double[] row = parseStringArray(parseString(line, sep, trim), missingValueString, nColSkip);
                for (int j = 0; j < nCol; j++) {
                    colMatrix.get(j)[i] = row[j];
                }
                i++;
            }
            line = reader.readLine();
            k++;
        }

        return colMatrix;
    }

    static public String[] readHeaders(String textFilePath) throws IOException {
        return readHeaders(textFilePath, "\\s+", 0, false, true);
    }

    static public String[] readHeaders(String textFilePath,
            String sep,
            int headerRowIndex) throws IOException {
        return readHeaders(textFilePath, sep, headerRowIndex, false, true);
    }

    static public String[] readHeaders(String textFilePath,
            String sep,
            int headerRowIndex,
            boolean detectCharset,
            boolean trim) throws IOException {

        BufferedReader reader = createBufferedReader(textFilePath, detectCharset);
        int n = 0;
        String[] headers = new String[0];
        String line = reader.readLine();
        while (line != null) {
            if (n == headerRowIndex) {
                headers = parseString(line, sep, trim);
                break;
            }
            line = reader.readLine();
            n++;
        }
        reader.close();

        return headers;
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

    static private double[] parseStringArray(String[] str, String missingValueString, int nColSkip) {
        double[] result = new double[str.length - nColSkip];
        for (int k = nColSkip; k < str.length; k++) {
            if (str[k].equals(missingValueString)) {
                result[k - nColSkip] = Double.NaN;
            } else {
                try {
                    result[k - nColSkip] = Double.parseDouble(str[k]);
                } catch (NumberFormatException e) {
                    // NOTE: this try/catch is necessary because BaM sometimes gives
                    // very low/high values that can't be parsed (e.g. -0.179769+309)
                    System.err.println(e);
                    result[k - nColSkip] = Double.NaN;
                }
            }
        }
        return result;
    }

    public static void prettyPrintMatrix(List<double[]> matrix) {

        // Getting matrix dimensions
        int nCol = matrix.size();
        if (nCol == 0) {
            System.out.println("\nmatrix | 0 x 0 | empty matrix\n");
            return;
        }
        int[] nRows = new int[nCol];
        int nRow = matrix.get(0).length;
        int nRowMax = nRow;
        for (int k = 0; k < nCol; k++) {
            if (nRow != matrix.get(k).length) {
                nRow = -1;
                if (matrix.get(k).length > nRowMax) {
                    nRowMax = matrix.get(k).length;
                }
            }
            nRows[k] = matrix.get(k).length;
        }

        // Printing matrix dimensions
        System.out.print("\nmatrix | (row x column) ");
        if (nRow == -1) {
            System.out.print("[");
            for (int i : nRows) {
                System.out.printf("%d, ", i);

            }
            System.out.print("]");
        } else {
            System.out.printf("%d", nRow);
        }
        System.out.printf(" x %d\n\n", nCol);

        // Printing column headers
        for (int j = 0; j < nCol; j++) {
            String s = String.format("Col_%d", j);
            System.out.printf("%12s", s);
            if (j >= 4) {
                System.out.print(" ...");
                break;
            }
        }
        System.out.print("\n");

        // Printing content, row wise
        for (int j = 0; j < nRowMax; j++) {
            for (int i = 0; i < nCol; i++) {
                if (matrix.get(i).length > j) {
                    System.out.printf("%12.4f", matrix.get(i)[j]);
                } else {
                    System.out.printf("%12s", "  -  "); // row not long enough
                }
                if (i >= 4) {
                    System.out.print(" ..."); // meaning there's more column
                    break;
                }
            }

            System.out.print("\n");
            if (j >= 5) {
                System.out.printf("%12s\n", "..."); // meaning there's more row
                break;
            }
        }
        System.out.print("\n");
    }

}
