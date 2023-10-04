package org.baratinage.ui.baratin;

import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotPoints;

public class PosteriorRatingCurvePlot extends RowColPanel {
        public void updatePlot(
                        double[] stage,
                        double[] dischargeMaxpost,
                        List<double[]> dischargeParametricUncertainty,
                        List<double[]> dischargeTotalUncertainty,
                        List<double[]> transitionStages,
                        List<double[]> gaugings) {

                Plot plot = new Plot(true);

                PlotLine mp = new PlotLine(
                                "Posterior rating curve",
                                stage,
                                dischargeMaxpost,
                                AppConfig.AC.RATING_CURVE_COLOR,
                                5);

                PlotBand totEnv = new PlotBand(
                                "Structural and parametric uncertainty",
                                stage,
                                dischargeTotalUncertainty.get(0),
                                dischargeTotalUncertainty.get(1),
                                AppConfig.AC.RATING_CURVE_TOTAL_UNCERTAINTY_COLOR);

                PlotBand parEnv = new PlotBand(
                                "Parametric uncertainty",
                                stage,
                                dischargeParametricUncertainty.get(0),
                                dischargeParametricUncertainty.get(1),
                                AppConfig.AC.RATING_CURVE_PARAM_UNCERTAINTY_COLOR);

                int n = transitionStages.size();
                PlotInfiniteBand[] bands = new PlotInfiniteBand[n];
                for (int k = 0; k < n; k++) {
                        double[] transitionStage = transitionStages.get(k);
                        PlotInfiniteLine line = new PlotInfiniteLine("k_" + k, transitionStage[0],
                                        AppConfig.AC.STAGE_TRANSITION_VALUE_COLOR, 2);
                        bands[k] = new PlotInfiniteBand("Hauteur de transition",
                                        transitionStage[1], transitionStage[2],
                                        AppConfig.AC.STAGE_TRANSITION_UNCERTAINTY_COLOR);
                        plot.addXYItem(line, false);
                        plot.addXYItem(bands[k], k == 0);
                }

                PlotPoints gaugingsPoints = new PlotPoints(
                                "gaugings",
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(0),
                                gaugings.get(1),
                                gaugings.get(2),
                                gaugings.get(3),
                                AppConfig.AC.GAUGING_COLOR);

                plot.addXYItem(totEnv);
                plot.addXYItem(parEnv);
                plot.addXYItem(mp);
                plot.addXYItem(gaugingsPoints);

                T.t(mp, "lgd_posterior_rating_curve");
                T.t(parEnv, "lgd_posterior_parametric_uncertainty");
                T.t(totEnv, "lgd_posterior_parametric_structural_uncertainty");
                T.t(bands[0], "lgd_posterior_transition_stage");
                T.t(gaugingsPoints, "lgd_active_gaugings");

                T.t(plot, (plt) -> {
                        plt.axisX.setLabel(T.text("stage_level"));
                        plt.axisY.setLabel(T.text("discharge"));
                        plt.axisYlog.setLabel(T.text("discharge"));
                        plt.update(); // needed to make sure change are reflected in plot
                        // actually useless here since axis label changes also notify the chart
                        // that it must update...
                });

                PlotContainer plotContainer = new PlotContainer(plot);

                clear();
                appendChild(plotContainer);
        }
}
