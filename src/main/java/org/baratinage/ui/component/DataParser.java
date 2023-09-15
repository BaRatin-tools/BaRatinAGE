package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class DataParser extends RowColPanel {

    private enum COL_TYPE {
        DOUBLE, DATETIME
    }

    private record ColSettings(
            COL_TYPE type,
            String dateTimeFormat) {
    };

    private String missingValueCode;
    // private String[] headers;
    private List<String[]> rawData;

    private DataTableModel dataTableModel;
    private JTable dataTable;
    private Map<Integer, ColSettings> colSettings = new HashMap<>();

    private static final Function<String, Double> TO_DOUBLE = (String v) -> {
        try {
            Double d = Double.parseDouble(v);
            return d == null ? Double.NaN : d;
        } catch (Exception e) {
            return Double.NaN;
        }
    };

    private static final Predicate<String> DOUBLE_VALIDATOR = (String v) -> {
        try {
            Double d = Double.parseDouble(v);
            return !(d == null || d.isNaN());
        } catch (Exception e) {
            return false;
        }
    };

    private static final CustomCellRenderer IGNORE_COL_RENDERER = new CustomCellRenderer();

    private static Predicate<String> buildDateTimeValidator(String format) {
        return (String v) -> {
            try {
                LocalDateTime.parse(v, DateTimeFormatter.ofPattern(format));
                return true;
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static Function<String, LocalDateTime> buildDateTimeConverter(String format) {
        return (String v) -> {
            try {
                LocalDateTime dataTime = LocalDateTime.parse(v, DateTimeFormatter.ofPattern(format));
                return dataTime;
            } catch (Exception e) {
                return null;
            }
        };
    }

    public DataParser() {

        setPadding(5);
        dataTableModel = new DataTableModel();

        dataTable = new JTable(dataTableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.setCellSelectionEnabled(true);

        JScrollPane scrollpane = new JScrollPane(dataTable);
        scrollpane.setBorder(BorderFactory.createEmptyBorder());

        Dimension defaultPrefDim = scrollpane.getPreferredSize();
        defaultPrefDim.height = 300;
        scrollpane.setPreferredSize(defaultPrefDim);
        appendChild(scrollpane);

    }

    public void setRawData(List<String[]> rawData, String[] headers, String missingValueCode) {

        this.rawData = rawData;
        this.missingValueCode = missingValueCode;

        dataTableModel.setRawData(rawData);

        int nCol = rawData.size();
        JTableHeader tableHeader = dataTable.getTableHeader();
        TableColumnModel tableColModel = tableHeader.getColumnModel();

        CustomCellRenderer doubleColRenderer = new CustomCellRenderer(missingValueCode, DOUBLE_VALIDATOR);

        for (int k = 0; k < nCol; k++) {
            TableColumn tableCol = tableColModel.getColumn(k);
            ColSettings cs = colSettings.get(k);
            if (cs == null) {
                tableCol.setCellRenderer(IGNORE_COL_RENDERER);
            } else if (cs.type == COL_TYPE.DOUBLE) {
                tableCol.setCellRenderer(doubleColRenderer);
            } else if (cs.type == COL_TYPE.DATETIME) {
                Predicate<String> dataTimeValidator = buildDateTimeValidator(cs.dateTimeFormat);
                tableCol.setCellRenderer(new CustomCellRenderer(missingValueCode, dataTimeValidator));
            }
            if (headers != null && headers.length > k) {
                tableCol.setHeaderValue(headers[k]);
            }
        }
    }

    public void ignoreAll() {
        colSettings.clear();
    }

    public void ignoreCol(int colIndex) {
        colSettings.remove(colIndex);
    }

    public void setAsDoubleCol(int colIndex) {
        colSettings.put(colIndex, new ColSettings(COL_TYPE.DOUBLE, ""));
    }

    public void setAsDateTimeCol(int colIndex, String format) {
        colSettings.put(colIndex, new ColSettings(COL_TYPE.DATETIME, format));
    }

    public boolean testColValidity(int colIndex) {
        if (colIndex < 0 || colIndex > rawData.size()) {
            return false;
        }
        ColSettings cs = colSettings.get(colIndex);
        if (cs == null) {
            return true;
        } else if (cs.type == COL_TYPE.DOUBLE) {
            for (String v : rawData.get(colIndex)) {
                if (!DOUBLE_VALIDATOR.test(v)) {
                    return false;
                }
            }
        } else if (cs.type == COL_TYPE.DATETIME) {
            for (String v : rawData.get(colIndex)) {
                Predicate<String> dateTimeValidator = buildDateTimeValidator(cs.dateTimeFormat);
                if (!dateTimeValidator.test(v)) {
                    return false;
                }
            }
        }
        return true;
    }

    public double[] getDoubleCol(int colIndex) {
        Function<String, Double> converter = (String v) -> {
            if (v.equals(missingValueCode)) {
                return Double.NaN;
            } else {
                return TO_DOUBLE.apply(v);
            }
        };
        String[] rawCol = rawData.get(colIndex);
        int n = rawCol.length;
        double[] col = new double[n];
        for (int k = 0; k < n; k++) {
            col[k] = converter.apply(rawCol[k]);
        }
        return col;
    }

    public LocalDateTime[] getDateTimeCol(int colIndex, String format) {
        Function<String, LocalDateTime> dateTimeConverter = buildDateTimeConverter(format);
        Function<String, LocalDateTime> converter = (String v) -> {
            if (v.equals(missingValueCode)) {
                return null;
            } else {
                return dateTimeConverter.apply(v);
            }
        };
        String[] rawCol = rawData.get(colIndex);
        int n = rawCol.length;
        LocalDateTime[] col = new LocalDateTime[n];
        for (int k = 0; k < n; k++) {
            col[k] = converter.apply(rawCol[k]);
        }
        return col;
    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {

        private boolean ignored;
        private Predicate<String> validator;
        private String missingValueCode;

        public CustomCellRenderer() {
            ignored = true;
        }

        public CustomCellRenderer(String missingValueCode, Predicate<String> validator) {
            this.validator = validator;
            this.missingValueCode = missingValueCode;
            ignored = false;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String valStr = value.toString();

            if (ignored) {
                setForeground(Color.LIGHT_GRAY);
                // setFont(getFont().deriveFont(Font.ITALIC));
                return this;
            }

            if (valStr.equals(missingValueCode)) {
                setForeground(AppConfig.AC.INVALID_COLOR);
                setFont(getFont().deriveFont(Font.PLAIN).deriveFont(Font.ITALIC));
            } else {
                setFont(getFont().deriveFont(Font.BOLD));
                if (!validator.test(valStr)) {
                    setForeground(AppConfig.AC.INVALID_COLOR);
                    setFont(getFont().deriveFont(Font.ITALIC).deriveFont(Font.BOLD));
                } else {
                    setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    // setFont(getFont().deriveFont(Font.PLAIN).deriveFont(Font.BOLD));
                }
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
                    System.err.println("DataParser Error: Inconsistent number of rows... Aborting.");
                    return false;
                }
            }
            this.rawData = rawData;
            fireTableStructureChanged();
            fireTableDataChanged();
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
