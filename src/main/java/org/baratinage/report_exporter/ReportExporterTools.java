package org.baratinage.report_exporter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.baratinage.jbam.DistributionType;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.utils.Misc;

public class ReportExporterTools {

  public static List<String[]> getParPriorDistSimplifiedStringTable(List<ParameterPriorDistSimplified> parameters) {
    if (parameters != null) {
      List<String[]> parTable = new ArrayList<>();
      parTable.add(new String[] {
          "", T.text("mean_value"), T.text("uncertainty_value")
      });
      for (ParameterPriorDistSimplified par : parameters) {
        parTable.add(new String[] {
            par.nameLabel.getText()
                .replace("<html>", "")
                .replace("</html>", ""),
            par.meanValueField.getText(),
            par.uncertaintyValueField.getText()
        });
      }
      return parTable;
    }
    return null;
  }

  public static List<String[]> getParPriorDistStringTable(List<ParameterPriorDist> parameters) {
    List<String[]> parTable = new ArrayList<>();
    parTable.add(new String[] {
        "", T.text("distribution_parameters"), T.text("initial_guess")
    });
    for (ParameterPriorDist par : parameters) {
      DistributionType distType = par.getDistributionType();
      Double initialValue = par.getInitialGuess();
      Double[] distParameters = par.getDistributionParameters();

      List<String> distParametersStr = new ArrayList<>();
      for (Double d : distParameters) {
        distParametersStr.add(Misc.formatNumber(d, 4, 0.001, 10000));
      }
      String priorDistString = T.text("dist_" + distType.bamName) + "(" + String.join(", ", distParametersStr) + ")";
      parTable.add(new String[] {
          par.getParameter().name == "k" ? "&kappa;" : par.getParameter().name,
          priorDistString,
          Misc.formatNumber(initialValue, 4, 0.001, 10000)
      });
    }
    return parTable;
  }

  public static List<String[]> getDataTableRows(JTable table) {
    TableModel model = table.getModel();
    int nCol = model.getColumnCount();
    int nRow = model.getRowCount();
    List<String[]> rows = new ArrayList<>();
    for (int k = 0; k < nRow + 1; k++) {
      rows.add(new String[nCol]);
    }
    for (int k = 0; k < nCol; k++) {
      TableColumn tableColumn = table.getColumnModel().getColumn(k);
      Object headerValue = tableColumn.getHeaderValue();
      if (headerValue instanceof String) {
        rows.get(0)[k] = (String) headerValue;
      } else {
        rows.get(0)[k] = table.getColumnName(k);
      }
    }
    for (int i = 0; i < nRow; i++) {
      for (int j = 0; j < nCol; j++) {
        TableCellRenderer renderer = table.getCellRenderer(i, j);
        Object value = table.getValueAt(i, j);
        Component component = table.prepareRenderer(renderer, i, j);
        String str = "";
        if (component instanceof JLabel) {
          str = ((JLabel) component).getText();
        } else {
          str = value != null ? value.toString() : "";
        }
        rows.get(i + 1)[j] = str;
      }
    }
    return rows;
  }

}
