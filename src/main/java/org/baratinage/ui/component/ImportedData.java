package org.baratinage.ui.component;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IDataset;

public class ImportedData extends BamItem implements IDataset {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    public ImportedData() {
        super(TYPE);
    }

    @Override
    public String[] getColumnNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColumnNames'");
    }

    @Override
    public double[] getColumn(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColumn'");
    }

    @Override
    public double[] getColumn(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColumn'");
    }

    @Override
    public double[] getRow(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRow'");
    }

    @Override
    public int getNumgeberOfColumns() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumgeberOfColumns'");
    }

    @Override
    public int getNumberOfRows() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfRows'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parentHasChanged'");
    }

    @Override
    public String toJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJsonString'");
    }

    @Override
    public void fromJsonString(String jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJsonString'");
    }

}