package org.baratinage.ui.baratin;

import java.awt.Color;
import java.util.List;

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
            List<double[]> discharge,
            List<double[]> transitionStages) {

        Plot plot = new Plot(true);

        PlotLine mp = new PlotLine(
                "Prior rating curve",
                stage,
                discharge.get(0),
                Color.BLACK,
                5);
        PlotBand parEnv = new PlotBand(
                "Prior parametric uncertainty",
                stage,
                discharge.get(1),
                discharge.get(2),
                new Color(200, 200, 255, 100));

        int n = transitionStages.size();
        PlotInfiniteBand[] bands = new PlotInfiniteBand[n];
        for (int k = 0; k < n; k++) {
            double[] transitionStage = transitionStages.get(k);
            PlotInfiniteLine line = new PlotInfiniteLine("k_" + k, transitionStage[0],
                    Color.GREEN, 2);
            bands[k] = new PlotInfiniteBand("Hauteur de transition",
                    transitionStage[1], transitionStage[2], new Color(100, 255, 100, 100));
            plot.addXYItem(line, false);
            plot.addXYItem(bands[k], k == 0);
        }

        plot.addXYItem(mp);
        plot.addXYItem(parEnv);

        Lg.register(plot, () -> {
            mp.setLabel(Lg.text("prior_rating_curve"));
            parEnv.setLabel(Lg.text("prior_parametric_uncertainty"));
            bands[0].setLabel(Lg.text("prior_transition_stage"));
            plot.axisX.setLabel(Lg.text("stage_level"));
            plot.axisY.setLabel(Lg.text("discharge"));
            plot.axisYlog.setLabel(Lg.text("discharge"));
        });

        PlotContainer plotContainer = new PlotContainer(plot);

        clear();
        appendChild(plotContainer);
    }

}
