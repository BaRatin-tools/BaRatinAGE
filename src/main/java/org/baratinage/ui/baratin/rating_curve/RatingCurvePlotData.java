package org.baratinage.ui.baratin.rating_curve;

import java.util.HashMap;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;

public class RatingCurvePlotData implements IPlotDataProvider {

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

    @Override
    public HashMap<String, PlotItem> getPlotItems() {
        HashMap<String, PlotItem> plotItems = new HashMap<>();

        boolean axisFliped = false;

        if (stage != null & discharge != null) {
            double[] mpX = axisFliped ? discharge : stage;
            double[] mpY = axisFliped ? stage : discharge;
            plotItems.put("mp", new PlotLine("mp", mpX, mpY, AppSetup.COLORS.PLOT_LINE, 5));
        }

        if (parametricUncertainty != null) {
            plotItems.put("param_uncertainty", new PlotBand("param_uncertainty",
                    stage,
                    parametricUncertainty.get(0),
                    parametricUncertainty.get(1),
                    axisFliped,
                    AppSetup.COLORS.PLOT_ENVELOP));
        }

        return plotItems;
    }

}
