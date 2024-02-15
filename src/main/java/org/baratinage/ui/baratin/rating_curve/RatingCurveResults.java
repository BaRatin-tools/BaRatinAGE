package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.EstimatedParameter;

import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.BamEstimatedParameter;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.commons.McmcTraceResultsPanel;
import org.baratinage.ui.commons.ParameterSummaryTable;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.component.DataTable;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.translation.T;

public class RatingCurveResults extends TabContainer {

    private final RatingCurvePlot ratingCurvePlot;
    private final DensityPlotGrid paramDensityPlots;

    private final DataTable rcGridTable;
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

        rcGridTable = new DataTable();

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

    public void updateResults(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> gaugings,
            List<EstimatedParameter> parameters) {

        // reorganize and process results
        OrganizedEstimatedParameters organizedParameters = processParameters(parameters);

        // rating curve plot
        List<double[]> transitionStages = organizedParameters.parameters
                .stream()
                .filter(bep -> bep.shortName.startsWith("k"))
                .map(bep -> {
                    double[] u95 = bep.get95interval();
                    double mp = bep.getMaxpost();
                    return new double[] { mp, u95[0], u95[1] };
                }).collect(Collectors.toList());

        ratingCurvePlot.setPosteriorPlot(
                stage,
                dischargeMaxpost,
                paramU,
                totalU,
                transitionStages,
                gaugings);

        // rating curve table
        rcGridTable.clearColumns();
        rcGridTable.addColumn(stage);
        rcGridTable.addColumn(dischargeMaxpost);
        rcGridTable.addColumn(paramU.get(0));
        rcGridTable.addColumn(paramU.get(1));
        rcGridTable.addColumn(totalU.get(0));
        rcGridTable.addColumn(totalU.get(1));
        rcGridTable.updateData();

        rcGridTable.setHeaderWidth(200);
        rcGridTable.setHeader(0, "h [m]");
        rcGridTable.setHeader(1, "Q_maxpost [m3/s]");
        rcGridTable.setHeader(2, "Q_param_low [m3/s]");
        rcGridTable.setHeader(3, "Q_param_high [m3/s]");
        rcGridTable.setHeader(4, "Q_total_low [m3/s]");
        rcGridTable.setHeader(5, "Q_total_high [m3/s]");
        rcGridTable.updateHeader();

        // with the barem exporter
        baremeExporter.updateRatingCurveValues(stage, dischargeMaxpost, totalU.get(0), totalU.get(1));

        // densities plots
        paramDensityPlots.clearPlots();
        for (BamEstimatedParameter p : organizedParameters.parameters) {
            paramDensityPlots.addPlot(p);
        }
        for (BamEstimatedParameter p : organizedParameters.gammas) {
            paramDensityPlots.addPlot(p);
        }
        paramDensityPlots.addPlot(organizedParameters.logPost);
        paramDensityPlots.updatePlots();

        // summaries of parameters as a table
        paramSummaryTable.updateResults(organizedParameters.parameters);

        // mcmc traces and export
        mcmcResultPanel.updateResults(
                organizedParameters.parameters,
                organizedParameters.gammas,
                organizedParameters.logPost);

        // rating curve equation
        rcEquation.updateKACBEquation(organizedParameters.parameters);

    }

    private record OrganizedEstimatedParameters(
            List<BamEstimatedParameter> parameters,
            List<BamEstimatedParameter> gammas,
            BamEstimatedParameter logPost) {

    }

    private OrganizedEstimatedParameters processParameters(List<EstimatedParameter> parameters) {

        List<BamEstimatedParameter> processedParameters = new ArrayList<>();
        for (EstimatedParameter p : parameters) {
            BamEstimatedParameter bp = processParameter(p);
            processedParameters.add(bp);
        }

        BamEstimatedParameter logPostParameter = processedParameters
                .stream().filter(bep -> bep.type.equals("LogPost"))
                .findFirst()
                .orElse(null);

        List<BamEstimatedParameter> gammas = processedParameters
                .stream()
                .filter(bep -> bep.isGammaParameter)
                .sorted(
                        Comparator
                                .<BamEstimatedParameter>comparingInt(bep -> bep.index)
                                .thenComparing(bep -> bep.type))
                .collect(Collectors.toList());

        Map<String, Integer> controlParameterOrder = Map.of("k", 0, "a", 1, "c", 2, "b", 3);
        List<BamEstimatedParameter> kacbParameters = processedParameters
                .stream()
                .filter(bep -> (bep.isEstimatedParameter || bep.isDerivedParameter) && !bep.isGammaParameter)
                .filter(bep -> controlParameterOrder.keySet().contains(bep.type))
                .sorted(
                        Comparator
                                .<BamEstimatedParameter>comparingInt(bep -> bep.index)
                                .thenComparing(bep -> controlParameterOrder.get(bep.type)))

                .collect(Collectors.toList());

        return new OrganizedEstimatedParameters(
                kacbParameters,
                gammas,
                logPostParameter);
    }

    private static record NamesAndIndex(String name, int index) {
    };

    private static Pattern gammaNamePattern = Pattern.compile("Y\\d+_gamma_(\\d+)");
    private static Pattern compParNamePattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
    private static Pattern confParNamePattern = Pattern.compile("([a-zA-Z]+)_(\\d+)");

    private static NamesAndIndex getNamesIndex(Pattern pattern, String rawName, String defaultName,
            boolean incrementIndex) {
        Matcher matcher = pattern.matcher(rawName);
        if (matcher.matches()) {
            String name = defaultName;
            int index = -1;
            try {
                if (matcher.groupCount() == 2) {
                    name = matcher.group(1);
                    index = Integer.parseInt(matcher.group(2));
                } else if (matcher.groupCount() == 1) {
                    index = Integer.parseInt(matcher.group(1));
                }
            } catch (Exception e) {
                ConsoleLogger.error(e);
            }
            if (incrementIndex && index >= 0) {
                index++;
            }
            return new NamesAndIndex(name, index);
        } else {
            return new NamesAndIndex("", -1);
        }
    }

    private static BamEstimatedParameter processParameter(EstimatedParameter parameter) {
        if (parameter.name.equals("LogPost")) {
            return new BamEstimatedParameter(parameter, "LogPost", "LogPost",
                    false, false, false,
                    "LogPost", -1);
        }
        if (parameter.name.startsWith("Y") && parameter.name.contains("gamma")) {
            NamesAndIndex namesAndIndex = getNamesIndex(gammaNamePattern, parameter.name, "&gamma;", true);
            String niceName = String.format(
                    "<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    true, false, true,
                    "&gamma;", namesAndIndex.index);
        }
        if (parameter.name.contains("_")) {
            NamesAndIndex namesAndIndex = getNamesIndex(confParNamePattern, parameter.name, "", true);
            String niceName = String.format(
                    "<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    true, false, false,
                    namesAndIndex.name, namesAndIndex.index);
        } else {
            NamesAndIndex namesAndIndex = getNamesIndex(compParNamePattern, parameter.name, "", false);
            String niceName = String.format("<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    false, true, false,
                    namesAndIndex.name, namesAndIndex.index);
        }
    }
}
