package org.baratinage.ui.baratin;

import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;

public class PriorRatingCurvePlot extends RowColPanel {

    public void updatePlot(
            double[] stage,
            double[] dischargeMaxpost,
            List<double[]> dischargePriorParamUncertainty,
            List<double[]> transitionStages) {

        Plot plot = new Plot(true);

        PlotLine mp = new PlotLine(
                "Prior rating curve",
                stage,
                dischargeMaxpost,
                AppConfig.AC.PRIOR_LINE_COLOR,
                5);
        PlotBand parEnv = new PlotBand(
                "Prior parametric uncertainty",
                stage,
                dischargePriorParamUncertainty.get(0),
                dischargePriorParamUncertainty.get(1),
                AppConfig.AC.PRIOR_ENVELOP_COLOR);

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

        plot.addXYItem(parEnv);
        plot.addXYItem(mp);

        Lg.register(plot, () -> {
            mp.setLabel(Lg.text("prior_rating_curve"));
            parEnv.setLabel(Lg.text("prior_parametric_uncertainty"));
            bands[0].setLabel(Lg.text("prior_transition_stage"));
            plot.axisX.setLabel(Lg.text("stage_level"));
            plot.axisY.setLabel(Lg.text("discharge"));
            plot.axisYlog.setLabel(Lg.text("discharge"));
            plot.update(); // needed to make sure change are reflected in plot
            // actually useless here since axis label changes also notify the chart
            // that it must update...
        });

        PlotContainer plotContainer = new PlotContainer(plot);

        clear();
        appendChild(plotContainer);
    }

}
