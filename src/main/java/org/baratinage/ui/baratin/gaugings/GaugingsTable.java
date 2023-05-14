package org.baratinage.ui.baratin.gaugings;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.baratinage.ui.container.RowColPanel;

public class GaugingsTable extends RowColPanel {

    private GaugingsTableModel dataModel;
    private JTable table;

    public GaugingsTable() {

        dataModel = new GaugingsTableModel();
        table = new JTable(dataModel);
        setHeaders();

        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);

        JScrollPane scrollpane = new JScrollPane(table);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        appendChild(scrollpane);

    }

    public void set(GaugingsDataset gaugingDataset) {
        dataModel.set(gaugingDataset);
        dataModel.fireTableStructureChanged();
        setHeaders();
    }

    public void setHeaders() {
        JTableHeader tableHeader = table.getTableHeader();
        TableColumnModel tableColModel = tableHeader.getColumnModel();
        tableColModel.getColumn(0).setHeaderValue("Hauteur d'eau");
        tableColModel.getColumn(1).setHeaderValue("Débit");
        tableColModel.getColumn(2).setHeaderValue("Incertitude (%)");
        tableColModel.getColumn(3).setHeaderValue("Actif ?");
    }

    public AbstractTableModel getTableModel() {
        return dataModel;
    }

    private class GaugingsTableModel extends AbstractTableModel {

        // private double[] stageData;
        // private double[] streamflowData;
        // private double[] streamflowUncertaintyData;
        // private boolean[] activeGaugings;
        // private int nGaugings = 0;
        GaugingsDataset gaugingDataset;

        public void set(GaugingsDataset gaugingDataset) {
            this.gaugingDataset = gaugingDataset;
            // int n = stageData.length;
            // if (streamflowData.length != n)
            // return;
            // if (streamflowUncertaintyData.length != n)
            // return;
            // nGaugings = n;

            // // this.stageData = stageData;
            // // this.streamflowData = streamflowData;
            // // this.streamflowUncertaintyData = streamflowUncertaintyData;

            // activeGaugings = new boolean[n];
            // for (int k = 0; k < n; k++) {
            // activeGaugings[k] = true;
            // }
        }

        @Override
        public Class<?> getColumnClass(int index) {
            return index == 3 ? Boolean.class : Double.class;
        }

        @Override
        public int getRowCount() {
            return gaugingDataset == null ? 0 : gaugingDataset.getNumberOfRows();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex >= 4 || rowIndex >= gaugingDataset.getNumberOfRows()) {
                return -9999;
            }
            Gauging gauging = gaugingDataset.getGauging(rowIndex);
            if (columnIndex == 0)
                return gauging.stage;
            if (columnIndex == 1)
                return gauging.discharge;
            if (columnIndex == 2)
                return gauging.dischargeUncertainty;
            if (columnIndex == 3)
                return gauging.activeState;
            return -9999;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Gauging gauging = gaugingDataset.getGauging(rowIndex);
            if (columnIndex < 3 && value.getClass() == Double.class) {
                Double doubleValue = (Double) value;
                if (columnIndex == 0) {
                    gauging.stage = doubleValue;
                } else if (columnIndex == 1) {
                    gauging.discharge = doubleValue;
                } else if (columnIndex == 2) {
                    gauging.dischargeUncertainty = doubleValue;
                }
            } else if (columnIndex == 3) {
                if (value.getClass() == Boolean.class) {
                    gauging.activeState = (Boolean) value;
                }
            }
            gaugingDataset.setGauging(rowIndex, gauging);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3;
        }
    }
}
