package org.baratinage.ui.baratin.hydrograph;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;

public class HydrographTable extends DataTable {

    private boolean hasStageUncertainty = false;

    public HydrographTable() {

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            showColumnDescriptionDialog();
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");
        toolsPanel.addChild(showHeaderDescription, false);

    }

    private void showColumnDescriptionDialog() {

        ColumnHeaderDescription colHeaderDescRCGridTable = new ColumnHeaderDescription();
        colHeaderDescRCGridTable.addColumnDesc("Time [yyyy-MM-dd hh:mm:ss]", () -> {
            return T.text("time_and_date");
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_maxpost [m3/s]", () -> {
            return String.format("%s (%s)", T.text("discharge"), T.text("maxpost_desc"));
        });
        if (hasStageUncertainty) {
            colHeaderDescRCGridTable.addColumnDesc("Q_limni_low [m3/s]", () -> {
                return T.html("low_bound_uncertainty", T.text("stage_uncertainty"));
            });
            colHeaderDescRCGridTable.addColumnDesc("Q_limni_high [m3/s]", () -> {
                return T.html("high_bound_uncertainty", T.text("stage_uncertainty"));
            });
        }
        String paramUdesc = hasStageUncertainty
                ? String.format("%s + %s", T.text("stage_uncertainty"), T.text("parametric_uncertainty"))
                : T.text("parametric_uncertainty");
        colHeaderDescRCGridTable.addColumnDesc("Q_param_low [m3/s]", () -> {
            return T.html("low_bound_uncertainty", paramUdesc);
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_param_high [m3/s]", () -> {
            return T.html("high_bound_uncertainty", paramUdesc);
        });
        String totalUdesc = hasStageUncertainty
                ? String.format("%s + %s + %s", T.text("stage_uncertainty"), T.text("parametric_uncertainty"),
                        T.text("structural_uncertainty"))
                : String.format("%s + %s", T.text("parametric_uncertainty"), T.text("structural_uncertainty"));
        colHeaderDescRCGridTable.addColumnDesc("Q_total_low [m3/s]", () -> {
            return T.html("low_bound_uncertainty", totalUdesc);
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_total_high [m3/s]", () -> {
            return T.html("high_bound_uncertainty", totalUdesc);
        });

        colHeaderDescRCGridTable.openDialog(T.text("hydrograph"));

    }

    public void updateTable(LocalDateTime[] dateTimeVector,
            double[] maxpost,
            List<double[]> stageU,
            List<double[]> paramU,
            List<double[]> totalU) {

        hasStageUncertainty = stageU != null;

        clearColumns();
        addColumn(dateTimeVector);
        addColumn(maxpost);
        if (hasStageUncertainty) {
            addColumn(stageU.get(0));
            addColumn(stageU.get(1));
        }
        addColumn(paramU.get(0));
        addColumn(paramU.get(1));
        addColumn(totalU.get(0));
        addColumn(totalU.get(1));
        updateData();

        setHeaderWidth(200);
        int k = 0;
        setHeader(k, "Time [yyyy-MM-dd hh:mm:ss]");
        k++;
        setHeader(k, "Q_maxpost [m3/s]");
        k++;
        if (hasStageUncertainty) {
            setHeader(k, "Q_limni_low [m3/s]");
            k++;
            setHeader(k, "Q_limni_high [m3/s]");
            k++;
        }
        setHeader(k, "Q_param_low [m3/s]");
        k++;
        setHeader(k, "Q_param_high [m3/s]");
        k++;
        setHeader(k, "Q_total_low [m3/s]");
        k++;
        setHeader(k, "Q_total_high [m3/s]");
        k++;
        updateHeader();
    }
}
