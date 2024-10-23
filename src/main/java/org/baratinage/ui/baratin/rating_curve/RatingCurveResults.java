package org.baratinage.ui.baratin.rating_curve;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.EstimatedParameter;
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

    private static ImageIcon rcIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve.svg");
    private static ImageIcon traceIcon = AppSetup.ICONS.getCustomAppImageIcon("trace.svg");
    private static ImageIcon tableIcon = AppSetup.ICONS.getCustomAppImageIcon("table.svg");
    private static ImageIcon dpIcon = AppSetup.ICONS.getCustomAppImageIcon("densities.svg");
    private static ImageIcon rcTblIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_table.svg");
    private static ImageIcon rcEqIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_equation.svg");

    private final BaremExporter baremeExporter;

    public RatingCurveResults(BamProject project) {

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

        addTab("rating_curve", rcIcon, ratingCurvePlot);
        addTab("Rating Curve table", rcTblIcon, rcGridTable);
        addTab("Rating Curve equation", rcEqIcon, rcEquation);
        addTab("parameter_densities", dpIcon, paramDensityPlots);
        addTab("parameter_table", tableIcon, paramSummaryTable);
        addTab("other_results", traceIcon, mcmcResultPanel);

        T.updateHierarchy(this, ratingCurvePlot);
        T.updateHierarchy(this, paramDensityPlots);
        T.updateHierarchy(this, rcGridTable);
        T.updateHierarchy(this, rcEquation);
        T.updateHierarchy(this, mcmcResultPanel);

        T.t(this, () -> {
            exportToBaremeButton.setText(T.text("export_to_bareme_format"));
            setTitleAt(0, T.html("posterior_rating_curve"));
            setTitleAt(1, T.html("rating_table"));
            setTitleAt(2, T.html("equation"));
            setTitleAt(3, T.html("parameter_densities"));
            setTitleAt(4, T.html("parameter_summary_table"));
            setTitleAt(5, T.html("mcmc_results"));
        });
    }

    public void updateResults2(
            RatingCurvePlotData rcPlotData, RatingCurveCalibrationResults parameters) {

        ratingCurvePlot.setPosteriorPlot(rcPlotData);

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

    public void updateQFHResults(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> gaugings,
            List<EstimatedParameter> parameters) {

    }

}
