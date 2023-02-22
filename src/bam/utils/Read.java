package bam.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.universalchardet.ReaderFactory;

public class Read {

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
        int nCol = getColumnCount(textFilePath, detectCharset, sep, trim, nRowSkip);
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
            if (i > nRow)
                break;
            if (k >= nRowSkip) {
                double[] row = parseStringArray(parseString(line, sep, trim), missingValueString);
                for (int j = 0; j < nCol; j++) {
                    // matrix[i][j] = row[j + nColSkip];
                    colMatrix.get(j)[i] = row[j + nColSkip];
                }
                i++;
            }
            line = reader.readLine();
            k++;
        }

        return colMatrix;
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

    static private double[] parseStringArray(String[] str, String missingValueString) {
        double[] result = new double[str.length];
        for (int k = 0; k < str.length; k++) {
            if (str[k].equals(missingValueString)) {
                result[k] = Double.NaN;
            } else {
                result[k] = Double.parseDouble(str[k]);
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
