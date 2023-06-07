package org.baratinage.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.universalchardet.ReaderFactory;

public class ReadFile {

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

    static public String[] getLines(String filePath, int maxLines, boolean detectCharset) throws IOException {
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

    @Deprecated
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

    static public String[] parseString(String str, String sep, boolean trim) {
        if (trim) {
            return str.trim().split(sep);
        } else {
            return str.split(sep);
        }
    }

    static public double[] arrayStringToDouble(String[] str, String missingValueString, int nColSkip) {
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

    static public double toDouble(String str, String missingValueCode) {
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

    static public List<String[]> linesToStringMatrix(String[] lines, String sep, int nRowSkip, int nRowMax,
            boolean trim) {
        int nLines = lines.length;
        if (nRowSkip >= nLines) {
            System.err.println("nRowSkip greater than number of lines!");
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
                System.err.println("Error while parsing line " + k + "...");
                break;
            }
            for (int j = 0; j < nCol; j++) {
                columns.get(j)[i] = row[j];
            }
        }

        return columns;
    }

    static public List<String[]> getSubStringMatrix(List<String[]> data, int from, int to) {
        if (from >= to)
            return data;
        if (data.size() == 0)
            return data;

        int nCol = data.size();
        int nRow = data.get(0).length;
        if (nRow <= from)
            return data;

        nRow = Math.min(nRow - from, to - from);

        List<String[]> subData = new ArrayList<>();
        for (int k = 0; k < nCol; k++) {
            subData.add(new String[nRow]);
        }
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                subData.get(j)[i] = data.get(j)[i + from];
            }
        }

        return subData;
    }

    static public List<String[]> getSubStringMatrix(List<String[]> data, int from) {
        return getSubStringMatrix(data, from, Integer.MAX_VALUE);
    }

    static public String[] getStringRow(List<String[]> columns, int index) {
        int nCol = columns.size();
        String[] row = new String[nCol];
        for (int k = 0; k < nCol; k++) {
            row[k] = columns.get(k)[index];
        }
        return row;
    }

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
                    System.err.println("Inconsistent number of columns (row " + k + ")");
                    continue;
                }
                for (int j = 0; j < nCol; j++) {
                    columns.get(j)[i] = toDouble(row[j], missingValueCode);
                }
                i++;
            }
            line = reader.readLine();
            k++;
        }

        return columns;
    }

}
