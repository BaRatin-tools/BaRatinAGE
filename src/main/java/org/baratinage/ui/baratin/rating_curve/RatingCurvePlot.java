package org.baratinage.ui.baratin.rating_curve;

import java.awt.Color;
import java.util.List;

import javax.swing.JCheckBox;

import org.baratinage.ui.container.RowColPanel;
import org.baratinage.AppSetup;
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

        private boolean axisFliped = false;
        private boolean dischargeAxisInLog = false;

        private final JCheckBox switchDischargeAxisScale;
        private final JCheckBox switchAxisCheckbox;

        public RatingCurvePlot() {
                super(AXIS.COL);

                plotContainer = new PlotContainer(true);

                switchDischargeAxisScale = new JCheckBox();
                switchDischargeAxisScale.setSelected(false);
                switchDischargeAxisScale.setText("log_scale_discharge_axis");

                switchAxisCheckbox = new JCheckBox();
                switchAxisCheckbox.setSelected(false);
                switchAxisCheckbox.setText("swap_xy_axis");

                switchDischargeAxisScale.addActionListener((e) -> {
                        dischargeAxisInLog = switchDischargeAxisScale.isSelected();
                        updatePlot();
                });

                switchAxisCheckbox.addActionListener((e) -> {
                        axisFliped = switchAxisCheckbox.isSelected();
                        updatePlot();
                });

                RowColPanel toolsPanel = new RowColPanel(AXIS.ROW, ALIGN.START);
                toolsPanel.setBackground(Color.WHITE);
                toolsPanel.setGap(5);
                // RowColPanel toolsPanel = plotContainer.toolsPanel;
                toolsPanel.appendChild(switchDischargeAxisScale, 0);
                toolsPanel.appendChild(switchAxisCheckbox, 0);

                setBackground(Color.WHITE);
                appendChild(plotContainer, 1);
                appendChild(toolsPanel, 0, 5);

                T.updateHierarchy(this, plotContainer);

                T.t(this, switchAxisCheckbox, false, "swap_xy_axis");
                T.t(this, switchDischargeAxisScale, false, "log_scale_discharge_axis");
        }

        public void setPriorPlot(double[] stage,
                        double[] dischargeMaxpost,
                        List<double[]> dischargeParamUncertainty,
                        List<double[]> transitionStages) {

                this.isPrior = true;
                this.stage = stage;
                this.dischargeMaxpost = dischargeMaxpost;
                this.dischargeParamUncertainty = dischargeParamUncertainty;
                this.dischargeTotalUncertainty = null;
                this.transitionStages = transitionStages;
                this.gaugings = null;

                updatePlot();
        }

        public void setPosteriorPlot(double[] stage,
                        double[] dischargeMaxpost,
                        List<double[]> dischargeParamUncertainty,
                        List<double[]> dischargeTotalUncertainty,
                        List<double[]> transitionStages,
                        List<double[]> gaugings) {

                this.isPrior = false;
                this.stage = stage;
                this.dischargeMaxpost = dischargeMaxpost;
                this.dischargeParamUncertainty = dischargeParamUncertainty;
                this.dischargeTotalUncertainty = dischargeTotalUncertainty;
                this.transitionStages = transitionStages;
                this.gaugings = gaugings;

                updatePlot();
        }

        private void updatePlot() {

                // remove all translators related to old plot
                T.clear(plot);

                // create new plot
                plot = new Plot(true);
                T.updateHierarchy(this, plot);

                // set proper axis scale
                if (dischargeAxisInLog) {
                        if (axisFliped) {
                                plot.plot.setDomainAxis(plot.axisXlog);
                        } else {
                                plot.plot.setRangeAxis(plot.axisYlog);
                        }
                }

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
                        double coeffDir = axisFliped ? 0 : Double.POSITIVE_INFINITY;
                        addPlotItemToPlot(plot,
                                        new PlotInfiniteBand2("k", coeffDir,
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
                        Color totalColor = isPrior ? null : AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY;
                        addPlotItemToPlot(
                                        plot,
                                        new PlotBand2("post_total_uncertainty",
                                                        stage,
                                                        dischargeTotalUncertainty.get(0),
                                                        dischargeTotalUncertainty.get(1),
                                                        axisFliped,
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
                                new PlotBand2("param_uncertainty",
                                                stage,
                                                dischargeParamUncertainty.get(0),
                                                dischargeParamUncertainty.get(1),
                                                axisFliped,
                                                paramColor),
                                paramLegendKey);

                // maxpost
                String mpLegendKey = isPrior ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
                Color mpColor = isPrior ? AppSetup.COLORS.PRIOR_LINE : AppSetup.COLORS.RATING_CURVE;
                double[] mpX = axisFliped ? dischargeMaxpost : stage;
                double[] mpY = axisFliped ? stage : dischargeMaxpost;

                addPlotItemToPlot(
                                plot,
                                new PlotLine("mp", mpX, mpY, mpColor, 5),
                                mpLegendKey);
                // gaugings (only if posterior rc)

                if (!isPrior && gaugings != null) {
                        if (axisFliped) {
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
                        String dischargeString = T.text("discharge") + " [m3/s]";
                        String stageString = T.text("stage_level") + " [m]";
                        if (axisFliped) {
                                plot.axisX.setLabel(dischargeString);
                                plot.axisXlog.setLabel(dischargeString);
                                plot.axisY.setLabel(stageString);
                                plot.axisYlog.setLabel(stageString);
                        } else {
                                plot.axisX.setLabel(stageString);
                                plot.axisXlog.setLabel(stageString);
                                plot.axisY.setLabel(dischargeString);
                                plot.axisYlog.setLabel(dischargeString);
                        }
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
