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
    private DataTable mcmcTable;

    private static ImageIcon rcIcon = SvgIcon.buildCustomAppImageIcon("rating_curve.svg");
    private static ImageIcon dpIcon = SvgIcon.buildCustomAppImageIcon("densities.svg");
    private static ImageIcon rcTblIcon = SvgIcon.buildCustomAppImageIcon("rating_curve_table.svg");

    public RatingCurveResults() {

        ratingCurvePlot = new PosteriorRatingCurvePlot();

        paramDensityPlots = new DensityPlotGrid();

        rcGridTable = new DataTable();

        addTab("rating_curve", rcIcon, ratingCurvePlot);
        addTab("parameter_densities", dpIcon, paramDensityPlots);
        addTab("Rating Curve Table", rcTblIcon, rcGridTable);

        T.updateHierarchy(this, ratingCurvePlot);
        T.updateHierarchy(this, paramDensityPlots);
        T.t(this, () -> {
            setTitleAt(0, T.html("posterior_rating_curve"));
            setTitleAt(1, T.html("parameter_densities"));
        });
    }

    public void updateResults(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> transitionStages,
            List<double[]> gaugings,
            List<EstimatedParameter> parameters) {
        updatePlots(stage, dischargeMaxpost, paramU, totalU, transitionStages, gaugings, parameters);
        updateTables(stage, dischargeMaxpost, paramU, totalU, transitionStages, gaugings, parameters);
    }

    public void updateTables(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> transitionStages,
            List<double[]> gaugings,
            List<EstimatedParameter> parameters) {
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

    public void updatePlots(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> paramU,
            List<double[]> totalU,
            List<double[]> transitionStages,
            List<double[]> gaugings,
            List<EstimatedParameter> parameters) {
        ratingCurvePlot.updatePlot(
                stage,
                dischargeMaxpost,
                paramU,
                totalU,
                transitionStages,
                gaugings);

        List<EstimatedParameter> modifiedParameters = reorganizeAndRenameParameters(parameters);

        paramDensityPlots.clearPlots();

        for (EstimatedParameter p : modifiedParameters) {
            paramDensityPlots.addPlot(p);
        }

        paramDensityPlots.updatePlots();

    }

    private List<EstimatedParameter> reorganizeAndRenameParameters(List<EstimatedParameter> parameters) {

        EstimatedParameter logPostParameter = null;
        EstimatedParameter[] controlParameters = new EstimatedParameter[parameters.size()];
        EstimatedParameter[] strucErrorParameters = new EstimatedParameter[2];

        int nControlPar = 0;
        int nStrucErrPar = 0;

        for (EstimatedParameter p : parameters) {
            String rawName = p.name;
            if (rawName.equals("LogPost")) {
                logPostParameter = p;
            } else if (rawName.startsWith("Y") && rawName.contains("gamma")) {
                int n = getParameterIndex(rawName, "gamma_");
                String niceName = String.format("<html>&gamma;<sub>%d</sub></html>", n + 1);
                strucErrorParameters[n] = new EstimatedParameter(
                        niceName,
                        p.mcmc,
                        p.summary,
                        p.maxpostIndex,
                        null); // null so prior dist is not accounted for in plot
                nStrucErrPar++;
            } else if (rawName.startsWith("b")) {
                int n = getParameterIndex(rawName, "b");
                String niceName = String.format("<html>b<sub>%s</sub></html>", n);
                int m = (n - 1) * 4 + 3;
                controlParameters[m] = new EstimatedParameter(
                        niceName,
                        p.mcmc,
                        p.summary,
                        p.maxpostIndex,
                        p.parameterConfig);
                nControlPar++;
            } else {
                String[] s = rawName.split("_");
                String name = s[0];
                int n = getParameterIndex(rawName, "_");
                String niceName = String.format("<html>%s<sub>%d</sub></html>", name, n + 1);
                int m = n * 4;
                if (rawName.startsWith("a")) {
                    m = m + 1;
                } else if (rawName.startsWith("c")) {
                    m = m + 2;
                }
                controlParameters[m] = new EstimatedParameter(
                        niceName,
                        p.mcmc,
                        p.summary,
                        p.maxpostIndex,
                        p.parameterConfig);
                nControlPar++;
            }
        }

        List<EstimatedParameter> modifiedParameters = new ArrayList<>();
        for (int k = 0; k < nControlPar; k++) {
            modifiedParameters.add(controlParameters[k]);
        }
        for (int k = 0; k < nStrucErrPar; k++) {
            modifiedParameters.add(strucErrorParameters[k]);
        }
        if (logPostParameter != null) {
            modifiedParameters.add(logPostParameter);
        }

        return modifiedParameters;
    }

    private int getParameterIndex(String rawName, String splitChar) {
        String[] s = rawName.split(splitChar);
        int n = -1;
        if (s.length == 2) {
            try {
                n = Integer.parseInt(s[1]);
            } catch (Exception e) {

            }
        }
        return n;
    }

}
