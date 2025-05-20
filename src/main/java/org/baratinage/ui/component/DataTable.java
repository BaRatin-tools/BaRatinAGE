package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

public class DataTable extends SimpleFlowPanel {

    // should create a specific action to export using specific formats
    // (e.g. bareme rating curve format)
    public final SimpleFlowPanel actionPanel;
    public final SimpleFlowPanel toolsPanel;

    private final CustomTableModel model;
    private final JTable table;
    private CustomCellRenderer cellRenderer;

    public DataTable() {
        super(true);
        setPadding(5);
        setGap(5);

        model = new CustomTableModel();

        model.addTableModelListener(
                (e) -> {
                    fireChangeListeners();
                });
        table = new JTable();

        table.setRowHeight(20);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollpane = new JScrollPane(table);

        actionPanel = new SimpleFlowPanel();
        actionPanel.setGap(5);
        JButton exportButton = new JButton();
        exportButton.addActionListener((e) -> {
            saveAsCSV();
        });
        exportButton.setIcon(AppSetup.ICONS.SAVE);
        exportButton.setText("CSV");
        T.t(this, () -> {
            exportButton.setToolTipText(T.text("to_csv"));
        });
        actionPanel.addChild(exportButton, false);
        JButton copyToClipboardButton = new JButton();
        copyToClipboardButton.addActionListener((e) -> {
            copyToCliboard();
        });
        copyToClipboardButton.setIcon(AppSetup.ICONS.COPY);
        T.t(this, () -> {
            copyToClipboardButton.setToolTipText(T.text("to_clipboard"));
        });
        actionPanel.addChild(copyToClipboardButton, 0);

        toolsPanel = new SimpleFlowPanel();
        toolsPanel.setGap(5);

        SimpleCheckbox displayFullPrecision = new SimpleCheckbox();
        T.t(this, () -> {
            displayFullPrecision.setText(T.text("display_full_precision"));
        });
        displayFullPrecision.setSelected(false);
        displayFullPrecision.addChangeListener(
                l -> {
                    cellRenderer.losslessDoubles = displayFullPrecision.isSelected();
                    repaint();
                });
        toolsPanel.addChild(displayFullPrecision, false);

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        defaultPrefDim.width = 300;
        scrollpane.setPreferredSize(defaultPrefDim);

        addChild(actionPanel, false);
        addChild(scrollpane, true);
        addChild(toolsPanel, false);

    }

    public void selectRow(int index) {
        table.setRowSelectionInterval(index, index);
        table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
        table.scrollRectToVisible(table.getCellRect(index, 0, true));
        table.repaint();
    }

    public void updateCellRenderer() {
        cellRenderer = new CustomCellRenderer("yyyy-MM-dd HH:mm:ss");
        table.setDefaultRenderer(LocalDateTime.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);
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
            ConsoleLogger.error("chosen file is null.");
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
            ConsoleLogger.error("failed to write data to CSV file!\n" + e);
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
        addColumn(values, false);
    }

    public void addColumn(double[] values, boolean editable) {
        int n = values.length;
        Double[] boxed = new Double[values.length];
        for (int k = 0; k < n; k++) {
            boxed[k] = values[k];
        }
        addColumn(boxed, editable);
    }

    public void addColumn(int[] values) {
        addColumn(values, false);
    }

    public void addColumn(int[] values, boolean editable) {
        int n = values.length;
        Integer[] boxed = new Integer[values.length];
        for (int k = 0; k < n; k++) {
            boxed[k] = values[k];
        }
        addColumn(boxed, editable);
    }

    public void addColumn(boolean[] values) {
        addColumn(values, false);
    }

    public void addColumn(boolean[] values, boolean editable) {
        int n = values.length;
        Boolean[] boxed = new Boolean[values.length];
        for (int k = 0; k < n; k++) {
            boxed[k] = values[k];
        }
        addColumn(boxed, editable);
    }

    public <A> void addColumn(A[] values) {
        addColumn(values, false);
    }

    public <A> void addColumn(A[] values, boolean editable) {
        model.addColumn(values, editable);
    }

    public Object[] getColumn(int colIndex) {
        return model.getColumnValues(colIndex);
    }

