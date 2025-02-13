package org.baratinage.ui.baratin.hydrograph;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;

public class HydrographTable extends DataTable {

    public HydrographTable() {

        ColumnHeaderDescription colHeaderDescRCGridTable = new ColumnHeaderDescription();
        colHeaderDescRCGridTable.addColumnDesc("Time [yyyy-MM-dd hh:mm:ss]", () -> {
            return T.text("time_and_date");
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_maxpost [m3/s]", () -> {
            return String.format("%s (%s)", T.text("discharge"), T.text("maxpost_desc"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_param_low [m3/s]", () -> {
            return T.text("low_bound_uncertainty", T.text("parametric_uncertainty"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_param_high [m3/s]", () -> {
            return T.text("high_bound_uncertainty", T.text("parametric_uncertainty"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_total_low [m3/s]", () -> {
            return T.text("low_bound_uncertainty", T.text("total_uncertainty")) + " *";
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_total_high [m3/s]", () -> {
            return T.text("high_bound_uncertainty", T.text("total_uncertainty")) + " *";
        });

        colHeaderDescRCGridTable.addColumnDesc(() -> {
            return String.format(" * %s = %s + %s + %s",
                    T.text("total_uncertainty"),
                    T.text("parametric_uncertainty"),
                    T.text("structural_uncertainty"),
                    T.text("stage_uncertainty"));
        });

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            colHeaderDescRCGridTable.openDialog(T.text("hydrograph"));
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");

        toolsPanel.appendChild(showHeaderDescription);

    }

    public void updateTable(LocalDateTime[] dateTimeVector, double[] maxpost, List<double[]> paramU,
            List<double[]> totalU) {
        clearColumns();
        addColumn(dateTimeVector);
        addColumn(maxpost);
        addColumn(paramU.get(0));
        addColumn(paramU.get(1));
        addColumn(totalU.get(0));
        addColumn(totalU.get(1));
        updateData();

        setHeaderWidth(200);
        setHeader(0, "Time [yyyy-MM-dd hh:mm:ss]");
        setHeader(1, "Q_maxpost [m3/s]");
        setHeader(2, "Q_param_low [m3/s]");
        setHeader(3, "Q_param_high [m3/s]");
        setHeader(4, "Q_total_low [m3/s]");
        setHeader(5, "Q_total_high [m3/s]");
        updateHeader();
    }
}
