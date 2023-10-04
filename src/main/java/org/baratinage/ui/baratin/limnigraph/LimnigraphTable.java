package org.baratinage.ui.baratin.limnigraph;

import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;

public class LimnigraphTable extends RowColPanel {

    private LimniTableModel dataModel;
    private JTable table;

    public LimnigraphTable() {

        dataModel = new LimniTableModel();
        table = new JTable(dataModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setHeaders(new String[] { "data/time", "h" });

        table.setDefaultRenderer(LocalDateTime.class, new DateTimeCellRenderer("yyyy-MM-dd HH:mm:ss"));

        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);

        JScrollPane scrollpane = new JScrollPane(table);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        defaultPrefDim.width = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        appendChild(scrollpane);

        T.t(this, (limniTable) -> {
            if (limniTable.dataModel.limniDataset == null) {
                limniTable.setHeaders(new String[] { "" });
                return;
            }
            limniTable.setHeaders(limniTable.dataModel.limniDataset.getHeaders());
        });

    }

    public void set(LimnigraphDataset limniDataset) {
        dataModel.set(limniDataset);
        dataModel.fireTableStructureChanged();
        setHeaders(limniDataset.getHeaders());
    }

    public TableColumnModel getTableColumnModel() {
        JTableHeader tableHeader = table.getTableHeader();
        return tableHeader.getColumnModel();
    }

    public void setHeaders(String[] headers) {
        TableColumnModel tableColModel = getTableColumnModel();

        for (int k = 0; k < headers.length; k++) {
            tableColModel.getColumn(k).setHeaderValue(headers[k]);
        }
        tableColModel.getColumn(0).setHeaderValue(T.text("date_time"));

        table.getTableHeader().updateUI();
    }

    public AbstractTableModel getTableModel() {
        return dataModel;
    }

    private class LimniTableModel extends AbstractTableModel {

        LimnigraphDataset limniDataset;

        public void set(LimnigraphDataset limniDataset) {
            this.limniDataset = limniDataset;
        }

        @Override
        public Class<?> getColumnClass(int index) {
            return index == 0 ? LocalDateTime.class : Double.class;
        }

        @Override
        public int getRowCount() {
            return limniDataset == null ? 0 : limniDataset.getNumberOfRows();
        }

        @Override
        public int getColumnCount() {
            return limniDataset == null ? 2 : limniDataset.getNumberOfColumns();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= limniDataset.getNumberOfRows()) {
                return -9999;
            }
            if (columnIndex - 1 >= limniDataset.getNumberOfColumns()) {
                return -9999;
            }

            if (columnIndex == 0)
                return limniDataset.getDateTime(rowIndex);
            List<double[]> data = limniDataset.getStageMatrix();
            if (data.size() >= columnIndex) {
                double[] column = data.get(columnIndex - 1);
                if (column.length > rowIndex) {
                    return column[rowIndex];
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            // do nothing for now
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    private class DateTimeCellRenderer extends DefaultTableCellRenderer {

        private DateTimeFormatter formatter;

        public DateTimeCellRenderer(String printFormat) {
            formatter = DateTimeFormatter.ofPattern(printFormat);
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof LocalDateTime) {
                LocalDateTime ldt = (LocalDateTime) value;
                value = ldt.format(formatter);
            }
            return super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
        }
    }

}
