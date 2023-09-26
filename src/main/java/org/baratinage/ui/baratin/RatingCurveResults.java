package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.lg.Lg;

public class RatingCurveResults extends JTabbedPane {

    private PosteriorRatingCurvePlot ratingCurvePlot;
    private DensityPlotGrid paramDensityPlots;

    public RatingCurveResults() {

        ratingCurvePlot = new PosteriorRatingCurvePlot();

        paramDensityPlots = new DensityPlotGrid();

        addTab("rating_curve",
                ratingCurvePlot);
        addTab("parameter_densities",
                paramDensityPlots);

        ImageIcon rcIcon = SvgIcon.buildCustomAppImageIcon("rating_curve.svg", AppConfig.AC.ICON_SIZE);
        ImageIcon dpIcon = SvgIcon.buildCustomAppImageIcon("densities.svg", AppConfig.AC.ICON_SIZE);

        JLabel ratingCurveTitle = new JLabel();
        ratingCurveTitle.setIcon(rcIcon);
        setTabComponentAt(0, ratingCurveTitle);

        JLabel paraDensitiesTitle = new JLabel();
        paraDensitiesTitle.setIcon(dpIcon);
        setTabComponentAt(1, paraDensitiesTitle);

        Lg.register(this, () -> {
            ratingCurveTitle.setText(Lg.text("posterior_rating_curve"));
            paraDensitiesTitle.setText(Lg.text("parameter_densities"));
        });
    }

    public void updatePlot(
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

        for (EstimatedParameter p : modifiedParameters) {
            paramDensityPlots.addPlot(p);
        }

        paramDensityPlots.updatePlot();

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
                int n = getParameterIndex(rawName, "gamma");
                String niceName = String.format("<html>&gamma;<sub>%d</sub></html>", n);
                strucErrorParameters[n - 1] = new EstimatedParameter(niceName, p.mcmc, p.summary, p.maxpostIndex,
                        p.parameterConfig);
                nStrucErrPar++;
            } else if (rawName.startsWith("b")) {
                int n = getParameterIndex(rawName, "b");
                String niceName = String.format("<html>b<sub>%s</sub></html>", n);
                int m = (n - 1) * 4 + 2;
                controlParameters[m] = new EstimatedParameter(niceName, p.mcmc, p.summary, p.maxpostIndex,
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
                    m = m + 3;
                }
                controlParameters[m] = new EstimatedParameter(niceName, p.mcmc, p.summary, p.maxpostIndex,
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