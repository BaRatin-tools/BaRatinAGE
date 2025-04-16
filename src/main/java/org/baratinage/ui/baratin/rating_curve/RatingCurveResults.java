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
import org.baratinage.ui.container.TabContainer;

import org.baratinage.translation.T;

public class RatingCurveResults extends TabContainer {

    private final RatingCurvePlot ratingCurvePlot;
    private final DensityPlotGrid paramDensityPlots;

    private final RatingCurveTable rcGridTable;
    private final RatingCurveEquation rcEquation;
    private final McmcTraceResultsPanel mcmcResultPanel;
    private final ParameterSummaryTable paramSummaryTable;

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

        paramDensityPlots = new DensityPlotGrid();

        rcGridTable = new RatingCurveTable();

        baremeExporter = new BaremExporter();

        JButton exportToBaremeButton = new JButton();
        exportToBaremeButton.addActionListener((e) -> {
            baremeExporter.exportRatingCurve();
        });
        rcGridTable.actionPanel.appendChild(exportToBaremeButton);

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

    public void updateResults(
            RatingCurvePlotData rcPlotData, RatingCurveCalibrationResults parameters) {

        if (rcPlotData.isPriorRatingCurve()) {
            ratingCurvePlot.setPriorPlot(rcPlotData);
        } else {
            ratingCurvePlot.setPosteriorPlot(rcPlotData);
        }

        rcGridTable.updateTable(rcPlotData);

        baremeExporter.updateRatingCurveValues(rcPlotData);

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
