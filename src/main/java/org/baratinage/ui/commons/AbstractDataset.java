package org.baratinage.ui.commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

public class AbstractDataset {

    protected final String name;
    protected final double[][] data;
    private final TreeMap<String, Integer> headersMap;
    // private final String[] headers;
    protected final int nRow;
    protected final int nCol;

    private static String[] buildDefaultHeaders(int n) {
        String[] headers = new String[n];
        for (Integer k = 0; k < n; k++) {
            headers[k] = k.toString();
        }
        return headers;
    }

    protected AbstractDataset(String name, double[]... columns) {
        this(name, buildDefaultHeaders(columns.length), columns);
    }

    protected AbstractDataset(String name, String[] headers, double[]... columns) {

        if (columns.length != headers.length) {
            throw new IllegalArgumentException(
                    "'headers' length is different than the number of columns! Headers are ignored.");
        }

        this.name = name;
        this.headersMap = new TreeMap<>();

        int nCol = 0;
        int nRow = -1;
        for (int k = 0; k < columns.length; k++) {
            if (columns[k] != null) {
                nCol++;
                if (nRow < 0) {
                    nRow = columns[k].length;
                }
            }
        }
        this.nCol = nCol;
        this.nRow = nRow < 0 ? 0 : nRow;

        if (nCol != columns.length) {
            System.out.println("There are some NULL columns !");
        }

        int index = 0;
        for (int k = 0; k < headers.length; k++) {
            if (columns[k] == null) {
                continue;
            }
            this.headersMap.put(headers[k], index);
            index++;
        }

        this.data = new double[nCol][];

        index = 0;
        for (int k = 0; k < columns.length; k++) {
            if (columns[k] == null) {
                ConsoleLogger.warn(String.format("Column %d ('%s') is null !", k, headers[k]));
                continue;
            }
            int m = columns[k].length;
            if (m != nRow) {
                ConsoleLogger.warn(
                        String.format(
                                "Mismatch in the number of rows per column! " +
                                        " Column #%d has %d rows wile %d rows are expected",
                                k, m, nRow));
            }
            this.data[index] = columns[k];
            index++;
        }

    }

    protected AbstractDataset(String name, String hashString, String... headers) {

        this.name = name;

        // try loading using sanitized name
        Path dataFilePath = buildDataFilePath(Misc.sanitizeName(name), hashString);
        if (!Files.exists(dataFilePath)) {
            dataFilePath = buildDataFilePath(name, hashString);
        }
        String dataFilePathString = dataFilePath.toString();

        TreeMap<String, Integer> _headersMap = new TreeMap<>();
        String[] _headers = new String[0];
        int _nCol = 0;
        int _nRow = 0;
        double[][] _data = new double[0][0];

        if (!Files.exists(dataFilePath)) {
            ConsoleLogger.error("File '" + dataFilePath + "' not found!");
            // this.headers = _headers;
            this.headersMap = _headersMap;
            this.nCol = _nCol;
            this.nRow = _nRow;
            this.data = _data;
            return;
        }

        try {
            _headers = readHeaders(dataFilePathString);
            _nCol = getColCount(dataFilePathString);
            for (int k = 0; k < _nCol; k++) {
                _headersMap.put(_headers[k], k);
            }
            _nRow = readRowCount(dataFilePathString);
            _data = readMatrix(dataFilePathString, _nRow, _nCol);
        } catch (IOException e) {
            ConsoleLogger.error("Failed to read data file ...(" + dataFilePathString + ")\n" + e);
            ConsoleLogger.error(e);
        }

        // if headers are provided, they are added to the headers array
        // this is only for backward compatibility issues when building
        // the hash string
        if (headers != null && headers.length > 0) {
            List<String> headersAll = new ArrayList<>();
            for (String s : _headers) {
                headersAll.add(s);
            }
            for (String s : headers) {
                if (!headersAll.contains(s)) {
                    headersAll.add(s);
                }
            }
            _headers = new String[headersAll.size()];
            for (int k = 0; k < headersAll.size(); k++) {
                _headers[k] = headersAll.get(k);
            }
        }

        this.headersMap = _headersMap;
        // this.headers = _headers;
        this.nCol = _nCol;
        this.nRow = _nRow;
        this.data = _data;
    }

    public String getName() {
        return name;
    }

