package org.baratinage.ui.commons;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class AbstractDataset {

    public static record NamedColumn(String name, double[] values) {

    };

    private final String name;
    private final List<NamedColumn> data;

    protected AbstractDataset(String name, NamedColumn... namedColumns) {
        this.name = name;
        data = new ArrayList<>();
        int nRow = -1;
        for (NamedColumn column : namedColumns) {
            double[] values = column.values();
            if (nRow == -1 && values != null) {
                nRow = values.length;
            }
            if (values != null && nRow != values.length) {
                ConsoleLogger.error("cannot add NamedVector '" +
                        column.name() + "' because its length (" +
                        values.length + ") doesn't match expected length (" + nRow + ").");
            } else {
                data.add(column);
            }
        }
    }

    protected AbstractDataset(String name, String hashString, String... headers) {

        this.name = name;

        Path dataFilePath = buildDataFilePath(name, hashString);
        String[] fileHeaders = null;
        List<double[]> fileData = null;
        if (Files.exists(dataFilePath)) {
            ConsoleLogger.log("Reading file '" + dataFilePath + "'...");
            String dataFilePathString = dataFilePath.toString();

            try {
                String headerLine = ReadFile.getLines(dataFilePathString, 1, false)[0];
                fileHeaders = ReadFile.parseString(headerLine, ";", false);
                fileData = ReadFile.readMatrix(
                        dataFilePathString,
                        ";",
                        1,
                        Integer.MAX_VALUE,
                        "NA",
                        false,
                        false);

            } catch (IOException e) {
                ConsoleLogger.error("Failed to read data file ...(" + dataFilePathString + ")");
                ConsoleLogger.stackTrace(e);
            }

        } else {
            ConsoleLogger.error("File '" + dataFilePath + "' not found!");
        }

        data = new ArrayList<>();
        if (fileHeaders != null && fileData != null && fileHeaders.length == fileData.size()) {
            for (int k = 0; k < headers.length; k++) {
                int index = -1;
                for (int i = 0; i < fileHeaders.length; i++) {
                    if (fileHeaders[i].equals(headers[k])) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    ConsoleLogger.log("column '" + headers[k] + "' is null.");
                    data.add(new NamedColumn(headers[k], null));
                } else {
                    data.add(new NamedColumn(headers[k], fileData.get(index)));
                }
            }
        } else {
            ConsoleLogger.error("Failed to load data, inconsistencies found between headers and data sizes ...");
        }

    }

    public String getName() {
        return name;
    }

    public String[] getHeaders() {
        String[] headers = new String[data.size()];
        for (int k = 0; k < data.size(); k++) {
            headers[k] = data.get(k).name();
        }
        return headers;
    }

    public String getHeader(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= getNumberOfColumns()) {
            return null;
        }
        return data.get(columnIndex).name();
    }

    public List<double[]> getMatrix() {
        List<double[]> matrix = new ArrayList<>(data.size());
        for (NamedColumn column : data) {
            matrix.add(column.values());
        }
        return matrix;
    }

    public double[] getColumn(String colName) {
        for (NamedColumn col : data) {
            if (col.name.equals(colName)) {
                return col.values();
            }
        }
        return null;
    }

    public Double getValue(String colName, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            return null;
        }
        for (NamedColumn col : data) {
            if (col.name.equals(colName)) {
                return col.values()[rowIndex];
            }
        }
        return null;
    }

    public int getNumberOfColumns() {
        return data.size();
    }

    public int getNumberOfRows() {
        if (data.size() < 1) {
            return 0;
        }
        for (NamedColumn column : data) {
            double[] values = column.values();
            if (values != null) {
                return values.length;
            }
        }
        return 0;
    }

    private String computeHashString() {
        int[] hashCodes = new int[data.size() * 2];
        int k = 0;
        for (NamedColumn column : data) {
            hashCodes[k] = column.name().hashCode();
            k++;
            hashCodes[k] = Arrays.hashCode(column.values());
            k++;
        }
        int hashCode = Arrays.hashCode(hashCodes);
        hashCode = hashCode < 0 ? hashCode * -1 : hashCode;
        String hashString = "" + hashCode;
        ConsoleLogger.log("hash string  '" + hashString + "' was built for '" + name + "' ");
        return hashString;
    }

    protected void writeDataFile() {
        String hashString = computeHashString();
        String dataFilePath = buildDataFilePath(name, hashString).toString();
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
        List<String> nonNullHeaders = new ArrayList<>();
        List<double[]> nonNullMatrix = new ArrayList<>();
        for (NamedColumn col : data) {
            double[] values = col.values();
            if (values != null) {
                nonNullHeaders.add(col.name());
                nonNullMatrix.add(values);
            }
        }
        try {
            ConsoleLogger.log("Writting data '" + name + "' to file...");
            WriteFile.writeMatrix(
                    dataFilePath,
                    nonNullMatrix,
                    ";",
                    "NA",
                    nonNullHeaders.toArray(new String[nonNullHeaders.size()]));
        } catch (IOException e) {
            ConsoleLogger.error("Failed to write data '" +
                    name + "' to file... (" +
                    dataFilePath + ")");
            ConsoleLogger.stackTrace(e);
        }
    }

    public DatasetConfig save(boolean writeFile) {

        String hashString = computeHashString();

        String[] headers = getHeaders();
        String dataFilePath = buildDataFilePath(name, hashString).toString();
        if (writeFile) {
            writeDataFile(dataFilePath);
        }
        return new DatasetConfig(
                name, hashString, headers, dataFilePath);
    }

    private static String buildDataFileName(String name, String hashString) {
        return name + "_" + hashString + ".txt";
    }

    private static Path buildDataFilePath(String name, String hashString) {
        return Path.of(AppConfig.AC.APP_TEMP_DIR,
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

}
