package org.baratinage.ui.baratin.limnigraph;

import java.util.List;
import org.baratinage.translation.T;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;

public class LimnigraphErrors extends RowColPanel {

    private LimnigraphDataset dataset;

    private final DataTable errConfigTable;
    private final DataTable errMatrixTable;

    public LimnigraphErrors() {
        super(AXIS.COL);

        errConfigTable = new DataTable();
        errMatrixTable = new DataTable();

        TabContainer tableTabs = new TabContainer();
        tableTabs.addTab("Error configuration", errConfigTable);
        tableTabs.addTab("Error matrix", errMatrixTable);

        T.updateHierarchy(this, errConfigTable);
        T.updateHierarchy(this, errMatrixTable);

        T.t(this, () -> {
            tableTabs.setTitleAt(0, T.text("stage_error_config"));
            tableTabs.setTitleAt(1, T.text("stage_matrix_with_errors"));
            errConfigTable.updateCellRenderer();
            errMatrixTable.updateCellRenderer();
        });

        appendChild(tableTabs, 1);
    }

    public void updateDataset(LimnigraphDataset dataset) {

        this.dataset = dataset;

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

                errConfigTable.setHeaderWidth(100);
                errConfigTable.setHeaderWidth(0, 150);

                errConfigTable.updateHeader();
            });

            updateErrMatrixTable();

        }

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