    public String[] getHeaders() {
        String[] headers = headersMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue())
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
        return headers;
    }

    public List<double[]> getMatrix() {
        List<double[]> matrix = new ArrayList<>();
        for (int col = 0; col < data.length; col++) {
            matrix.add(data[col]);
        }
        return matrix;
    }

    public boolean containsColumn(String colname) {
        return headersMap.containsKey(colname);
    }

    public void renameColumn(String oldName, String newName) {
        if (!headersMap.containsKey(oldName)) {
            return;
        }
        int columnIndex = headersMap.get(oldName);
        headersMap.remove(oldName);
        headersMap.put(newName, columnIndex);

    }

    public double[] getColumn(String colname) {
        Integer index = headersMap.containsKey(colname) ? headersMap.get(colname) : null;
        return index == null ? null : getColumn(index);
    }

    public double[] getColumn(int index) {
        if (index >= 0 && index < data.length) {
            return data[index];
        }
        return null;
    }

    public int getNumberOfColumns() {
        return nCol;
    }

    public int getNumberOfRows() {
        return nRow;
    }

    private String computeHashString() {
        List<Integer> hashCodes = new ArrayList<>();
        for (String h : getHeaders()) {
            hashCodes.add(h.hashCode());
            Integer i = headersMap.get(h);
            double[] d = i == null ? null : data[i];
            hashCodes.add(Arrays.hashCode(d));
        }
        int hashCode = Arrays.hashCode(hashCodes.stream().mapToInt(Integer::intValue).toArray());
        hashCode = hashCode < 0 ? hashCode * -1 : hashCode;
        String hashString = "" + hashCode;
        ConsoleLogger.log("hash string '" + hashString + "' was built for '" + name + "' ");
        return hashString;
    }

    protected void writeDataFile() {
        String hashString = computeHashString();
        String sanitizedName = Misc.sanitizeName(name);
        String dataFilePath = buildDataFilePath(sanitizedName, hashString).toString();
        writeDataFile(dataFilePath);
    }

    private void writeDataFile(String dataFilePath) {
        if (dataFilePath == null) {
            ConsoleLogger.error("cannot write data file because dataFilePath is null");
            return;
        }
        File f = new File(dataFilePath);
        if (f.exists()) {
            // dataFilePath is suppose to have a name with a hash string reflecting actual
            // data, if same name, it means same data!
            ConsoleLogger.log("no need to write file, it already exists.");
            return;
        }
        try {
            ConsoleLogger.log("Writting data '" + name + "' to file...");
            writeMatrix(dataFilePath, data, getHeaders());
        } catch (IOException e) {
            ConsoleLogger.error("Failed to write data '" +
                    name + "' to file... (" +
                    dataFilePath + ")\n" + e);
        }
    }

    public DatasetConfig save(boolean writeFile) {
        String hashString = computeHashString();
        String[] headers = getHeaders();
        String sanitizedName = Misc.sanitizeName(name);
        String dataFilePath = buildDataFilePath(sanitizedName, hashString).toString();
        if (writeFile) {
            writeDataFile(dataFilePath);
        }
        return new DatasetConfig(
                name,
                hashString,
                headers,
                dataFilePath);
    }

    private static String buildDataFileName(String name, String hashString) {
        return name + "_" + hashString + ".txt";
    }

    private static Path buildDataFilePath(String name, String hashString) {
        return Path.of(AppSetup.PATH_APP_TEMP_DIR,
                buildDataFileName(name, hashString));
    }

    protected static double[] toDouble(boolean[] src) {
        int n = src.length;
        double[] tgt = new double[n];
        for (int k = 0; k < n; k++) {
            tgt[k] = src[k] ? 1d : 0d;
        }
        return tgt;
    }

    protected static boolean[] toBoolean(double[] src) {
        int n = src.length;
        boolean[] tgt = new boolean[n];
        for (int k = 0; k < n; k++) {
            tgt[k] = src[k] == 1d;
        }
        return tgt;
    }

    protected static double[] toDouble(int[] src) {
        int n = src.length;
        double[] tgt = new double[n];
        for (int k = 0; k < n; k++) {
            tgt[k] = (double) src[k];
        }
        return tgt;
    }

    protected static int[] toInt(double[] src) {
        int n = src.length;
        int[] tgt = new int[n];
        for (int k = 0; k < n; k++) {
            tgt[k] = ((Double) src[k]).intValue();
        }
        return tgt;
    }

    private static String[] readHeaders(String filePath) throws IOException {
        String[] headers = new String[0];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null)
                throw new IOException("Empty file");
            headers = header.split(";");
        }
        return headers;
    }

    private static int getColCount(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null)
                throw new IOException("Empty file");
            String firstRow = br.readLine();
            return firstRow.split(";").length;
        }
    }

    private static int readRowCount(String filePath) throws IOException {
        int rows = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip headers
            while (br.readLine() != null)
                rows++;
        }
        return rows;
    }

    private static double[][] readMatrix(String filePath, int rows, int cols) throws IOException {
        double[][] matrix = new double[cols][rows];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != cols) {
                    continue;
                }
                for (int col = 0; col < cols; col++) {
                    Double v = parts[col].equals("NA") ? Double.NaN : Double.parseDouble(parts[col]);
                    matrix[col][row] = v;
                }
                row++;
            }
        }
        return matrix;
    }

    private static void writeMatrix(String filePath, double[][] matrix, String[] headers) throws IOException {
        int rows = matrix[0].length;
        int cols = matrix.length;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(String.join(";", headers));
            bw.newLine();
            // Write data row by row
            for (int row = 0; row < rows; row++) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < cols; col++) {
                    if (col > 0) {
                        sb.append(";");
                    }
                    Double d = matrix[col][row];
                    sb.append(Double.isNaN(d) || Double.isInfinite(d) ? "NA" : d);
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }
    }

}
