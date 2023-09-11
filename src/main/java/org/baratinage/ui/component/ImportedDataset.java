package org.baratinage.ui.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.baratinage.ui.bam.IDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;

public class ImportedDataset implements IDataset {

    private String name;
    private List<double[]> data;
    private String[] headers;

    public ImportedDataset(String name, String dataFilePath) {
        this.name = name;
        data = new ArrayList<>();
        headers = new String[] {};
        try {
            String headerLine = ReadFile.getLines(dataFilePath, 1, false)[0];
            headers = ReadFile.parseString(headerLine, ";", false);
        } catch (IOException e1) {
            System.out.println("Failed to read data file ...(" + dataFilePath + ")");
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
            System.out.println("Failed to read data file ...(" + dataFilePath + ")");
            e2.printStackTrace();
        }
    }

    public ImportedDataset(
            String name,
            List<double[]> data,
            String[] headers) {
        this.name = name;
        this.data = data;
        this.headers = headers;
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
        return headers;
    }

    @Override
    public int hashCode() {
        Integer hashSum = 0;
        for (int k = 0; k < data.size(); k++) {
            hashSum += headers[k].hashCode();
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
                    headers);
        } catch (IOException e) {
            System.err.println("Failed to write data to file... (" + getDatasetName() + ")");
            e.printStackTrace();
        }
    }

    @Override
    public String[] getColumnNames() {
        return this.headers;
    }

    @Override
    public double[] getColumn(String name) {
        int nCol = getNumberOfColumns();
        for (int k = 0; k < nCol; k++) {
            if (headers[k].equals(name)) {
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

}