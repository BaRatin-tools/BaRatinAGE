package org.baratinage.ui.bam;

public interface IDataset {
    public String getName();

    public String[] getColumnNames();

    public double[] getColumn(String name);

    public double[] getColumn(int index);

    public double[] getRow(int index);

    public int getNumberOfColumns();

    public int getNumberOfRows();
}