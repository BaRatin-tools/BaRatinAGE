package org.baratinage.ui.baratin.rating_curve;

import java.util.HashMap;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;
import org.baratinage.utils.ConsoleLogger;

public class RatingCurvePlotData implements IPlotDataProvider {

    public static final String MAXPOST = "maxpost";
    public static final String PARAM_U = "param_u";
    public static final String TOTAL_U = "total_u";
    public static final String STAGE_TRANSITION = "stage_transition_value";
    public static final String STAGE_TRANSITION_U = "stage_transition_u";

    public final double[] stage;
    public final double[] discharge;
    public final List<double[]> parametricUncertainty; // min, max
    public final List<double[]> totalUncertainty; // min, max
    public final List<double[]> stageTransitions; // central, min, max
    public final List<double[]> gaugings; // h, Q, Qmin, Qmax

    public boolean axisFliped = false;
    public boolean smoothed = false;

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

    public static double[] smoothValues(double[] x) {
        int nSmooth = Double.valueOf((double) x.length * 0.01).intValue();
        nSmooth = Math.max(nSmooth, 1);
        ConsoleLogger.log(String.format("Smoothing values using %d wide half window", nSmooth));
        x = Calc.smoothArray(x, nSmooth);
        return x;
    }

    @Override
    public HashMap<String, PlotItem> getPlotItems() {
        HashMap<String, PlotItem> plotItems = new HashMap<>();

        if (stageTransitions != null) {
            for (int k = 0; k < stageTransitions.size(); k++) {
                double[] transitionStage = stageTransitions.get(k);
                double coeffDir = axisFliped ? 0 : Double.POSITIVE_INFINITY;
                String defaultLegend = isPriorRatingCurve() ? T.text("lgd_prior_activation_stage")
                        : T.text("lgd_posterior_activation_stage");
                plotItems.put(STAGE_TRANSITION_U + k,
                        new PlotInfiniteBand(defaultLegend,
                                coeffDir,
                                transitionStage[1],
                                transitionStage[2],
                                isPriorRatingCurve()
                                        ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_UNCERTAINTY
                                        : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_UNCERTAINTY,
                                0.9f));

                plotItems.put(STAGE_TRANSITION + k,
                        new PlotInfiniteLine(defaultLegend,
                                coeffDir,
                                transitionStage[0],
                                isPriorRatingCurve()
                                        ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_VALUE
                                        : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_VALUE,
                                2));

            }
        }

        if (totalUncertainty != null) {

            double[] x = totalUncertainty.get(0);
            double[] y = totalUncertainty.get(1);
            if (smoothed) {
                x = smoothValues(x);
                y = smoothValues(y);
            }

            plotItems.put(TOTAL_U, new PlotBand(T.text("lgd_discharge_total_u"),
                    stage,
                    x,
                    y,
                    axisFliped,
                    AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY));
        }

        if (parametricUncertainty != null) {
            plotItems.put(PARAM_U,
                    new PlotBand(
                            isPriorRatingCurve()
                                    ? T.text("lgd_prior_parametric_uncertainty")
                                    : T.text("lgd_posterior_parametric_uncertainty"),
                            stage,
                            parametricUncertainty.get(0),
                            parametricUncertainty.get(1),
                            axisFliped,
                            isPriorRatingCurve() ? AppSetup.COLORS.PRIOR_ENVELOP
                                    : AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY));
        }

        if (stage != null & discharge != null) {
            double[] mpX = axisFliped ? discharge : stage;
            double[] mpY = axisFliped ? stage : discharge;
            plotItems.put(MAXPOST, new PlotLine(isPriorRatingCurve() ? T.text("lgd_prior_rating_curve")
                    : T.text("lgd_posterior_rating_curve"), mpX, mpY, AppSetup.COLORS.RATING_CURVE,
                    5));
        }

        return plotItems;
    }

}
