package org.baratinage.ui.component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.baratinage.App;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IDataset;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.WriteFile;
import org.json.JSONObject;

// FIXME: should this class really extend BamItem?
public class ImportedDataset extends BamItem implements IDataset {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    // private String name;
    protected List<double[]> data;
    protected String[] headers;
    protected int nCol;
    protected int nRow;

    protected String tempDataFileName;

    public ImportedDataset() {
        super(TYPE);
    }

    public ImportedDataset(
            String name,
            List<double[]> data,
            String[] headers) {
        super(TYPE);

        this.nCol = headers.length;
        this.nRow = data.get(0).length;

        this.data = data;
        this.headers = headers;

        setName(name);
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
    public void parentHasChanged(BamItem parent) {
        System.out.println("UNIMPLEMENTED: parentHasChanged! ");
    }

    @Override
    public String[] getTempDataFileNames() {
        return tempDataFileName == null ? new String[] {} : new String[] { tempDataFileName };
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("name", getName());

        tempDataFileName = UUID.randomUUID().toString() + ".txt";
        String tempDataFilePath = Path.of(App.TEMP_DIR, tempDataFileName).toString();

        try {
            WriteFile.writeMatrix(
                    tempDataFilePath,
                    data,
                    ";",
                    "NA",
                    headers);
        } catch (IOException e) {
            System.err.println("Failed to write gaugings data to file... (" + getName() + ")");
            e.printStackTrace();
        }

        json.put("dataFileName", tempDataFileName);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        String name = json.getString("name");
        setName(name);

        tempDataFileName = json.getString("dataFileName");
        if (tempDataFileName == null) {
            System.err.println("Missing tempDataFileName!");
            return;
        }

        String tempDataFilePath = Path.of(App.TEMP_DIR, tempDataFileName).toString();

        String headerLine;
        try {
            headerLine = ReadFile.getLines(tempDataFilePath, 1, false)[0];
            headers = ReadFile.parseString(headerLine, ";", false);
            nCol = headers.length;
        } catch (IOException e1) {
            System.out.println("Failed to read gauging data file ...(" + getName() + ")");
            e1.printStackTrace();
        }

        try {
            data = ReadFile.readMatrix(
                    tempDataFilePath,
                    ";",
                    1,
                    Integer.MAX_VALUE,
                    "NA",
                    false,
                    false);
            nRow = data.get(0).length;
        } catch (IOException e2) {
            System.out.println("Failed to read gauging data file ...(" + getName() + ")");
            e2.printStackTrace();
        }

    }

}