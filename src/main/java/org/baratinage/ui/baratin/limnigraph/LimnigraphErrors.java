package org.baratinage.ui.baratin.limnigraph;

import javax.swing.JButton;

import org.baratinage.translation.T;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;

public class LimnigraphErrors extends RowColPanel {

    private final DataTable errConfigTable;
    private final DataTable errMatrixTable;
    private final JButton computeErrMatrixBtn;

    public LimnigraphErrors() {
        super(AXIS.COL);

        errConfigTable = new DataTable();
        errMatrixTable = new DataTable();

        computeErrMatrixBtn = new JButton();
        T.t(this, computeErrMatrixBtn, false, "compute_stage_err_matrix");

        RowColPanel configPanel = new RowColPanel();
        configPanel.appendChild(computeErrMatrixBtn);

        TabContainer tableTabs = new TabContainer(TabContainer.SIDE.LEFT);
        tableTabs.addTab("Error configuration", errConfigTable);
        tableTabs.addTab("Error matrix", errMatrixTable);

        T.updateHierarchy(this, errConfigTable);
        T.updateHierarchy(this, errMatrixTable);

        appendChild(configPanel, 0);
        appendChild(tableTabs, 1);
    }

    public void updateDataset(LimnigraphDataset dataset) {

        T.clear(errConfigTable);
        errConfigTable.clearColumns();

        if (dataset.hasNonSysErr() || dataset.hasSysErr()) {
            errConfigTable.addColumn(dataset.getDateTime());
            errConfigTable.addColumn(dataset.getStage());

            if (dataset.hasNonSysErr()) {
                errConfigTable.addColumn(dataset.getNonSysErrStd());
            }

            if (dataset.hasSysErr()) {
                errConfigTable.addColumn(dataset.getSysErrStd());
                errConfigTable.addColumn(dataset.getSysErrInd());
            }

            errConfigTable.updateData();

            T.t(errConfigTable, () -> {
                errConfigTable.setHeaderWidth(0, 150);
                errConfigTable.setHeader(0, T.text("date_time"));
                errConfigTable.setHeader(1, T.text("stage_level"));
                int colIndex = 2;
                if (dataset.hasNonSysErr()) {
                    errConfigTable.setHeader(colIndex, T.text("stage_non_sys_error_std"));
                    colIndex++;
                }
                if (dataset.hasSysErr()) {
                    errConfigTable.setHeader(colIndex, T.text("stage_sys_error_std"));
                    colIndex++;
                    errConfigTable.setHeader(colIndex, T.text("stage_sys_error_ind"));
                    colIndex++;
                }
                errConfigTable.updateHeader();
            });

        }
    }
}
