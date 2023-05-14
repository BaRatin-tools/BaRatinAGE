package org.baratinage.ui.component;

import java.util.List;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IDataset;
import org.json.JSONObject;

public class ImportedDataset extends BamItem implements IDataset {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    // private String name;
    protected List<double[]> data;
    protected String[] headers;
    protected int nCol;
    protected int nRow;

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

    // public void setHeaders(String[] headers) {

    // }

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
    public JSONObject toJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    }

    @Override
    public void fromJSON(JSONObject json) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

}