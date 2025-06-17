package org.baratinage.ui.baratin.rating_curve;

import java.awt.Color;
import java.util.List;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.Calc;
import org.baratinage.utils.ConsoleLogger;

public class RatingCurvePlot extends SimpleFlowPanel {

    private final PlotContainer plotContainer;

    private Plot plot;

    private boolean isPrior;

    private double[] stage;
    private double[] dischargeMaxpost;
    private List<double[]> dischargeParamUncertainty;
    private List<double[]> dischargeTotalUncertainty;
    private List<double[]> transitionStages;
    private List<double[]> gaugings;

    private final RatingCurvePlotToolsPanel toolsPanel;

    public RatingCurvePlot() {
        super(true);

        plotContainer = new PlotContainer(true);
        toolsPanel = new RatingCurvePlotToolsPanel();
        toolsPanel.addChangeListener(l -> {
            if (plot != null) {
                updatePlot();
            }
        });

        addChild(plotContainer, true);
        addChild(toolsPanel, 0, 5);

        T.updateHierarchy(this, plotContainer);
        T.updateHierarchy(this, toolsPanel);

    }

    public void setPriorPlot(RatingCurvePlotData ratingCurveData) {

        this.isPrior = true;
        this.stage = ratingCurveData.stage;
        this.dischargeMaxpost = ratingCurveData.discharge;
        this.dischargeParamUncertainty = ratingCurveData.parametricUncertainty;
        this.dischargeTotalUncertainty = null;
        this.transitionStages = ratingCurveData.stageTransitions;
        this.gaugings = null;

        toolsPanel.configure(true, true, false);
        updatePlot();
    }

    public void setPosteriorPlot(
            RatingCurvePlotData ratingCurveData) {
        this.isPrior = false;
        this.stage = ratingCurveData.stage;
        this.dischargeMaxpost = ratingCurveData.discharge;
        this.dischargeParamUncertainty = ratingCurveData.parametricUncertainty;
        this.dischargeTotalUncertainty = ratingCurveData.totalUncertainty;
        this.transitionStages = ratingCurveData.stageTransitions;
        this.gaugings = ratingCurveData.gaugings;

        toolsPanel.configure(true, true, true);
        updatePlot();
    }

    private void updatePlot() {

        // remove all translators related to old plot
        T.clear(plot);

        // create new plot
        plot = new Plot(true);
        T.updateHierarchy(this, plot);

        // stage transitions
        int n = transitionStages.size();
        String stageLegendText = isPrior ? "lgd_prior_activation_stage"
                : "lgd_posterior_activation_stage";
        Color stageActivationValueColor = isPrior ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_VALUE
                : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_VALUE;
        Color stageActivationUncertaintyColor = isPrior ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_UNCERTAINTY
                : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_UNCERTAINTY;
        for (int k = 0; k < n; k++) {
            double[] transitionStage = transitionStages.get(k);
            double coeffDir = toolsPanel.axisFlipped() ? 0 : Double.POSITIVE_INFINITY;
            addPlotItemToPlot(plot,
                    new PlotInfiniteBand("k", coeffDir,
                            transitionStage[1], transitionStage[2],
                            stageActivationUncertaintyColor, 0.9f),
                    k == 0 ? stageLegendText : null);
            addPlotItemToPlot(plot,
                    new PlotInfiniteLine("transition_line", coeffDir, transitionStage[0],
                            stageActivationValueColor, 2),
                    null);
        }

        // total uncertainty (only posterior rc)
        if (!isPrior && dischargeTotalUncertainty != null) {
            double[] smoothedTotalQUlow = dischargeTotalUncertainty.get(0);
            double[] smoothedTotalQUhigh = dischargeTotalUncertainty.get(1);
            if (toolsPanel.totalEnvSmoothed()) {
                int nSmooth = Double.valueOf((double) smoothedTotalQUlow.length * 0.01).intValue();
                nSmooth = Math.max(nSmooth, 1);
                ConsoleLogger.log("smoothing total envelop using a half window size of " + nSmooth
                        + "...");
                smoothedTotalQUlow = Calc.smoothArray(smoothedTotalQUlow, nSmooth);
                smoothedTotalQUhigh = Calc.smoothArray(smoothedTotalQUhigh, nSmooth);
            }
            Color totalColor = isPrior ? null : AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY;
            addPlotItemToPlot(
                    plot,
                    new PlotBand("post_total_uncertainty",
                            stage,
                            smoothedTotalQUlow,
                            smoothedTotalQUhigh,
                            toolsPanel.axisFlipped(),
                            totalColor),
                    "lgd_posterior_parametric_structural_uncertainty");
        }

        // parametric uncertainty
        Color paramColor = isPrior ? AppSetup.COLORS.PRIOR_ENVELOP
                : AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY;
        String paramLegendKey = isPrior ? "lgd_prior_parametric_uncertainty"
                : "lgd_posterior_parametric_uncertainty";
        addPlotItemToPlot(
                plot,
                new PlotBand("param_uncertainty",
                        stage,
                        dischargeParamUncertainty.get(0),
                        dischargeParamUncertainty.get(1),
                        toolsPanel.axisFlipped(),
                        paramColor),
                paramLegendKey);

        // maxpost
        String mpLegendKey = isPrior ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
        Color mpColor = isPrior ? AppSetup.COLORS.PRIOR_LINE : AppSetup.COLORS.RATING_CURVE;
        double[] mpX = toolsPanel.axisFlipped() ? dischargeMaxpost : stage;
        double[] mpY = toolsPanel.axisFlipped() ? stage : dischargeMaxpost;

        addPlotItemToPlot(
                plot,
                new PlotLine("mp", mpX, mpY, mpColor, 5),
                mpLegendKey);
        // gaugings (only if posterior rc)

        if (!isPrior && gaugings != null) {
            if (toolsPanel.axisFlipped()) {
                addPlotItemToPlot(
                        plot, new PlotPoints(
                                "gaugings",
                                gaugings.get(1),
                                gaugings.get(2),
                                gaugings.get(3),
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(0),
                                AppSetup.COLORS.GAUGING),
                        "lgd_active_gaugings");
            } else {
                addPlotItemToPlot(
                        plot, new PlotPoints(
                                "gaugings",
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(1),
                                gaugings.get(2),
                                gaugings.get(3),
                                AppSetup.COLORS.GAUGING),
                        "lgd_active_gaugings");
            }
        }

        // dealing with axis translations
        T.t(plot, () -> {
            toolsPanel.updatePlotAxis(plot);
        });

        plotContainer.setPlot(plot);
    }

    private static void addPlotItemToPlot(Plot plot, PlotItem item, String legendKey) {
        plot.addXYItem(item, legendKey != null);
        if (legendKey != null) {
            T.t(plot, () -> {
                item.setLabel(T.text(legendKey));
            });
        }
    }

}
