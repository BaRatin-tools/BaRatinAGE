package org.baratinage.ui.component.data_import;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class DataTableModel extends AbstractTableModel {

  public record NamedColumn(String name, String[] values) {
  }

  public final List<NamedColumn> data = new ArrayList<>();

  public int nDisplay = 15;

  public int addColumn(String header, String[] values) {
    data.add(new NamedColumn(header, values));
    return data.size() - 1;
  }

  public String[] getRow(int rowIndex) {
    String[] row = new String[getColumnCount()];
    for (int k = 0; k < getColumnCount(); k++) {
      row[k] = (String) getValueAt(rowIndex, k);
    }
    return row;
  }

  @Override
  public int getRowCount() {
    if (getColumnCount() <= 0) {
      return 0;
    }
    return Math.min(nDisplay, data.get(0).values().length);
  }

  @Override
  public int getColumnCount() {
    return data.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data.get(columnIndex).values[rowIndex];
  }

  @Override
  public String getColumnName(int columnIndex) {
    return data.get(columnIndex).name();
  }

}
