package org.baratinage.ui.baratin.rating_curve;

import java.util.List;

public class RatingCurvePlotData {

    public final double[] stage;
    public final double[] discharge;
    public final List<double[]> parametricUncertainty; // min, max
    public final List<double[]> totalUncertainty; // min, max
    public final List<double[]> stageTransitions; // central, min, max
    public final List<double[]> gaugings; // h, Q, Qmin, Qmax

    public RatingCurvePlotData(
            double[] stage, double[] discharge,
            List<double[]> parametricUncertainty,
            List<double[]> totalUncertainty,
            List<double[]> stageTransitions,
            List<double[]> gaugings) {
        this.stage = stage;
        this.discharge = discharge;
        this.parametricUncertainty = parametricUncertainty;
        this.totalUncertainty = totalUncertainty;
        this.stageTransitions = stageTransitions;
        this.gaugings = gaugings;
    }

    public boolean isPriorRatingCurve() {
        return this.totalUncertainty == null || this.gaugings == null;
    }

}
