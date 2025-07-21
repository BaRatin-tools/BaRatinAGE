package org.baratinage.ui.baratin.rating_curve;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.component.DataTable;

public class RatingCurveTable extends DataTable {

    public final JCheckBox cropTotalEnvelopCheckbox;

    public RatingCurveTable() {

        cropTotalEnvelopCheckbox = new JCheckBox();
        cropTotalEnvelopCheckbox.setSelected(false);
        cropTotalEnvelopCheckbox.setText("crop_total_envelop_zero");

        ColumnHeaderDescription colHeaderDescRCGridTable = new ColumnHeaderDescription();
        colHeaderDescRCGridTable.addColumnDesc("h [m]", () -> {
            return T.text("stage");
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_maxpost [m3/s]", () -> {
            return String.format("%s (%s)", T.text("discharge"), T.text("maxpost_desc"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_param_low [m3/s]", () -> {
            return T.html("low_bound_uncertainty", T.text("parametric_uncertainty"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_param_high [m3/s]", () -> {
            return T.html("high_bound_uncertainty", T.text("parametric_uncertainty"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_total_low [m3/s]", () -> {
            return T.html("low_bound_uncertainty", T.text("parametric_structural_uncertainty"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Q_total_high [m3/s]", () -> {
            return T.html("high_bound_uncertainty", T.text("parametric_structural_uncertainty"));
        });

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            colHeaderDescRCGridTable.openDialog(T.text("rating_table"));
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");

        T.t(this, cropTotalEnvelopCheckbox, false, "crop_total_envelop_zero");

        toolsPanel.addChild(showHeaderDescription, false);
        toolsPanel.addChild(cropTotalEnvelopCheckbox, false);
    }

    public void updateTable(RatingCurvePlotData ratingCurveData) {

        clearColumns();
        addColumn(ratingCurveData.stage);
        addColumn(ratingCurveData.discharge);
        addColumn(ratingCurveData.parametricUncertainty.get(0));
        addColumn(ratingCurveData.parametricUncertainty.get(1));
        if (!ratingCurveData.isPriorRatingCurve()) {
            addColumn(ratingCurveData.totalUncertainty.get(0));
            addColumn(ratingCurveData.totalUncertainty.get(1));
        }
        updateData();

        setHeaderWidth(200);
        setHeader(0, "h [m]");
        setHeader(1, "Q_maxpost [m3/s]");
        setHeader(2, "Q_param_low [m3/s]");
        setHeader(3, "Q_param_high [m3/s]");
        if (!ratingCurveData.isPriorRatingCurve()) {
            setHeader(4, "Q_total_low [m3/s]");
            setHeader(5, "Q_total_high [m3/s]");
        }
        updateHeader();

    }
}
