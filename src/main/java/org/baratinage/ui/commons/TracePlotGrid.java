package org.baratinage.ui.commons;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;

public class TracePlotGrid extends RowColPanel {
    // FIXME: refactoring possible with DensityPlotGrid
    private final List<EstimatedParameter> estimatedParameters = new ArrayList<>();

    public void addPlot(EstimatedParameter estimatedParameter) {
        estimatedParameters.add(estimatedParameter);
    }

    public void clearPlots() {
        estimatedParameters.clear();
    }

    public void updatePlots() {
        GridPanel gridPanel = new GridPanel();
        int nColMax = 4;
        int nPlots = estimatedParameters.size();
        int nCol = nColMax;
        // since Java 18: int Math.ceilDiv(int, int) could be used
        int nRow = (int) Math.ceil(((double) nPlots) / ((double) nCol));

        clear();
        appendChild(gridPanel, 1);

        for (int k = 0; k < nCol; k++) {
            gridPanel.setColWeight(k, 1);
        }
        for (int k = 0; k < nRow + 1; k++) {
            gridPanel.setRowWeight(k, 1);
        }

        int r = 0;
        int c = 0;

        int nMcmc = estimatedParameters.get(0).mcmc.length;
        double[] x = Calc.sequence(0, nMcmc, nMcmc);
        for (int k = 0; k < estimatedParameters.size(); k++) {

            EstimatedParameter estimParam = estimatedParameters.get(k);

            Plot plot = new Plot(false, false);

            FixedTextAnnotation title = new FixedTextAnnotation(estimParam.name, 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            PlotLine trace = new PlotLine("", x, estimParam.mcmc,
                    AppConfig.AC.PLOT_LINE_COLOR, 1);
            PlotInfiniteLine mp = new PlotInfiniteLine("",
                    estimParam.maxpostIndex,
                    AppConfig.AC.POSTERIOR_LINE_COLOR,
                    2);

            plot.addXYItem(mp);
            plot.addXYItem(trace);

            PlotContainer pc = new PlotContainer(plot, false);
            T.updateHierarchy(this, pc);
            gridPanel.insertChild(pc, c, r);

            c++;
            if (c >= nColMax) {
                c = 0;
                r++;
            }
        }

        // Legend legend = new Legend();

        // PlotContainer pc = new PlotContainer(legend.getLegendPlot(), false);
        // T.updateHierarchy(this, pc);

        // gridPanel.insertChild(pc, c, r);

        // T.t(this, () -> {
        // legend.clearLegend();

        // legend.addLegendItem(PlotBand.buildLegendItem(
        // T.text("prior_density"),
        // AppConfig.AC.PRIOR_ENVELOP_COLOR));
        // legend.addLegendItem(PlotBand.buildLegendItem(
        // T.text("posterior_density"),
        // AppConfig.AC.POSTERIOR_ENVELOP_COLOR));
        // legend.addLegendItem(PlotLine.buildLegendItem(
        // T.text("maxpost"),
        // AppConfig.AC.POSTERIOR_LINE_COLOR,
        // new BasicStroke(3f)));

        // legend.getLegendPlot().update();
        // });

    }
}
