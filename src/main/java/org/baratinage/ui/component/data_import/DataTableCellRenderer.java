package org.baratinage.ui.component.data_import;

import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.baratinage.AppSetup;
import org.baratinage.ui.container.SimpleFlowPanel;

public class DataTableCellRenderer extends DefaultTableCellRenderer {

  public final HashMap<Integer, IDataTableColumn> columnsConfig = new HashMap<>();

  SimpleFlowPanel panel = new SimpleFlowPanel();
  JLabel mainLabel = new JLabel();
  JLabel secondaryLabel = new JLabel();

  public DataTableCellRenderer() {
    panel.setGap(5);
    panel.addChild(mainLabel, false);
    panel.addChild(secondaryLabel, false);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    IDataTableColumn cc = columnsConfig.get(column);

    if (cc == null) {
      setForeground(isSelected ? table.getSelectionForeground() : AppSetup.COLORS.DEFAULT_FG_LIGHT);
      setFont(getFont().deriveFont(Font.ITALIC));
      return this;
    }

    panel.setBackground(getBackground());
    mainLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
    secondaryLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

    mainLabel.setFont(getFont());
    secondaryLabel.setFont(getFont());

    mainLabel.setText(cc.getLabelText(column, row));
    secondaryLabel.setText(cc.getSecondaryLabelText(column, row));

    setText(cc.getLabelText(column, row));

    if (cc.isMissing(column, row)) {
      mainLabel.setForeground(isSelected ? table.getSelectionForeground() : AppSetup.COLORS.INVALID_FG);
      mainLabel.setFont(getFont().deriveFont(Font.PLAIN));
      secondaryLabel.setForeground(isSelected ? table.getSelectionForeground() : AppSetup.COLORS.INVALID_FG);
      secondaryLabel.setFont(getFont().deriveFont(Font.PLAIN));
    } else {
      if (cc.isInvalid(column, row)) {
        secondaryLabel.setForeground(isSelected ? table.getSelectionForeground() : AppSetup.COLORS.INVALID_FG);
        secondaryLabel.setFont(getFont().deriveFont(Font.ITALIC).deriveFont(Font.BOLD));
      } else {
        mainLabel.setFont(getFont().deriveFont(Font.BOLD));
      }
    }
    return panel;
  }
}
