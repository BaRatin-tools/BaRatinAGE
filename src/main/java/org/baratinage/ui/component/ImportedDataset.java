package org.baratinage.ui.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.baratinage.ui.bam.IDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;

public class ImportedDataset implements IDataset {

    protected String name;
    protected List<double[]> data;
    protected String[] headers;
    protected int nCol;
    protected int nRow;

    protected String tempDataFileName;

    public ImportedDataset() {
    }

    public ImportedDataset(
            String name,
            List<double[]> data,
            String[] headers) {
        setDatasetName(name);
        setData(data, headers);
    }

    public void setDatasetName(String name) {
        this.name = name;
    }

    public void setData(List<double[]> data, String[] headers) {
        this.nCol = headers.length;
        this.nRow = data.get(0).length;

        this.data = data;
        this.headers = headers;
    }

    public List<double[]> getData() {
        // Note: only copy the main data container!
        List<double[]> dataCopy = new ArrayList<>();
        for (int k = 0; k < data.size(); k++) {
            dataCopy.add(data.get(k));
        }
        return dataCopy;
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

    public void setDataFromFile(String dataFilePath) {
        String headerLine;
        try {
            headerLine = ReadFile.getLines(dataFilePath, 1, false)[0];
            headers = ReadFile.parseString(headerLine, ";", false);
            nCol = headers.length;
        } catch (IOException e1) {
            System.out.println("Failed to read data file ...(" + getDatasetName() + ")");
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
            nRow = data.get(0).length;
        } catch (IOException e2) {
            System.out.println("Failed to read data file ...(" + getDatasetName() + ")");
            e2.printStackTrace();
        }
    }

    @Override
    public String[] getColumnNames() {
        return this.headers;
    }

    @Override
    public double[] getColumn(String name) {
        for (int k = 0; k < nCol; k++) {
            if (headers[k].equals(name)) {
                return data.get(k);
            }
        }
        return null;
    }

    @Override
    public double[] getColumn(int index) {
        if (index >= nCol)
            return null;
        return data.get(index);
    }

    @Override
    public double[] getRow(int index) {
        double[] row = new double[nCol];
        for (int k = 0; k < nCol; k++) {
            row[k] = data.get(k)[index];
        }
        return row;
    }

    @Override
    public int getNumberOfColumns() {
        return nCol;
    }

    @Override
    public int getNumberOfRows() {
        return nRow;
    }

    @Override
    public String getDatasetName() {
        return name;
    }

}