    public void setHeader(int colIndex, String headerText) {
        int nCol = table.getColumnCount();
        if (colIndex < 0 || colIndex >= nCol) {
            ConsoleLogger.error("Cannot set header text, colIndex is invalid!");
            return;
        }
        TableColumn tableColumn = table.getColumnModel().getColumn(colIndex);
        tableColumn.setHeaderValue(headerText);
    }

    public void setHeaderWidth(int colIndex, int width) {
        int nCol = table.getColumnCount();
        if (colIndex < 0 || colIndex >= nCol) {
            ConsoleLogger.error("cannot set header width, colIndex is invalid!");
            return;
        }
        TableColumn tableColumn = table.getColumnModel().getColumn(colIndex);
        tableColumn.setPreferredWidth(width);
    }

    public void setHeaderWidth(int width) {
        int nCol = table.getColumnCount();
        for (int k = 0; k < nCol; k++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(k);
            tableColumn.setPreferredWidth(width);
        }
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    private void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    private static class CustomTableModel extends AbstractTableModel {

        private static record Column(Object[] values, boolean editable) {
            public Class<?> getColumnClass() {
                if (values.length > 0) {
                    return values[0].getClass();
                }
                return Void.class;
            }
        }

        private final List<Column> columns;
        private int nRow;

        public CustomTableModel() {
            columns = new ArrayList<>();
            nRow = -1;
        }

        public void clearColumns() {
            columns.clear();
            nRow = -1;
        }

        private boolean isLengthValid(int length) {
            if (nRow == -1) {
                nRow = length;
                return true;
            } else {
                if (nRow != length) {
                    ConsoleLogger.error("cannot add a column of length " +
                            length + "! Length " +
                            nRow + " expected.");
                    return false;
                }
            }
            return true;
        }

        public <A> void addColumn(A[] values, boolean editable) {
            if (isLengthValid(values.length)) {
                Column col = new Column(values, editable);
                columns.add(col);
            }
        }

        @Override
        public Class<?> getColumnClass(int index) {
            if (index >= 0 && index < getColumnCount()) {
                return columns.get(index).getColumnClass();
            } else

            {
                return Void.class;
            }
        }

        @Override
        public int getRowCount() {
            return nRow;
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int colIndex) {
            if (rowIndex >= 0 && rowIndex < getRowCount() && colIndex >= 0 && colIndex < getColumnCount()) {
                Column c = columns.get(colIndex);
                Object value = c.values()[rowIndex];
                if (c.getColumnClass().equals(Integer.class)) {
                    Integer valueInt = (Integer) value;
                    if (valueInt.equals(AppSetup.CONFIG.INT_MISSING_VALUE)) {
                        return null;
                    }
                }
                return value;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int colIndex) {

            if (rowIndex >= 0 && rowIndex < getRowCount() && colIndex >= 0 && colIndex < getColumnCount()) {
                Column c = columns.get(colIndex);
                if (!c.editable) {
                    ConsoleLogger.error("Cannot set a value in a non-editable column.");
                    return;
                }

                if (c.getColumnClass() != value.getClass()) {
                    ConsoleLogger.error("Cannot set a value of a type different from the column type.");
                    return;
                }
                c.values()[rowIndex] = value;
                fireTableDataChanged();
            }

        }

        @Override
        public boolean isCellEditable(int rowIndex, int colIndex) {
            if (colIndex >= 0 && colIndex < getColumnCount()) {
                return columns.get(colIndex).editable();
            }
            return false;
        }

        public Object[] getColumnValues(int colIndex) {
            if (colIndex >= 0 && colIndex < getColumnCount()) {
                return columns.get(colIndex).values();
            }
            return null;
        }

    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {

        private DateTimeFormatter dateTimeFormatter;
        public boolean losslessDoubles = false;

        public CustomCellRenderer(String printFormat) {
            dateTimeFormatter = DateTimeFormatter.ofPattern(printFormat);
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            if (value instanceof Number) {
                setHorizontalAlignment(JLabel.RIGHT);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            if (value instanceof LocalDateTime) {
                LocalDateTime ldt = (LocalDateTime) value;
                value = ldt.format(dateTimeFormatter);
            } else if (value instanceof Double) {
                Double d = (Double) value;
                if (!d.isNaN()) {
                    value = Misc.formatNumber(d, losslessDoubles);
                } else {
                    value = "";
                }
            }

            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            setBorder(new CompoundBorder(getBorder(), new EmptyBorder(0, 5, 0, 5)));
            return this;
        }

    }

}
