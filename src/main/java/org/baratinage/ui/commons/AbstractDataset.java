package org.baratinage.ui.commons;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;
import org.json.JSONArray;
import org.json.JSONObject;

public class AbstractDataset {

    public static record NamedColumn(String name, double[] values) {

    };

    private final String name;
    private final List<NamedColumn> data;

    private String dataFilePath;

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
                System.err.println("AbstractDataset Error: cannot add NamedVector '" +
                        column.name() + "' because its length (" +
                        values.length + ") doesn't match expected length (" + nRow + ").");
            } else {
                data.add(column);
            }
        }
    }

    protected AbstractDataset(JSONObject json) {

        name = json.getString("name");

        String hashString = json.getString("hashString");

        JSONArray headersJson = json.getJSONArray("headers");
        int nCol = headersJson.length();
        String[] headers = new String[nCol];
        for (int k = 0; k < nCol; k++) {
            headers[k] = headersJson.getString(k);
        }

        Path dataFilePath = buildDataFilePath(name, hashString);
        String[] fileHeaders = null;
        List<double[]> fileData = null;
        if (Files.exists(dataFilePath)) {
            System.out.println("AbstractDataset: Reading file '" + dataFilePath + "'...");
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
                System.err.println("AbstractDataset Error: Failed to read data file ...(" + dataFilePathString + ")");
                e.printStackTrace();
            }

        } else {
            System.err.println("AbstractDataset Error: File '" + dataFilePath + "' not found!");
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
                    System.out.println("AbstractDataset: column '" + headers[k] + "' is null.");
                    data.add(new NamedColumn(headers[k], null));
                } else {
                    data.add(new NamedColumn(headers[k], fileData.get(index)));
                }
            }
        } else {
            System.err.println(
                    "AbstractDataset Error: Failed to load data, inconsistencies found between headers and data sizes ...");
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
        System.out.println("AbstractDataset: hash string  '" + hashString + "' was built for '" + name + "' ");
        return hashString;
    }

    private void writeDataFile() {
        if (dataFilePath == null) {
            System.err.println("AbstractDataset Error: cannot write data file because dataFilePath is null");
            return;
        }
        File f = new File(dataFilePath);
        if (f.exists()) {
            // dataFilePath is suppose to have a name with a hash string reflecting actual
            // data, if same name, it means same data!
            System.out.println("AbstractDataset: no need to write file, it already exists.");
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
            System.out.println("AbstractDataset: Writting data '" + name + "' to file...");
            WriteFile.writeMatrix(
                    dataFilePath,
                    nonNullMatrix,
                    ";",
                    "NA",
                    nonNullHeaders.toArray(new String[nonNullHeaders.size()]));
        } catch (IOException e) {
            System.err.println(
                    "AbstractDataset Error: Failed to write data '" +
                            name + "' to file... (" +
                            dataFilePath + ")");
            e.printStackTrace();
        }
    }

    public JSONObject toJSON(BamProject project) {
        JSONObject json = new JSONObject();

        String hashString = computeHashString();

        json.put("name", name);
        json.put("hashString", hashString);

        String[] headers = getHeaders();
        JSONArray headersJson = new JSONArray(headers);
        json.put("headers", headersJson);

        dataFilePath = buildDataFilePath(name, hashString).toString();

        // FIXME: this method may be called too many times... throttle?
        writeDataFile();

        project.registerFile(dataFilePath);

        return json;
    }

    private static Path buildDataFilePath(String name, String hashString) {
        return Path.of(AppConfig.AC.APP_TEMP_DIR, name + "_" + hashString + ".txt");
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
