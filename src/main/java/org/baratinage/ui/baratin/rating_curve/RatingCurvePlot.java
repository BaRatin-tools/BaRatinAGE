package org.baratinage.ui.baratin.rating_curve;

import java.awt.Color;
import java.util.List;

import javax.swing.JToggleButton;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.EditablePlot;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
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

    public final RatingCurvePlotToolsPanel toolsPanel;

    public final PlotEditor plotEditor;
    private final JToggleButton plotEditorToggleBtn;
    private final SimpleFlowPanel plotArea;

    public RatingCurvePlot() {
        super();

        plotContainer = new PlotContainer(true);
        toolsPanel = new RatingCurvePlotToolsPanel();
        toolsPanel.addChangeListener(l -> {
            if (plot != null) {
                updatePlot();
            }
        });

        plotArea = new SimpleFlowPanel(true);

        plotEditor = new PlotEditor();
        plotEditorToggleBtn = new JToggleButton();
        plotEditorToggleBtn.setIcon(AppSetup.ICONS.EDIT);
        plotEditorToggleBtn.addActionListener(l -> {
            removeAll();
            if (plotEditorToggleBtn.isSelected()) {
                addChild(plotEditor, false);
            }
            addChild(plotArea, true);
        });
        plotContainer.toolsPanel.addChild(plotEditorToggleBtn, false);

        plotArea.addChild(plotContainer, true);
        plotArea.addChild(toolsPanel, 0, 5);

        addChild(plotArea);

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

        toolsPanel.configure(true, true, false, false);
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

        toolsPanel.configure(true, true, true, true);
        updatePlot();
    }

    private void updatePlot() {

        // create new plot
        plot = new Plot(true);

        if (toolsPanel.logDischargeAxis()) {
            if (toolsPanel.axisFlipped()) {
                plot.setXAxisLog(true);
            } else {
                plot.setYAxisLog(true);
            }
        }

        // stage transitions
        int n = transitionStages.size();

        EditablePlotItem stageTransitionBand = null;
        EditablePlotItem stageTransitionLine = null;

        if (n > 0) {
            double coeffDir = toolsPanel.axisFlipped() ? 0 : Double.POSITIVE_INFINITY;
            double[] transitionStage = transitionStages.get(0);
            PlotInfiniteBand band = new PlotInfiniteBand("k_band", coeffDir, transitionStage[1], transitionStage[2]);
            PlotInfiniteLine line = new PlotInfiniteLine("k_line", coeffDir, transitionStage[0]);

            plot.addXYItem(band);
            plot.addXYItem(line);

            stageTransitionBand = new EditablePlotItem(band);
            stageTransitionLine = new EditablePlotItem(line);
            stageTransitionLine.setShowLegend(false);

            for (int k = 1; k < n; k++) {
                transitionStage = transitionStages.get(k);
                band = new PlotInfiniteBand("k_band", coeffDir, transitionStage[1], transitionStage[2]);
                line = new PlotInfiniteLine("k_line", coeffDir, transitionStage[0]);

                plot.addXYItem(band);
                plot.addXYItem(line);

                stageTransitionBand.addSibling(band);
                stageTransitionLine.addSibling(line);
            }
        }

        PlotBand totalUncertaintyBand = null;
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

            totalUncertaintyBand = new PlotBand("post_total_uncertainty",
                    stage,
                    smoothedTotalQUlow,
                    smoothedTotalQUhigh,
                    toolsPanel.axisFlipped());

            plot.addXYItem(totalUncertaintyBand);
        }

        // parametric uncertainty
        PlotBand paramUncertaintyBand = new PlotBand("param_uncertainty",
                stage,
                dischargeParamUncertainty.get(0),
                dischargeParamUncertainty.get(1),
                toolsPanel.axisFlipped());
        plot.addXYItem(paramUncertaintyBand);

        // maxpost
        double[] mpX = toolsPanel.axisFlipped() ? dischargeMaxpost : stage;
        double[] mpY = toolsPanel.axisFlipped() ? stage : dischargeMaxpost;

        PlotLine mpPlotLine = new PlotLine("mp", mpX, mpY);

        plot.addXYItem(mpPlotLine);

        // gaugings (only if posterior rc)
        PlotPoints gaugingsPoints = null;
        if (!isPrior && gaugings != null) {
            if (toolsPanel.axisFlipped()) {
                gaugingsPoints = new PlotPoints(
                        "gaugings",
                        gaugings.get(1),
                        gaugings.get(2),
                        gaugings.get(3),
                        gaugings.get(0),
                        gaugings.get(0),
                        gaugings.get(0),
                        AppSetup.COLORS.GAUGING);
            } else {
                gaugingsPoints = new PlotPoints(
                        "gaugings",
                        gaugings.get(0),
                        gaugings.get(0),
                        gaugings.get(0),
                        gaugings.get(1),
                        gaugings.get(2),
                        gaugings.get(3),
                        AppSetup.COLORS.GAUGING);

            }
            plot.addXYItem(gaugingsPoints);
        }

        plotContainer.setPlot(plot);

        if (gaugingsPoints != null) {
            plotEditor.addEditablePlotItem(
                    "gaugingsPoints",
                    gaugingsPoints.getLabel(),
                    gaugingsPoints);
        }

        plotEditor.addEditablePlotItem(
                "maxpostLine",
                mpPlotLine.getLabel(),
                mpPlotLine);

        plotEditor.addEditablePlotItem(
                "paramUncertaintyBand",
                paramUncertaintyBand.getLabel(),
                paramUncertaintyBand);

        if (totalUncertaintyBand != null) {
            plotEditor.addEditablePlotItem(
                    "totalUncertaintyBand",
                    totalUncertaintyBand.getLabel(),
                    totalUncertaintyBand);
        }

        if (n > 0) {
            plotEditor.addEditablePlotItem(
                    "stageLine",
                    stageTransitionLine.getLabel(),
                    stageTransitionLine);

            plotEditor.addEditablePlotItem(
                    "stageBand",
                    stageTransitionBand.getLabel(),
                    stageTransitionBand);

        }

        plotEditor.addEditablePlot(plot);

        plotEditor.updateEditor();

        setDefaultPlotEditorConfig();

        plot.update();

    }

    private void setDefaultPlotEditorConfig() {

        EditablePlotItem gaugings = plotEditor.getEditablePlotItem("gaugingsPoints");
        if (gaugings != null) {
            gaugings.setLabel(T.text("lgd_active_gaugings"));
            gaugings.setFillPaint(AppSetup.COLORS.GAUGING);
        }

        EditablePlotItem maxpost = plotEditor.getEditablePlotItem("maxpostLine");
        if (maxpost != null) {
            String mpLegendKey = isPrior ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
            Color mpColor = isPrior ? AppSetup.COLORS.PRIOR_LINE : AppSetup.COLORS.RATING_CURVE;
            maxpost.setLabel(T.text(mpLegendKey));
            maxpost.setLinePaint(mpColor);
            maxpost.setLineWidth(5);
        }

        // stage transition
        String stageLegendText = isPrior ? "lgd_prior_activation_stage"
                : "lgd_posterior_activation_stage";
        Color stageActivationValueColor = isPrior ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_VALUE
                : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_VALUE;
        Color stageActivationUncertaintyColor = isPrior ? AppSetup.COLORS.PRIOR_STAGE_ACTIVATION_UNCERTAINTY
                : AppSetup.COLORS.POSTERIOR_STAGE_ACTIVATION_UNCERTAINTY;

        EditablePlotItem stageTransitionBand = plotEditor.getEditablePlotItem("stageBand");
        EditablePlotItem stageTransitionLine = plotEditor.getEditablePlotItem("stageLine");
        stageTransitionBand.setLabel(T.text(stageLegendText));
        stageTransitionBand.setFillAlpha(0.9f);
        stageTransitionBand.setFillPaint(stageActivationUncertaintyColor);
        stageTransitionLine.setLabel(T.text(stageLegendText));
        stageTransitionLine.setLineWidth(2);
        stageTransitionLine.setLinePaint(stageActivationValueColor);

        // total uncertainty
        EditablePlotItem totalUncertainty = plotEditor.getEditablePlotItem("totalUncertaintyBand");
        if (totalUncertainty != null) {
            Color totalColor = isPrior ? null : AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY;
            totalUncertainty.setFillPaint(totalColor);
            totalUncertainty.setLabel(T.text("lgd_posterior_parametric_structural_uncertainty"));
        }

        // param uncertainty
        Color paramColor = isPrior ? AppSetup.COLORS.PRIOR_ENVELOP
                : AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY;
        String paramLegendKey = isPrior ? "lgd_prior_parametric_uncertainty"
                : "lgd_posterior_parametric_uncertainty";

        EditablePlotItem paramUncertainty = plotEditor.getEditablePlotItem("paramUncertaintyBand");
        paramUncertainty.setFillPaint(paramColor);
        paramUncertainty.setLabel(T.text(paramLegendKey));

        // plot axis legend items order
        EditablePlot p = plotEditor.getEditablePlot();

        if (toolsPanel.axisFlipped()) {
            p.setXAxisLabel(T.text("discharge") + " [m3/s]");
            p.setYAxisLabel(T.text("stage") + " [m]");
        } else {
            p.setXAxisLabel(T.text("stage") + " [m]");
            p.setYAxisLabel(T.text("discharge") + " [m3/s]");
        }

        if (isPrior) {
            p.updateLegendItems("maxpostLine",
                    "paramUncertaintyBand",
                    "stageBand");
        } else {

            p.updateLegendItems("gaugingsPoints",
                    "maxpostLine",
                    "paramUncertaintyBand",
                    "totalUncertaintyBand",
                    "stageBand");
        }

        plotEditor.updateEditor();
        plotEditor.saveAsDefault(false);
    }
}
