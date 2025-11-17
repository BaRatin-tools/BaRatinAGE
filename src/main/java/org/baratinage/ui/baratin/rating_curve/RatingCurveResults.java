package org.baratinage.ui.baratin.rating_curve;

// import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;

import org.baratinage.AppSetup;
// import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.commons.McmcTraceResultsPanel;
import org.baratinage.ui.commons.ParameterSummaryTable;
import org.baratinage.ui.commons.ReactiveValue;
import org.baratinage.ui.container.TabContainer;

import org.baratinage.translation.T;

public class RatingCurveResults extends TabContainer {

    public final ReactiveValue<Boolean> cropTotalUncertaintyToZero = new ReactiveValue<Boolean>(false);

    public final RatingCurvePlot ratingCurvePlot;
    public final DensityPlotGrid paramDensityPlots;

    private final RatingCurveTable rcGridTable;
    public final RatingCurveEquation rcEquation;
    public final McmcTraceResultsPanel mcmcResultPanel;
    public final ParameterSummaryTable paramSummaryTable;

    private static Icon rcPriorIcon = AppSetup.ICONS.getCustomAppImageIcon("prior_rating_curve.svg");
    private static Icon rcIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve.svg");
    private static Icon traceIcon = AppSetup.ICONS.getCustomAppImageIcon("trace.svg");
    private static Icon tableIcon = AppSetup.ICONS.getCustomAppImageIcon("table.svg");
    private static Icon dpIcon = AppSetup.ICONS.getCustomAppImageIcon("densities.svg");
    private static Icon rcTblIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_table.svg");
    private static Icon rcEqIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_equation.svg");

    private final BaremExporter baremeExporter;

    public RatingCurveResults(BamProject project) {
        this(project, false);
    }

    public RatingCurveResults(BamProject project, boolean priorResults) {

        ratingCurvePlot = new RatingCurvePlot();

        ratingCurvePlot.toolsPanel.addChangeListener(l -> {
            cropTotalUncertaintyToZero.set(ratingCurvePlot.toolsPanel.cropTotalEnv());
        });

        paramDensityPlots = new DensityPlotGrid();

        rcGridTable = new RatingCurveTable();

        rcGridTable.cropTotalEnvelopCheckbox.addChangeListener(l -> {
            cropTotalUncertaintyToZero.set(rcGridTable.cropTotalEnvelopCheckbox.isSelected());
        });

        cropTotalUncertaintyToZero.addListener(newValue -> {
            ratingCurvePlot.toolsPanel.cropTotalEnvelopCheckbox.setSelected(newValue);
            rcGridTable.cropTotalEnvelopCheckbox.setSelected(newValue);
            updateResults(rcPlotData, parameters);
        });

        baremeExporter = new BaremExporter();

        JButton exportToBaremeButton = new JButton();
        exportToBaremeButton.addActionListener((e) -> {
            baremeExporter.syncWithAllOtherInstances();
            baremeExporter.exportRatingCurve();
        });
        rcGridTable.actionPanel.addChild(exportToBaremeButton, false);

        rcEquation = new RatingCurveEquation();

        paramSummaryTable = new ParameterSummaryTable();

        mcmcResultPanel = new McmcTraceResultsPanel(project);

        if (priorResults) {
            addTab("Rating_curve plot", rcPriorIcon, ratingCurvePlot);
            addTab("Rating Curve table", rcTblIcon, rcGridTable);
        } else {
            addTab("Rating_curve plot", rcIcon, ratingCurvePlot);
            addTab("Rating Curve table", rcTblIcon, rcGridTable);
            addTab("Rating Curve equation", rcEqIcon, rcEquation);
            addTab("parameter_densities", dpIcon, paramDensityPlots);
            addTab("parameter_table", tableIcon, paramSummaryTable);
            addTab("other_results", traceIcon, mcmcResultPanel);
        }

        T.updateHierarchy(this, ratingCurvePlot);
        T.updateHierarchy(this, paramDensityPlots);
        T.updateHierarchy(this, rcGridTable);
        T.updateHierarchy(this, rcEquation);
        T.updateHierarchy(this, mcmcResultPanel);

        T.t(this, () -> {
            exportToBaremeButton.setText(T.text("export_to_bareme_format"));
            setTitleAt(1, T.html("rating_table"));
            if (priorResults) {
                setTitleAt(0, T.html("prior_rating_curve"));
            } else {
                setTitleAt(0, T.html("posterior_rating_curve"));
                setTitleAt(2, T.html("equation"));
                setTitleAt(3, T.html("parameter_densities"));
                setTitleAt(4, T.html("parameter_summary_table"));
                setTitleAt(5, T.html("mcmc_results"));
            }
            if (priorResults) {
                setTitleAt(0, T.html("prior_rating_curve"));
            } else {
                setTitleAt(0, T.html("posterior_rating_curve"));
                setTitleAt(2, T.html("equation"));
                setTitleAt(3, T.html("parameter_densities"));
                setTitleAt(4, T.html("parameter_summary_table"));
                setTitleAt(5, T.html("mcmc_results"));
            }
        });
    }

    private RatingCurvePlotData rcPlotData;
    private RatingCurveCalibrationResults parameters;

    public void updateResults(
            RatingCurvePlotData rcPlotData,
            RatingCurveCalibrationResults parameters) {

        this.rcPlotData = rcPlotData;
        this.parameters = parameters;

        RatingCurvePlotData rcPlotDataModified = rcPlotData;
        if (cropTotalUncertaintyToZero.get()) {
            rcPlotDataModified = rcPlotDataModified.cropTotalEnvelopValues();
        }

        if (rcPlotDataModified.isPriorRatingCurve()) {
            ratingCurvePlot.setPriorPlot(rcPlotDataModified);
        } else {
            ratingCurvePlot.setPosteriorPlot(rcPlotDataModified);
        }

        rcGridTable.updateTable(rcPlotDataModified);

        baremeExporter.updateRatingCurveValues(rcPlotDataModified);

        rcEquation.updateEquation(parameters.getEquationString());

        paramDensityPlots.clearPlots();
        for (EstimatedParameterWrapper p : parameters.getAllParameters()) {
            paramDensityPlots.addPlot(p);
        }
        paramDensityPlots.updatePlots();

        paramSummaryTable.updateResults(parameters.getModelAndDerivedParameters());

        mcmcResultPanel.updateResults(parameters.getAllParameters());

    }
}
