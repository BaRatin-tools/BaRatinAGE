package org.baratinage.ui.baratin.rating_curve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.baratin.EstimatedControlParameters;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.commons.TracePlotGrid;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.DataTable;
import org.baratinage.translation.T;

public class RatingCurveResults extends TabContainer {

    private final RatingCurvePlot ratingCurvePlot;
    private final DensityPlotGrid paramDensityPlots;
    private final TracePlotGrid paramTracePlots;
    private final DataTable rcGridTable;
    private final RatingCurveEquation rcEquation;
    private final RowColPanel mcmcResultPanel;
    // private final RowColPanel otherPanel;
    private final DataTable paramSummaryTable;

    private static ImageIcon rcIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve.svg");
    private static ImageIcon traceIcon = AppSetup.ICONS.getCustomAppImageIcon("trace.svg");
    private static ImageIcon tableIcon = AppSetup.ICONS.getCustomAppImageIcon("table.svg");
    private static ImageIcon dpIcon = AppSetup.ICONS.getCustomAppImageIcon("densities.svg");
    private static ImageIcon rcTblIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_table.svg");
    private static ImageIcon rcEqIcon = AppSetup.ICONS.getCustomAppImageIcon("rating_curve_equation.svg");

    public RatingCurveResults() {

        ratingCurvePlot = new RatingCurvePlot();

        paramDensityPlots = new DensityPlotGrid();

        rcGridTable = new DataTable();

        rcEquation = new RatingCurveEquation();

        paramSummaryTable = new DataTable();

        paramTracePlots = new TracePlotGrid();

        mcmcResultPanel = new RowColPanel(
                RowColPanel.AXIS.COL);

        addTab("rating_curve", rcIcon, ratingCurvePlot);
        addTab("Rating Curve table", rcTblIcon, rcGridTable);
        addTab("Rating Curve equation", rcEqIcon, rcEquation);
        addTab("parameter_densities", dpIcon, paramDensityPlots);
        addTab("parameter_table", tableIcon, paramSummaryTable);
        addTab("other_results", traceIcon, mcmcResultPanel);

        T.updateHierarchy(this, ratingCurvePlot);
        T.updateHierarchy(this, paramDensityPlots);
        T.updateHierarchy(this, paramTracePlots);
        T.updateHierarchy(this, rcGridTable);
        T.updateHierarchy(this, rcEquation);
        T.updateHierarchy(this, mcmcResultPanel);

        T.t(this, () -> {
            setTitleAt(0, T.html("posterior_rating_curve"));
            setTitleAt(1, T.html("grid_table"));
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

        RatingCurveEstimatedParameters rcEstimParam = processParameters(parameters);

        updateRatingCurvePlot(stage, dischargeMaxpost, paramU, totalU, gaugings, rcEstimParam);

        updateRatingCurveGridTable(stage, dischargeMaxpost, paramU, totalU);

        updateParametersPlots(rcEstimParam);

        updateMcmcResultPanel(rcEstimParam);

        updateParameterSummaryTabel(rcEstimParam);

        rcEquation.updateEquation(rcEstimParam.controls());

    }

    private void updateRatingCurveGridTable(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU) {
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
        rcGridTable.setHeader(1, "Q_maxpost [m3.s-1]");
        rcGridTable.setHeader(2, "Q_param_low [m3.s-1]");
        rcGridTable.setHeader(3, "Q_param_high [m3.s-1]");
        rcGridTable.setHeader(4, "Q_total_low [m3.s-1]");
        rcGridTable.setHeader(5, "Q_total_high [m3.s-1]");
        rcGridTable.updateHeader();

        // T.t(this, () -> {
        // rcGridTable.setHeader(0, T.text("stage_level"));
        // rcGridTable.setHeader(1, T.text("discharge"));
        // rcGridTable.setHeader(2,
        // T.text("parametric_uncertainty") +
        // " - " + T.text("percentile_0025"));
        // rcGridTable.setHeader(3,
        // T.text("parametric_uncertainty") +
        // " - " + T.text("percentile_0975"));
        // rcGridTable.setHeader(4,
        // T.text("parametric_structural_uncertainty") +
        // " - " + T.text("percentile_0025"));
        // rcGridTable.setHeader(5,
        // T.text("parametric_structural_uncertainty") +
        // " - " + T.text("percentile_0975"));
        // rcGridTable.setHeaderWidth(100);
        // rcGridTable.updateHeader();
        // });

    }

    private static String getParameterConsistencyCheckString(float value, float thresholdOffset) {
        if (value < (0f + thresholdOffset) || value > (1f - thresholdOffset)) {
            return "POSSIBLE_INCONSISTENCY";
        }
        return "OK";
    }

    private void updateParameterSummaryTabel(RatingCurveEstimatedParameters parameters) {

        int n = parameters.controls().size() * 4;
        String[] parameterNames = new String[n];
        double[] priorLow = new double[n];
        double[] priorHigh = new double[n];
        double[] postMaxpost = new double[n];
        double[] postLow = new double[n];
        double[] postHigh = new double[n];
        String[] consistencyCheck = new String[n];

        int controlIndex = 1;
        int k = 0;
        for (EstimatedControlParameters control : parameters.controls()) {
            EstimatedParameter p;
            double[] prior95;
            double[] post95;

            p = control.k();
            prior95 = p.parameterConfig.distribution.getPercentiles(0.025, 0.975, 2);
            post95 = p.get95interval();
            parameterNames[k] = "k_" + controlIndex;
            priorLow[k] = prior95[0];
            priorHigh[k] = prior95[1];
            postMaxpost[k] = p.getMaxpost();
            postLow[k] = post95[0];
            postHigh[k] = post95[1];
            consistencyCheck[k] = getParameterConsistencyCheckString(
                    p.getValidityCheckEstimate(), 0.01f);
            k++;

            p = control.a();
            prior95 = p.parameterConfig.distribution.getPercentiles(0.025, 0.975, 2);
            post95 = p.get95interval();
            parameterNames[k] = "a_" + controlIndex;
            priorLow[k] = prior95[0];
            priorHigh[k] = prior95[1];
            postMaxpost[k] = p.getMaxpost();
            postLow[k] = post95[0];
            postHigh[k] = post95[1];
            consistencyCheck[k] = getParameterConsistencyCheckString(
                    p.getValidityCheckEstimate(), 0.01f);
            k++;

            p = control.c();
            prior95 = p.parameterConfig.distribution.getPercentiles(0.025, 0.975, 2);
            post95 = p.get95interval();
            parameterNames[k] = "c_" + controlIndex;
            priorLow[k] = prior95[0];
            priorHigh[k] = prior95[1];
            postMaxpost[k] = p.getMaxpost();
            postLow[k] = post95[0];
            postHigh[k] = post95[1];
            consistencyCheck[k] = getParameterConsistencyCheckString(
                    p.getValidityCheckEstimate(), 0.01f);
            k++;

            p = control.b();
            prior95 = new double[] { Double.NaN, Double.NaN };
            post95 = p.get95interval();
            parameterNames[k] = "b_" + controlIndex;
            priorLow[k] = prior95[0];
            priorHigh[k] = prior95[1];
            postMaxpost[k] = p.getMaxpost();
            postLow[k] = post95[0];
            postHigh[k] = post95[1];
            consistencyCheck[k] = "";
            k++;

            controlIndex++;
        }

        paramSummaryTable.clearColumns();

        paramSummaryTable.addColumn(parameterNames);
        paramSummaryTable.addColumn(priorLow);
        paramSummaryTable.addColumn(priorHigh);
        paramSummaryTable.addColumn(postMaxpost);
        paramSummaryTable.addColumn(postLow);
        paramSummaryTable.addColumn(postHigh);
        paramSummaryTable.addColumn(consistencyCheck);

        paramSummaryTable.updateData();

        // paramSummaryTable.setHeader(0, "Name");
        // paramSummaryTable.setHeader(1, "Prior Low (2.5%)");
        // paramSummaryTable.setHeader(2, "Prior High (97.5%)");
        // paramSummaryTable.setHeader(3, "Posterior Maxpost ");
        // paramSummaryTable.setHeader(4, "Posterior Low (2.5%)");
        // paramSummaryTable.setHeader(5, "Posterior High (97.5%)");
        paramSummaryTable.setHeader(0, "Name");
        paramSummaryTable.setHeader(1, "Prior_low");
        paramSummaryTable.setHeader(2, "Prior_high");
        paramSummaryTable.setHeader(3, "Posterior_maxpost");
        paramSummaryTable.setHeader(4, "Posterior_low");
        paramSummaryTable.setHeader(5, "Posterior_high");
        paramSummaryTable.setHeader(6, "consistency");

        paramSummaryTable.setHeaderWidth(100);

        paramSummaryTable.updateHeader();

    }

    private void updateRatingCurvePlot(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> gaugings,
            RatingCurveEstimatedParameters parameters) {

        List<double[]> transitionStages = new ArrayList<>();
        for (EstimatedControlParameters p : parameters.controls()) {
            double[] u95 = p.k().get95interval();
            double mp = p.k().getMaxpost();
            transitionStages.add(new double[] { mp, u95[0], u95[1] });
        }

        ratingCurvePlot.setPosteriorPlot(
                stage,
                dischargeMaxpost,
                paramU,
                totalU,
                transitionStages,
                gaugings);

    }

    private void updateParametersPlots(RatingCurveEstimatedParameters parameters) {

        paramDensityPlots.clearPlots();

        for (EstimatedControlParameters p : parameters.controls()) {
            paramDensityPlots.addPlot(p.k());
            paramDensityPlots.addPlot(p.a());
            paramDensityPlots.addPlot(p.c());
            paramDensityPlots.addPlot(p.b());
        }

        paramDensityPlots.addPlot(parameters.gamma1());
        paramDensityPlots.addPlot(parameters.gamma2());
        paramDensityPlots.addPlot(parameters.logPost());

        paramDensityPlots.updatePlots();

        paramTracePlots.clearPlots();

        for (EstimatedControlParameters p : parameters.controls()) {
            paramTracePlots.addPlot(p.k());
            paramTracePlots.addPlot(p.a());
            paramTracePlots.addPlot(p.c());
            paramTracePlots.addPlot(p.b());
        }

        paramTracePlots.addPlot(parameters.gamma1());
        paramTracePlots.addPlot(parameters.gamma2());
        paramTracePlots.addPlot(parameters.logPost());

        paramTracePlots.updatePlots();

    }

    private void updateMcmcResultPanel(RatingCurveEstimatedParameters parameters) {

        JButton mcmcToCsvButton = new JButton();
        mcmcResultPanel.clear();
        RowColPanel actionPanel = new RowColPanel();
        actionPanel.appendChild(mcmcToCsvButton, 0);
        mcmcResultPanel.appendChild(actionPanel, 0);
        mcmcResultPanel.appendChild(paramTracePlots, 1);

        // u = (maxpost - prior.getParval()[0]) / prior.getParval()[1]; // center-scale
        // if (Math.abs(u) > 2.33d) { // 2.33 corresponds to a 1% probability for a
        // N(0,1)

        mcmcToCsvButton.setIcon(AppSetup.ICONS.SAVE);
        T.t(mcmcResultPanel, () -> {
            mcmcToCsvButton.setText(T.text("export_mcmc"));
            mcmcToCsvButton.setToolTipText(T.text("export_mcmc"));
        });
        mcmcToCsvButton.addActionListener((e) -> {

            File f = CommonDialog.saveFileDialog(
                    T.text("export_mcmc"),
                    T.text("csv_format"),
                    "csv");

            if (f != null) {
                int m = parameters.controls().size();
                int n = m * 4 + 3;
                List<double[]> matrix = new ArrayList<>(n);
                String[] headers = new String[n];
                for (int k = 0; k < m; k++) {
                    matrix.add(parameters.controls().get(k).k().mcmc);
                    matrix.add(parameters.controls().get(k).a().mcmc);
                    matrix.add(parameters.controls().get(k).c().mcmc);
                    matrix.add(parameters.controls().get(k).b().mcmc);
                    headers[k * 4 + 0] = "k_" + k;
                    headers[k * 4 + 1] = "a_" + k;
                    headers[k * 4 + 2] = "c_" + k;
                    headers[k * 4 + 3] = "b_" + k;

                }
                matrix.add(parameters.gamma1().mcmc);
                matrix.add(parameters.gamma2().mcmc);
                matrix.add(parameters.logPost().mcmc);
                headers[m * 4 + 0] = "gamma_" + 1;
                headers[m * 4 + 1] = "gamma_" + 2;
                headers[m * 4 + 2] = "LogPost";

                try {
                    WriteFile.writeMatrix(
                            f.getAbsolutePath(),
                            matrix,
                            ",",
                            "-9999",
                            headers);
                } catch (IOException ioe) {
                    ConsoleLogger.error("error while exporting MCMC simulation");
                    ConsoleLogger.stackTrace(ioe);
                }
            }
        });

    }

    private record RatingCurveEstimatedParameters(
            List<EstimatedControlParameters> controls,
            EstimatedParameter gamma1,
            EstimatedParameter gamma2,
            EstimatedParameter logPost) {
    }

    private RatingCurveEstimatedParameters processParameters(List<EstimatedParameter> parameters) {

        List<ProcessedParameter> processedParameters = new ArrayList<>();

        int nControls = 0;
        for (EstimatedParameter p : parameters) {
            ProcessedParameter pp = processParameter(p);
            if (pp.isControlParameter()) {
                if (pp.index() > nControls) {
                    nControls = pp.index();
                }
            }
            processedParameters.add(pp);
        }

        EstimatedParameter logPostParameter = findParameter(processedParameters, ParameterType.LOG_POST);
        EstimatedParameter gamma1Parameter = findParameter(processedParameters, ParameterType.GAMMA, 1);
        EstimatedParameter gamma2Parameter = findParameter(processedParameters, ParameterType.GAMMA, 2);
        List<EstimatedControlParameters> kacbParameters = new ArrayList<>(nControls);
        for (int index = 1; index <= nControls; index++) {
            EstimatedParameter k = findParameter(processedParameters, ParameterType.K, index);
            EstimatedParameter a = findParameter(processedParameters, ParameterType.A, index);
            EstimatedParameter c = findParameter(processedParameters, ParameterType.C, index);
            EstimatedParameter b = findParameter(processedParameters, ParameterType.B, index);
            kacbParameters.add(new EstimatedControlParameters(k, a, c, b));
        }

        return new RatingCurveEstimatedParameters(
                kacbParameters,
                gamma1Parameter,
                gamma2Parameter,
                logPostParameter);
    }

    private EstimatedParameter findParameter(List<ProcessedParameter> processedParameters, ParameterType type) {
        return findParameter(processedParameters, type, -1);
    }

    private EstimatedParameter findParameter(List<ProcessedParameter> processedParameters, ParameterType type,
            int index) {
        for (ProcessedParameter p : processedParameters) {
            if (p.type() == type) {
                if (index >= 0) {
                    if (p.index() == index) {
                        return p.parameter();
                    }
                } else {
                    return p.parameter();
                }
            }
        }
        return null;
    }

    private enum ParameterType {
        LOG_POST, GAMMA, K, A, C, B;

        public static ParameterType getTypeFromString(String letter) {
            String l = letter.toLowerCase();
            if (l.equals("logpost")) {
                return LOG_POST;
            } else if (l.equals("gamma")) {
                return GAMMA;
            } else if (l.equals("k")) {
                return K;
            } else if (l.equals("a")) {
                return A;
            } else if (l.equals("c")) {
                return C;
            } else if (l.equals("b")) {
                return B;
            }
            return null;
        }
    }

    private record ProcessedParameter(
            ParameterType type,
            boolean isControlParameter,
            String niceName,
            int index,
            EstimatedParameter parameter) {

    }

    private static ProcessedParameter processParameter(EstimatedParameter parameter) {
        String rawName = parameter.name;
        if (rawName.equals("LogPost")) {
            return new ProcessedParameter(
                    ParameterType.LOG_POST,
                    false,
                    "LogPost",
                    -1,
                    parameter);
        } else if (rawName.startsWith("Y") && rawName.contains("gamma")) {
            String[] s = rawName.split("gamma_");
            int i = s.length == 2 ? parseInt(s[1]) : -1;
            i++;
            String niceName = String.format("<html>&gamma;<sub>%d</sub></html>", i);
            return new ProcessedParameter(
                    ParameterType.GAMMA,
                    false,
                    niceName,
                    i,
                    modifyEstimatedParameter(parameter, niceName, true));
        } else if (rawName.startsWith("b")) {
            String[] s = rawName.split("b");
            int i = s.length == 2 ? parseInt(s[1]) : -1;
            String niceName = String.format("<html>b<sub>%d</sub></html>", i);
            return new ProcessedParameter(
                    ParameterType.B,
                    true,
                    niceName,
                    i,
                    modifyEstimatedParameter(parameter, niceName, false));
        } else {
            String[] s = rawName.split("_");
            String n = s[0];
            int i = s.length == 2 ? parseInt(s[1]) : -1;
            i++;
            String niceName = String.format("<html>%s<sub>%d</sub></html>", n, i);
            return new ProcessedParameter(
                    ParameterType.getTypeFromString(n),
                    true,
                    niceName,
                    i,
                    modifyEstimatedParameter(parameter, niceName, false));
        }
    }

    private static int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (Exception e) {
            return -9999;
        }
    }

    private static EstimatedParameter modifyEstimatedParameter(
            EstimatedParameter p,
            String name,
            boolean noParameterConfig) {
        return new EstimatedParameter(
                name,
                p.mcmc,
                p.summary,
                p.maxpostIndex,
                noParameterConfig ? null : p.parameterConfig);
    }

}
