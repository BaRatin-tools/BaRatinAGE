package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.baratinage.ui.container.RowColPanel;

public class DataTable extends RowColPanel {

    private final CustomTableModel model;
    private final JTable table;

    public DataTable() {
        model = new CustomTableModel();
        table = new JTable();

        table.setModel(model);
        // table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setDefaultRenderer(LocalDateTime.class, new DateTimeCellRenderer("yyyy-MM-dd HH:mm:ss"));

        JScrollPane scrollpane = new JScrollPane(table);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        defaultPrefDim.width = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        appendChild(scrollpane);
    }

    public void updateData() {
        model.fireTableStructureChanged();
    }

    public void updateHeader() {
        table.getTableHeader().updateUI();
    }

    public void clearColumns() {
        model.clearColumns();
    }

    public void addColumn(double[] values) {
        model.addColumn(values);
    }

    public void addColumn(int[] values) {
        model.addColumn(values);
    }

    public void addColumn(LocalDateTime[] values) {
        model.addColumn(values);
    }

    public void setHeader(int colIndex, String headerText) {
        int nCol = table.getColumnCount();
        if (colIndex < 0 || colIndex >= nCol) {
            System.err.println("DataTable Error: colIndex is invalid!");
            return;
        }
        TableColumn tableColumn = table.getColumnModel().getColumn(colIndex);
        tableColumn.setHeaderValue(headerText);
    }

    public void setHeaderWidth(int colIndex, int width) {
        int nCol = table.getColumnCount();
        if (colIndex < 0 || colIndex >= nCol) {
            System.err.println("DataTable Error: colIndex is invalid!");
            return;
        }
        TableColumn tableColumn = table.getColumnModel().getColumn(colIndex);
        tableColumn.setPreferredWidth(width);
    }

    private static class CustomTableModel extends AbstractTableModel {

        private static enum TYPE {
            INT(Integer.class), DOUBLE(Double.class), TIME(LocalDateTime.class);

            public final Class<?> c;

            private TYPE(Class<?> c) {
                this.c = c;
            }
        };

        private static record ColSettings(TYPE type, double[] d, int[] i, LocalDateTime[] t) {

        };

        private final List<ColSettings> columns;
        private int nRow;
        private int nCol;

        public CustomTableModel() {
            columns = new ArrayList<>();
            nRow = -1;
            nCol = 0;
        }

        public void clearColumns() {
            columns.clear();
            nRow = -1;
            nCol = 0;
        }

        private boolean isLengthValid(int length) {
            if (nRow == -1) {
                nRow = length;
                return true;
            } else {
                if (nRow != length) {
                    System.err.println("DataTable Error: cannot add a column of length " + length + "! Length " + nRow
                            + " expected.");
                    return false;
                }
            }
            return true;
        }

        private void addColumn(double[] d, int[] i, LocalDateTime[] t) {
            TYPE type = TYPE.DOUBLE;
            int n = -1;
            if (d != null) {
                type = TYPE.DOUBLE;
                n = d.length;
            } else if (i != null) {
                type = TYPE.INT;
                n = i.length;
            } else if (t != null) {
                type = TYPE.TIME;
                n = t.length;
            }
            if (n >= 0) {
                if (isLengthValid(n)) {
                    columns.add(new ColSettings(type, d, i, t));
                    nCol++;
                }
            }
        }

        public void addColumn(double[] values) {
            addColumn(values, null, null);
        }

        public void addColumn(int[] values) {
            addColumn(null, values, null);
        }

        public void addColumn(LocalDateTime[] values) {
            addColumn(null, null, values);
        }

        @Override
        public Class<?> getColumnClass(int index) {
            if (index >= 0 && index < nCol) {
                return columns.get(index).type.c;
            } else {
                return Void.class;
            }
        }

        @Override
        public int getRowCount() {
            return nRow;
        }

        @Override
        public int getColumnCount() {
            return nCol;
        }

        @Override
        public Object getValueAt(int rowIndex, int colIndex) {
            if (rowIndex >= 0 && rowIndex < nRow && colIndex >= 0 && colIndex < nCol) {
                ColSettings c = columns.get(colIndex);
                if (c.type == TYPE.DOUBLE) {
                    return c.d[rowIndex];
                } else if (c.type == TYPE.INT) {
                    return c.i[rowIndex];
                } else if (c.type == TYPE.TIME) {
                    return c.t[rowIndex];
                }
            }
            return null;
        }

    }

    private static class DateTimeCellRenderer extends DefaultTableCellRenderer {

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
