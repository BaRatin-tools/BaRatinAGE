package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class DataTable extends RowColPanel {

    private final CustomTableModel model;
    private final JTable table;
    private CustomCellRenderer cellRenderer;

    public DataTable() {
        super(AXIS.COL);
        setPadding(5);
        setGap(5);

        model = new CustomTableModel();
        table = new JTable();

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollpane = new JScrollPane(table);

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setMainAxisAlign(ALIGN.START);
        JButton exportButton = new JButton();
        exportButton.addActionListener((e) -> {
            saveAsCSV();
        });
        exportButton.setIcon(SvgIcon.buildFeatherAppImageIcon("save.svg"));
        exportButton.setText("CSV");
        T.t(this, () -> {
            exportButton.setToolTipText(T.text("to_csv"));
        });
        actionPanel.appendChild(exportButton);
        JButton copyToClipboardButton = new JButton();
        copyToClipboardButton.addActionListener((e) -> {
            copyToCliboard();
        });
        copyToClipboardButton.setIcon(SvgIcon.buildFeatherAppImageIcon("copy.svg"));
        T.t(this, () -> {
            copyToClipboardButton.setToolTipText(T.text("to_clipboard"));
        });
        actionPanel.appendChild(copyToClipboardButton);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        defaultPrefDim.width = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        appendChild(actionPanel, 0);
        appendChild(scrollpane, 1);

    }

    public void updateCellRenderer() {
        cellRenderer = new CustomCellRenderer("yyyy-MM-dd HH:mm:ss");
        table.setDefaultRenderer(LocalDateTime.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
    }

    private String getStringValue(int row, int col) {
        Object obj = table.getValueAt(row, col);
        if (obj == null) {
            return "";
        } else if (obj instanceof LocalDateTime) {
            return cellRenderer.dateTimeFormatter.format((LocalDateTime) obj);
        } else {
            return obj.toString();
        }
    }

    // private void buildSaveData
    private void copyToCliboard() {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(new StringSelection(buildDataString()), null);
    }

    private void saveAsCSV() {
        File file = CommonDialog.saveFileDialog(null, T.text("csv_format"), "csv");
        if (file == null) {
            System.err.println("DataTable Error: chosen file is null.");
            return;
        }
        String data = buildDataString();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("DataTable Error: failed to write data to CSV file!");
            e.printStackTrace();
        }
    }

    private String buildDataString() {
        // creating row wise data matrix;
        int nCol = model.getColumnCount();
        int nRow = model.getRowCount();
        // List<String[]> rows = new ArrayList<>();
        String[] rows = new String[nRow + 1];
        String[] headerRow = new String[nCol];
        for (int k = 0; k < nCol; k++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(k);
            Object headerValue = tableColumn.getHeaderValue();
            if (headerValue instanceof String) {
                headerRow[k] = (String) headerValue;
            } else {
                headerRow[k] = table.getColumnName(k);
            }
            headerRow[k] = "\"" + headerRow[k] + "\"";
        }
        rows[0] = String.join(",", headerRow);
        for (int i = 0; i < nRow; i++) {
            String[] row = new String[nCol];
            for (int j = 0; j < nCol; j++) {
                row[j] = getStringValue(i, j);
            }
            rows[i + 1] = String.join(",", row);
        }

        return String.join("\n", rows);
    }

    public void updateData() {
        updateCellRenderer();
        model.fireTableStructureChanged();
    }

    public void updateHeader() {
        updateCellRenderer();
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

    public void addColumn(String[] values) {
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

    public void autosetHeadersWidths(int min, int max) {
        int nCol = table.getColumnCount();
        Graphics g = table.getGraphics();
        if (g == null) {
            g = AppConfig.AC.APP_MAIN_FRAME.getGraphics();
            if (g == null) {
                System.err.println("DataTable Error: autoset width impossible, graphics is null");
                return;
            }
        }
        FontMetrics fm = g.getFontMetrics();
        for (int k = 0; k < nCol; k++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(k);
            // find a way to autocompute column width
            String text = (String) tableColumn.getHeaderValue();
            int aw = fm.stringWidth(text);
            int pw = Math.max(Math.min(aw, max), min);
            tableColumn.setPreferredWidth(pw);
        }
    }

    private static class CustomTableModel extends AbstractTableModel {

        private static enum TYPE {
            INT(Integer.class), DOUBLE(Double.class), TIME(LocalDateTime.class), STRING(String.class);

            public final Class<?> c;

            private TYPE(Class<?> c) {
                this.c = c;
            }
        };

        private static record ColSettings(TYPE type, double[] d, int[] i, LocalDateTime[] t, String[] s) {

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

        private void addColumn(double[] d, int[] i, LocalDateTime[] t, String[] s) {
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
            } else if (s != null) {
                type = TYPE.STRING;
                n = s.length;
            }
            if (n >= 0) {
                if (isLengthValid(n)) {
                    columns.add(new ColSettings(type, d, i, t, s));
                    nCol++;
                }
            }
        }

        public void addColumn(double[] values) {
            addColumn(values, null, null, null);
        }

        public void addColumn(int[] values) {
            addColumn(null, values, null, null);
        }

        public void addColumn(LocalDateTime[] values) {
            addColumn(null, null, values, null);
        }

        public void addColumn(String[] values) {
            addColumn(null, null, null, values);
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
                } else if (c.type == TYPE.STRING) {
                    return c.s[rowIndex];
                }
            }
            return null;
        }

    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {

        private DateTimeFormatter dateTimeFormatter;
        private DecimalFormat scientificFormatter;
        private DecimalFormat numberFormatter;

        public CustomCellRenderer(String printFormat) {
            dateTimeFormatter = DateTimeFormatter.ofPattern(printFormat);
            scientificFormatter = new DecimalFormat("0.00E0");
            numberFormatter = new DecimalFormat();
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof LocalDateTime) {
                LocalDateTime ldt = (LocalDateTime) value;
                value = ldt.format(dateTimeFormatter);
            } else if (value instanceof Double) {
                Double d = (Double) value;
                if (!d.isNaN()) {
                    Double absD = Math.abs(d);
                    if (absD != 0 && (absD < 1e-4 || absD > 1e4)) {
                        value = scientificFormatter.format(d);
                    } else {
                        value = numberFormatter.format(d);
                    }
                } else {
                    value = "";
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
        }
    }

}
