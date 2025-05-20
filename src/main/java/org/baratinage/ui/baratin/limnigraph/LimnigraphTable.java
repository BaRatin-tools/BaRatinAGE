package org.baratinage.ui.baratin.limnigraph;

import java.time.LocalDateTime;

import javax.swing.JButton;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;

public class LimnigraphTable extends DataTable {

    public LimnigraphTable() {

        ColumnHeaderDescription columnsDescription = new ColumnHeaderDescription();

        columnsDescription.addColumnDesc("Time [yyyy-MM-dd hh:mm:ss]", () -> {
            return T.text("time_and_date");
        });
        columnsDescription.addColumnDesc("Stage [m]", () -> {
            return T.text("stage");
        });
        columnsDescription.addColumnDesc("Stage_low_0.025 [m]", () -> {
            return T.text("percentile_0025");
        });
        columnsDescription.addColumnDesc("Stage_low_0.975 [m]", () -> {
            return T.text("percentile_0975");
        });

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            columnsDescription.openDialog(T.text("limnigraph"));
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");

        toolsPanel.addChild(showHeaderDescription, false);

    }

    public void updateTable(LocalDateTime[] dateTimeVector, double[] stage, double[] stage_low, double[] stage_high) {
        clearColumns();
        addColumn(dateTimeVector);
        addColumn(stage);
        if (stage_low != null && stage_high != null) {
            addColumn(stage_low);
            addColumn(stage_high);
        }
        updateData();

        setHeader(0, "Time [yyyy-MM-dd hh:mm:ss]");
        setHeader(1, "Stage [m]");
        if (stage_low != null && stage_high != null) {
            setHeader(2, "Stage_low_0.025 [m]");
            setHeader(3, "Stage_high_0.975 [m]");
        }
        // setHeaderWidth(100);
        // setHeaderWidth(0, 150);
        updateHeader();

    }
}
