package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.translation.T;

public class RatingCurveResults extends TabContainer {

    private PosteriorRatingCurvePlot ratingCurvePlot;
    private DensityPlotGrid paramDensityPlots;
    private DataTable rcGridTable;
    private RatingCurveEquation rcEquation;
    // private DataTable mcmcTable;

    private static ImageIcon rcIcon = SvgIcon.buildCustomAppImageIcon("rating_curve.svg");
    private static ImageIcon dpIcon = SvgIcon.buildCustomAppImageIcon("densities.svg");
    private static ImageIcon rcTblIcon = SvgIcon.buildCustomAppImageIcon("rating_curve_table.svg");
    private static ImageIcon rcEqIcon = SvgIcon.buildCustomAppImageIcon("rating_curve_equation.svg");

    public RatingCurveResults() {

        ratingCurvePlot = new PosteriorRatingCurvePlot();

        paramDensityPlots = new DensityPlotGrid();

        rcGridTable = new DataTable();

        rcEquation = new RatingCurveEquation();

        addTab("rating_curve", rcIcon, ratingCurvePlot);
        addTab("Rating Curve table", rcTblIcon, rcGridTable);
        addTab("Rating Curve equation", rcEqIcon, rcEquation);
        addTab("parameter_densities", dpIcon, paramDensityPlots);

        T.updateHierarchy(this, ratingCurvePlot);
        T.updateHierarchy(this, paramDensityPlots);
        T.updateHierarchy(this, rcGridTable);
        T.updateHierarchy(this, rcEquation);
        T.t(this, () -> {
            setTitleAt(0, T.html("posterior_rating_curve"));
            setTitleAt(1, T.html("grid_table"));
            setTitleAt(2, T.html("equation"));
            setTitleAt(3, T.html("parameter_densities"));
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

        updatePlots(stage, dischargeMaxpost, paramU, totalU, gaugings, rcEstimParam);
        updateTables(stage, dischargeMaxpost, paramU, totalU);
        rcEquation.updateEquation(rcEstimParam.controls());
    }

    private void updateTables(
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
        T.t(this, () -> {
            rcGridTable.setHeader(0, T.text("stage_level"));
            rcGridTable.setHeader(1, T.text("discharge"));
            rcGridTable.setHeader(2,
                    T.text("parametric_uncertainty") +
                            " - " + T.text("percentile_0025"));
            rcGridTable.setHeader(3,
                    T.text("parametric_uncertainty") +
                            " - " + T.text("percentile_0975"));
            rcGridTable.setHeader(4,
                    T.text("parametric_structural_uncertainty") +
                            " - " + T.text("percentile_0025"));
            rcGridTable.setHeader(5,
                    T.text("parametric_structural_uncertainty") +
                            " - " + T.text("percentile_0975"));
            rcGridTable.autosetHeadersWidths(100, 300);
            rcGridTable.updateHeader();
        });

    }

    private void updatePlots(
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

        ratingCurvePlot.updatePlot(
                stage,
                dischargeMaxpost,
                paramU,
                totalU,
                transitionStages,
                gaugings);

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
