package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultShift;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.DateTime;

public class ShiftDetectionTable extends SimpleFlowPanel {

  public final DataTable dataTable;

  private static ColumnHeaderDescription buildColumnDescs() {

    ColumnHeaderDescription headerDesc = new ColumnHeaderDescription();
    headerDesc.addColumnDesc("Name", () -> {
      return T.text("name");
    });

    headerDesc.addColumnDesc("Posterior_maxpost", () -> {
      return T.text("high_bound_parameter", T.text("maxpost_desc"));
    });
    headerDesc.addColumnDesc("Posterior_low", () -> {
      return T.text("low_bound_parameter", T.text("posterior_density"));
    });
    headerDesc.addColumnDesc("Posterior_high", () -> {
      return T.text("high_bound_parameter", T.text("posterior_density"));
    });

    return headerDesc;
  }

  private static final ColumnHeaderDescription resultsColumnsDescritption = buildColumnDescs();

  public ShiftDetectionTable(List<ResultShift> shifts) {
    // int n = shiftsOld.size();
    int n = shifts.size();
    String[] names = new String[n];
    LocalDateTime[] postMaxpost = new LocalDateTime[n];
    LocalDateTime[] postLow = new LocalDateTime[n];
    LocalDateTime[] postHight = new LocalDateTime[n];
    for (int k = 0; k < n; k++) {
      ResultShift shift = shifts.get(k);
      EstimatedParameter p = shift.parameter();
      names[k] = String.format("tau_%d", k);
      postMaxpost[k] = DateTime.doubleToDateTime(p.getMaxpost());
      double[] interval = p.get95interval();
      postLow[k] = DateTime.doubleToDateTime(interval[0]);
      postHight[k] = DateTime.doubleToDateTime(interval[1]);
    }

    dataTable = new DataTable();
    dataTable.addColumn(names);
    dataTable.addColumn(postMaxpost);
    dataTable.addColumn(postLow);
    dataTable.addColumn(postHight);
    dataTable.updateData();
    dataTable.setHeader(0, "Name");
    dataTable.setHeader(1, "Posterior_maxpost");
    dataTable.setHeader(2, "Posterior_low");
    dataTable.setHeader(3, "Posterior_high");
    dataTable.updateHeader();
    dataTable.autoResizeColumns();

    JButton showHeaderDescription = new JButton();
    showHeaderDescription.addActionListener(l -> {
      resultsColumnsDescritption.openDialog(T.text("shifts"));
    });
    T.t(this, showHeaderDescription, false, "table_headers_desc");
    dataTable.toolsPanel.addChild(showHeaderDescription, false);

    addChild(dataTable, true);
  }
}
