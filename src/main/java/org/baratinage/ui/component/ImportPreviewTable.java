package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baratinage.ui.container.RowColPanel;

public class ImportPreviewTable extends RowColPanel {

    DataTableModel dataModel;
    JTable table;

    public ImportPreviewTable() {

        dataModel = new DataTableModel();
        table = new JTable(dataModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);

        JScrollPane scrollpane = new JScrollPane(table);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        appendChild(scrollpane);
    }

    public void set(List<String[]> data, String[] headers, String missingValueCode) {
        dataModel.setRawData(data);
        dataModel.fireTableStructureChanged();

        int nCol = data.size();
        JTableHeader tableHeader = table.getTableHeader();
        TableColumnModel tableColModel = tableHeader.getColumnModel();
        NumberCellRenderer numberCellRenderer = new NumberCellRenderer();
        numberCellRenderer.missingValueCode = missingValueCode;
        for (int k = 0; k < nCol; k++) {
            TableColumn tableCol = tableColModel.getColumn(k);
            tableCol.setCellRenderer(numberCellRenderer);
            if (headers != null && headers.length > k) {
                tableCol.setHeaderValue(headers[k]);
            }
        }
        table.updateUI();
    }

    static class NumberCellRenderer extends DefaultTableCellRenderer {

        private boolean isValid = true;
        private boolean isMissing = false;
        public String missingValueCode = "";

        @Override
        public void setValue(Object value) {
            String strValue = value.toString();
            isMissing = strValue.equals(missingValueCode);
            if (!isMissing) {
                try {
                    Double val = Double.parseDouble(strValue);
                    isValid = !(val == null || val.isNaN());
                } catch (Exception e) {
                    isValid = false;
                }
            }
            setText(strValue);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, column);

            if (!isValid || isMissing) {
                Color clr = new Color(255, 125, 125, 200);
                setForeground(clr);
                setFont(getFont().deriveFont(Font.ITALIC));
                if (!isValid) {
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            } else {
                setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
            return this;
        }
    }

    private class DataTableModel extends AbstractTableModel {

        private List<String[]> rawData = new ArrayList<>();

        public boolean setRawData(List<String[]> rawData) {
            int nCol = rawData.size();
            if (nCol == 0) {
                this.rawData = new ArrayList<>();
                return true;
            }
            int nRow = rawData.get(0).length;
            if (nRow == 0) {
                this.rawData = new ArrayList<>();
                return true;
            }
            for (String[] col : rawData) {
                if (col.length != nRow) {
                    System.err.println("Inconsistent number of rows... Aborting.");
                    return false;
                }
            }
            this.rawData = rawData;
            return true;
        }

        @Override
        public int getRowCount() {
            return getColumnCount() > 0 ? rawData.get(0).length : 0;

        }

        @Override
        public int getColumnCount() {
            return rawData.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rawData.get(columnIndex)[rowIndex];
        }

    }

}
