package org.baratinage.ui.component.data_import;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleIntegerField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;

public class DataPreview extends SimpleFlowPanel {

  private final DataTableCellRenderer dataTableRenderer;
  private final DataTableModel dataTableModel;
  private final JTable dataTable;

  public DataPreview() {
    super(true);
    setPadding(5);
    setGap(5);

    dataTableModel = new DataTableModel();
    dataTableRenderer = new DataTableCellRenderer();
    dataTable = new JTable(dataTableModel);

    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    dataTable.getTableHeader().setReorderingAllowed(false);
    // dataTable.setCellSelectionEnabled(true);
    dataTable.setDefaultRenderer(Object.class, dataTableRenderer);

    JScrollPane scrollpane = new JScrollPane(dataTable);
    scrollpane.setBorder(BorderFactory.createEmptyBorder());

    /** having row numbers */
    JTable rowTable = new JTable(new AbstractTableModel() {
      public int getRowCount() {
        return dataTable.getRowCount();
      }

      public int getColumnCount() {
        return 1;
      }

      public Object getValueAt(int row, int col) {
        return row + 1;
      }
    });
    rowTable.setPreferredScrollableViewportSize(new Dimension(80, 0));
    rowTable.setRowHeight(dataTable.getRowHeight());
    rowTable.setEnabled(false);
    scrollpane.setRowHeaderView(rowTable);

    Dimension defaultPrefDim = scrollpane.getPreferredSize();
    defaultPrefDim.height = 250;
    scrollpane.setPreferredSize(defaultPrefDim);

    JLabel dataPreviewTitleLabel = new JLabel();
    T.t(this, dataPreviewTitleLabel, false, "data_preview_title");
    JLabel nRowPreloadLabel = new JLabel();
    T.t(this, nRowPreloadLabel, false, "n_rows_to_preload");
    SimpleIntegerField nRowPreloadField = new SimpleIntegerField(5, Integer.MAX_VALUE, 1);
    nRowPreloadField.setValue(dataTableModel.nDisplay);
    nRowPreloadField.addChangeListener((e) -> {
      dataTableModel.nDisplay = nRowPreloadField.getIntValue();
      updatePreviewTable();
    });
    SimpleFlowPanel nRowPreloadPanel = new SimpleFlowPanel();
    nRowPreloadPanel.setGap(5);
    nRowPreloadPanel.addChild(nRowPreloadLabel, false);
    nRowPreloadPanel.addChild(nRowPreloadField, true);

    SimpleFlowPanel headerPanel = new SimpleFlowPanel();

    headerPanel.addChild(dataPreviewTitleLabel, false);
    headerPanel.addExtensor();
    headerPanel.addChild(nRowPreloadPanel, false);
    addChild(headerPanel, false);
    addChild(scrollpane, true);
  }

  public void updatePreviewTable() {
    dataTableModel.fireTableStructureChanged();
    dataTableModel.fireTableDataChanged();

    for (int col = 0; col < dataTable.getColumnCount(); col++) {
      TableColumn column = dataTable.getColumnModel().getColumn(col);
      int minWidth = 50;
      int maxWidth = 200;
      int width = minWidth;

      for (int row = 0; row < dataTable.getRowCount(); row++) {
        TableCellRenderer renderer = dataTable.getCellRenderer(row, col);
        Component comp = dataTable.prepareRenderer(renderer, row, col);
        width = Math.min(Math.max(comp.getPreferredSize().width, width), maxWidth);
      }
      column.setPreferredWidth(width);
    }
  }

  public void setData(List<String[]> data, String[] headers) {
    if (data.size() != headers.length) {
      ConsoleLogger.error("Mismatch between the number of columns and the number of headers");
      return;
    }
    dataTableModel.data.clear();
    for (int index = 0; index < data.size(); index++) {
      dataTableModel.addColumn(headers[index], data.get(index));
    }
  }

  public void setColumnMapper(int colIndex, IDataTableColumn columnConfig) {
    dataTableRenderer.columnsConfig.put(colIndex, columnConfig);
  }

  public void resetColumnMappers() {
    dataTableRenderer.columnsConfig.clear();
  }
}
