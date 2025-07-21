package org.baratinage.ui.baratin.limnigraph;

import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.TabContainer;

public class LimnigraphErrors extends SimpleFlowPanel {

    private LimnigraphDataset dataset;

    private final DataTable errConfigTable;
    private final DataTable errMatrixTable;
    private final ColumnHeaderDescription columnsDescription;

    public LimnigraphErrors() {
        super(true);

        errConfigTable = new DataTable();
        errMatrixTable = new DataTable();

        TabContainer tableTabs = new TabContainer();
        tableTabs.addTab("Error configuration", errConfigTable);
        tableTabs.addTab("Error matrix", errMatrixTable);

        T.updateHierarchy(this, errConfigTable);
        T.updateHierarchy(this, errMatrixTable);

        T.t(this, () -> {
            tableTabs.setTitleAt(0, T.text("stage_and_uncertainty"));
            tableTabs.setTitleAt(1, T.text("stage_samples_width_errors"));
        });

        addChild(tableTabs, true);

        columnsDescription = new ColumnHeaderDescription();

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            columnsDescription.openDialog(T.text("stage_and_uncertainty"));
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");

        errConfigTable.toolsPanel.addChild(showHeaderDescription, false);

    }

    public void updateDataset(LimnigraphDataset dataset) {

        this.dataset = dataset;

        T.clear(errConfigTable);
        errConfigTable.clearColumns();
        columnsDescription.clearAllColumnDesc();

        updateErrMatrixTable();

        if (dataset == null) {
            return;
        }

        columnsDescription.addColumnDesc("Time [yyyy-MM-dd hh:mm:ss]", () -> {
            return T.text("time_and_date");
        });
        columnsDescription.addColumnDesc("Stage [m]", () -> {
            return T.text("stage");
        });

        if (dataset.hasNonSysErr()) {
            columnsDescription.addColumnDesc("Stage_non_sys_err_std [m]", () -> {
                return T.text("stage_non_sys_error_uncertainty");
            });
        }
        if (dataset.hasSysErr()) {
            columnsDescription.addColumnDesc("Stage_sys_err_std [m]", () -> {
                return T.text("stage_sys_error_uncertainty");
            });
            columnsDescription.addColumnDesc("Stage_sys_err_indices", () -> {
                return T.text("stage_sys_error_ind");
            });
        }

        errConfigTable.addColumn(dataset.getDateTime());
        errConfigTable.addColumn(dataset.getStage());

        if (dataset.hasNonSysErr()) {
            errConfigTable.addColumn(Arrays.stream(dataset.getNonSysErrStd()).map(u -> u * 2.0).toArray());
        }

        if (dataset.hasSysErr()) {
            errConfigTable.addColumn(Arrays.stream(dataset.getSysErrStd()).map(u -> u * 2.0).toArray());
            errConfigTable.addColumn(dataset.getSysErrInd());
        }

        errConfigTable.updateData();

        errConfigTable.setHeader(0, "Time [yyyy-MM-dd hh:mm:ss]");
        errConfigTable.setHeader(1, "Stage [m]");
        int colIndex = 2;
        if (dataset.hasNonSysErr()) {
            errConfigTable.setHeader(colIndex, "Stage_non_sys_err_std [m]");

            colIndex++;
        }
        if (dataset.hasSysErr()) {
            errConfigTable.setHeader(colIndex, "Stage_sys_err_std [m]");
            colIndex++;
            errConfigTable.setHeader(colIndex, "Stage_sys_err_indices");
            colIndex++;
        }

        errConfigTable.setHeaderWidth(100);
        errConfigTable.setHeaderWidth(0, 150);

        errConfigTable.updateHeader();

    }

    private void updateErrMatrixTable() {
        errMatrixTable.clearColumns();
        if (dataset.hasStageErrMatrix()) {
            List<double[]> matrix = dataset.getStageErrMatrix();
            for (int k = 0; k < matrix.size() - 2; k++) {
                errMatrixTable.addColumn(matrix.get(k));
            }
            errMatrixTable.updateData();
            for (int k = 0; k < matrix.size() - 2; k++) {
                errMatrixTable.setHeader(k, "#" + k);
            }
            errMatrixTable.updateHeader();
        }
    }

}
