package org.baratinage.ui.baratin.rating_curve;

import org.baratinage.ui.component.DataTable;

public class RatingCurveTable extends DataTable {
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
        setHeader(4, "Q_total_low [m3/s]");
        setHeader(5, "Q_total_high [m3/s]");
        updateHeader();

    }
}
