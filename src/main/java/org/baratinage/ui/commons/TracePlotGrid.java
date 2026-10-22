package org.baratinage.ui.commons;

import java.awt.BasicStroke;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.ResultColumn;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.GridPlotContainer;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;

public class TracePlotGrid extends SimpleFlowPanel {

    GridPlotContainer gridPanel = null;
    public final List<ResultColumn> parameters = new ArrayList<>();
    private final List<AbstractButton> actionButtons = new ArrayList<>();

    public void addPlot(ResultColumn estimatedParameter) {
        parameters.add(estimatedParameter);
    }

    public void addButton(AbstractButton button) {
        actionButtons.add(button);
    }

    public void clearPlots() {
        parameters.clear();
    }

    public void updatePlots() {

        int nColMax = 4;
        int nPlots = parameters.size();
        int nCol = nColMax;
        // since Java 18: int Math.ceilDiv(int, int) could be used
        int nRow = (int) Math.ceil(((double) nPlots) / ((double) nCol));

        gridPanel = new GridPlotContainer(nRow, nCol);
        for (AbstractButton btn : actionButtons) {
            gridPanel.toolsPanel.add(btn);
        }
        T.updateHierarchy(this, gridPanel);

        removeAll();
        addChild(gridPanel, true);

        int nMcmc = parameters.get(0).samples.length;
        double[] x = Calc.sequence(0, nMcmc, nMcmc);
        for (int k = 0; k < parameters.size(); k++) {

            ResultColumn p = parameters.get(k);

            Plot plot = new Plot(false, false);

            FixedTextAnnotation title = new FixedTextAnnotation(p.symbol.toHtml(), 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            PlotLine trace = new PlotLine("", x, p.samples,
                    AppSetup.COLORS.PLOT_LINE, 1);
            PlotInfiniteLine mp = new PlotInfiniteLine("",
                    p.maxpostIndex,
                    AppSetup.COLORS.POSTERIOR_LINE,
                    2);

            plot.addXYItem(mp);
            plot.addXYItem(trace);

            gridPanel.addPlot(plot);
        }

        Legend legend = new Legend();
        gridPanel.addPlot(legend.getLegendPlot());

        T.t(this, () -> {
            legend.clearLegend();

            legend.addLegendItem(PlotLine.buildLegendItem(
                    T.text("maxpost"),
                    AppSetup.COLORS.POSTERIOR_LINE,
                    new BasicStroke(2f)));

            legend.getLegendPlot().update();
        });
    }

    public IExportablePlot getPlot() {
        return gridPanel;
    }
}
