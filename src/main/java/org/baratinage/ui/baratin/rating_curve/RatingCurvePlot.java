package org.baratinage.ui.baratin.rating_curve;

import java.awt.Color;
import java.util.List;

import javax.swing.JCheckBox;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand2;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteBand2;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotPoints;

public class RatingCurvePlot extends RowColPanel {

    private final PlotContainer plotContainer;

    private Plot plot;

    private boolean isPrior;
    private double[] stage;
    private double[] dischargeMaxpost;
    private List<double[]> dischargeParamUncertainty;
    private List<double[]> dischargeTotalUncertainty;
    private List<double[]> transitionStages;
    private List<double[]> gaugings;

    public RatingCurvePlot() {
        super(AXIS.COL);

        plotContainer = new PlotContainer(true);

        JCheckBox switchAxisCheckbox = new JCheckBox();
        switchAxisCheckbox.setSelected(false);
        switchAxisCheckbox.setText("Switch X Y axis");
        switchAxisCheckbox.addActionListener((e) -> {
            updatePlot(
                    stage,
                    dischargeMaxpost,
                    dischargeParamUncertainty,
                    dischargeTotalUncertainty,
                    transitionStages,
                    gaugings,
                    isPrior,
                    switchAxisCheckbox.isSelected());
        });

        setBackground(Color.WHITE);
        appendChild(plotContainer, 1);
        appendChild(switchAxisCheckbox, 0, 5);

        T.updateHierarchy(this, plotContainer);

        T.t(this, switchAxisCheckbox, false, "inverted_rc_plot_axis");
    }

    public void setPriorPlot(double[] stage,
            double[] dischargeMaxpost,
            List<double[]> dischargeParamUncertainty,
            List<double[]> transitionStages) {
        updatePlot(stage,
                dischargeMaxpost,
                dischargeParamUncertainty,
                null,
                transitionStages,
                null,
                true,
                false);
    }

    public void setPosteriorPlot(double[] stage,
            double[] dischargeMaxpost,
            List<double[]> dischargeParamUncertainty,
            List<double[]> dischargeTotalUncertainty,
            List<double[]> transitionStages,
            List<double[]> gaugings) {
        updatePlot(
                stage,
                dischargeMaxpost,
                dischargeParamUncertainty,
                dischargeTotalUncertainty,
                transitionStages,
                gaugings,
                false,
                false);
    }

    public void updatePlot(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> dischargeParamUncertainty,
            List<double[]> dischargeTotalUncertainty,
            List<double[]> transitionStages,
            List<double[]> gaugings,
            boolean isPriorPlot,
            boolean flipAxis) {

        // remove all translators related to old plot
        T.clear(plot);
        plot = new Plot(true);
        T.updateHierarchy(this, plot);

        // reset class variables
        this.isPrior = isPriorPlot;
        this.stage = stage;
        this.dischargeMaxpost = dischargeMaxpost;
        this.dischargeParamUncertainty = dischargeParamUncertainty;
        this.dischargeTotalUncertainty = dischargeTotalUncertainty;
        this.transitionStages = transitionStages;
        this.gaugings = gaugings;

        // stage transitions
        int n = transitionStages.size();
        for (int k = 0; k < n; k++) {
            double[] transitionStage = transitionStages.get(k);
            double coeffDir = flipAxis ? 0 : Double.POSITIVE_INFINITY;
            addPlotItemToPlot(plot,
                    new PlotInfiniteBand2("k", coeffDir,
                            transitionStage[1], transitionStage[2],
                            AppConfig.AC.STAGE_TRANSITION_UNCERTAINTY_COLOR, 0.9f),
                    k == 0 ? "lgd_prior_transition_stage" : null);
            addPlotItemToPlot(plot,
                    new PlotInfiniteLine("transition_line", coeffDir, transitionStage[0],
                            AppConfig.AC.STAGE_TRANSITION_VALUE_COLOR, 2),
                    null);
        }

        // total uncertainty (only posterior rc)
        if (!isPrior && dischargeTotalUncertainty != null) {
            Color totalColor = isPriorPlot ? null : AppConfig.AC.RATING_CURVE_TOTAL_UNCERTAINTY_COLOR;
            addPlotItemToPlot(
                    plot,
                    new PlotBand2("post_total_uncertainty",
                            stage,
                            dischargeTotalUncertainty.get(0),
                            dischargeTotalUncertainty.get(1),
                            flipAxis,
                            totalColor),
                    "lgd_posterior_parametric_structural_uncertainty");
        }

        // parametric uncertainty
        Color paramColor = isPriorPlot ? AppConfig.AC.PRIOR_ENVELOP_COLOR
                : AppConfig.AC.RATING_CURVE_PARAM_UNCERTAINTY_COLOR;
        String paramLegendKey = isPriorPlot ? "lgd_prior_parametric_uncertainty"
                : "lgd_posterior_parametric_uncertainty";
        addPlotItemToPlot(
                plot,
                new PlotBand2("param_uncertainty",
                        stage,
                        dischargeParamUncertainty.get(0),
                        dischargeParamUncertainty.get(1),
                        flipAxis,
                        paramColor),
                paramLegendKey);

        // maxpost
        String mpLegendKey = isPriorPlot ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
        Color mpColor = isPriorPlot ? AppConfig.AC.PRIOR_LINE_COLOR : AppConfig.AC.RATING_CURVE_COLOR;
        double[] mpX = flipAxis ? dischargeMaxpost : stage;
        double[] mpY = flipAxis ? stage : dischargeMaxpost;

        addPlotItemToPlot(
                plot,
                new PlotLine("mp", mpX, mpY, mpColor, 5),
                mpLegendKey);
        // gaugings (only if posterior rc)

        if (!isPrior && gaugings != null) {
            if (flipAxis) {
                addPlotItemToPlot(
                        plot, new PlotPoints(
                                "gaugings",
                                gaugings.get(1),
                                gaugings.get(2),
                                gaugings.get(3),
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(0),
                                AppConfig.AC.GAUGING_COLOR),
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
                                AppConfig.AC.GAUGING_COLOR),
                        "lgd_active_gaugings");
            }
        }

        // dealing with axis translations
        T.t(plot, () -> {
            String dischargeString = T.text("discharge");
            String stageString = T.text("stage_level");
            if (flipAxis) {
                plot.axisX.setLabel(dischargeString);
                plot.axisY.setLabel(stageString);
                plot.axisYlog.setLabel(stageString);
            } else {
                plot.axisX.setLabel(stageString);
                plot.axisY.setLabel(dischargeString);
                plot.axisYlog.setLabel(dischargeString);
            }
        });

        plotContainer.setPlot(plot);
    }

    private static void addPlotItemToPlot(Plot plot, PlotItem item, String legendKey) {
        // FIXME: when chart
        plot.addXYItem(item, legendKey != null);
        if (legendKey != null) {
            T.t(plot, () -> {
                item.setLabel(T.text(legendKey));
            });
        }
    }

}
