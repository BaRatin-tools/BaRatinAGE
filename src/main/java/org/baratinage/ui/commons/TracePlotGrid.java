package org.baratinage.ui.commons;

import java.awt.BasicStroke;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
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
    public final List<EstimatedParameterWrapper> estimatedParameters = new ArrayList<>();
    private final List<AbstractButton> actionButtons = new ArrayList<>();

    public void addPlot(EstimatedParameterWrapper estimatedParameter) {
        estimatedParameters.add(estimatedParameter);
    }

    public void addButton(AbstractButton button) {
        actionButtons.add(button);
    }

    public void clearPlots() {
        estimatedParameters.clear();
    }

    public void updatePlots() {

        int nColMax = 4;
        int nPlots = estimatedParameters.size();
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

        int nMcmc = estimatedParameters.get(0).parameter.mcmc.length;
        double[] x = Calc.sequence(0, nMcmc, nMcmc);
        for (int k = 0; k < estimatedParameters.size(); k++) {

            EstimatedParameterWrapper estimParam = estimatedParameters.get(k);

            Plot plot = new Plot(false, false);

            FixedTextAnnotation title = new FixedTextAnnotation(estimParam.htmlName, 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            PlotLine trace = new PlotLine("", x, estimParam.parameter.mcmc,
                    AppSetup.COLORS.PLOT_LINE, 1);
            PlotInfiniteLine mp = new PlotInfiniteLine("",
                    estimParam.parameter.maxpostIndex,
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
