package org.baratinage.ui.component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;
import org.json.JSONObject;

public abstract class ImportedDataset implements IDataset {

    private String name;
    protected List<double[]> data;
    protected List<String> headers;

    public ImportedDataset(
            String name,
            List<double[]> data,
            String... headers) {
        this.name = name;
        this.data = data;
        this.headers = new ArrayList<>(Arrays.asList(headers));
    }

    public ImportedDataset(JSONObject json) {

        name = json.getString("name");

        int hashCode = json.getInt("hashCode");

        Path dataFilePath = buildDataFilePath(name, hashCode);

        if (Files.exists(dataFilePath)) {
            System.out.println("ImportedDataset: Reading file '" + dataFilePath + "'...");
            setDataFromFile(dataFilePath.toString());
        } else {
            System.err.println("ImportedDataset Error: File '" + dataFilePath + "' not found!");
        }
    }

    private void setDataFromFile(String dataFilePath) {
        // this.name = name;
        data = new ArrayList<>();
        headers = new ArrayList<>();
        try {
            String headerLine = ReadFile.getLines(dataFilePath, 1, false)[0];
            headers = new ArrayList<>(Arrays.asList(ReadFile.parseString(headerLine, ";",
                    false)));
        } catch (IOException e1) {
            System.out.println("ImportedDataset: Failed to read data file ...(" +
                    dataFilePath + ")");
            e1.printStackTrace();
        }

        try {
            data = ReadFile.readMatrix(
                    dataFilePath,
                    ";",
                    1,
                    Integer.MAX_VALUE,
                    "NA",
                    false,
                    false);
        } catch (IOException e2) {
            System.out.println("ImportedDataset: Failed to read data file ...(" +
                    dataFilePath + ")");
            e2.printStackTrace();
        }
    }

    public List<double[]> getData() {
        // Note/warning: only copy the main data container!
        List<double[]> dataCopy = new ArrayList<>();
        for (int k = 0; k < data.size(); k++) {
            dataCopy.add(data.get(k));
        }
        return dataCopy;
    }

    public String[] getHeaders() {
        return headers.toArray(new String[headers.size()]);
    }

    @Override
    public int hashCode() {
        Integer hashSum = 0;
        for (int k = 0; k < data.size(); k++) {
            hashSum += headers.get(k).hashCode();
            int s = 0;
            for (Double d : data.get(k)) {
                s += d.hashCode();
            }
            hashSum += s;
        }
        int hc = hashSum.hashCode();
        return hc < 0 ? hc * -1 : hc;
    }

    public void writeDataFile(String dataFilePath) {
        try {
            WriteFile.writeMatrix(
                    dataFilePath,
                    data,
                    ";",
                    "NA",
                    getHeaders());
        } catch (IOException e) {
            System.err.println("ImportedDataset Error: Failed to write data to file... (" + getDatasetName() + ")");
            e.printStackTrace();
        }
    }

    @Override
    public String[] getColumnNames() {
        return getHeaders();
    }

    @Override
    public double[] getColumn(String name) {
        int nCol = getNumberOfColumns();
        for (int k = 0; k < nCol; k++) {
            if (headers.get(k).equals(name)) {
                return data.get(k);
            }
        }
        return null;
    }

    @Override
    public double[] getColumn(int index) {
        int nCol = getNumberOfColumns();
        return data == null || index >= nCol ? null : data.get(index);
    }

    @Override
    public double[] getRow(int index) {
        int nRow = getNumberOfRows();
        int nCol = getNumberOfColumns();
        if (data == null || index >= nRow) {
            return null;
        }
        double[] row = new double[nCol];
        for (int k = 0; k < nCol; k++) {
            row[k] = data.get(k)[index];
        }
        return row;
    }

    @Override
    public int getNumberOfColumns() {
        return data.size();
    }

    @Override
    public int getNumberOfRows() {
        return data.size() == 0 ? 0 : data.get(0).length;
    }

    @Override
    public String getDatasetName() {
        return name;
    }

    public JSONObject toJSON(BamProject project) {
        JSONObject json = new JSONObject();

        String name = getDatasetName();
        int hashCode = hashCode();

        json.put("name", name);
        json.put("hashCode", hashCode);

        String dataFilePath = buildDataFilePath(name, hashCode).toString();

        writeDataFile(dataFilePath);
        project.registerFile(dataFilePath);

        return json;
    }

    // public void fromJSON(JSONObject json) {

    // }

    private static Path buildDataFilePath(String name, int hashCode) {
        return Path.of(AppConfig.AC.APP_TEMP_DIR, name + "_" + hashCode + ".txt");
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