package org.baratinage.ui.commons;

import java.awt.BasicStroke;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;
import org.baratinage.translation.T;

public class DensityPlotGrid extends SimpleFlowPanel {

    public final List<EstimatedParameterWrapper> estimatedParameters = new ArrayList<>();
    public final List<PlotContainer> plotContainers = new ArrayList<>();

    public boolean isTimeSeries = false;

    public void addPlot(EstimatedParameterWrapper estimatedParameter) {
        estimatedParameters.add(estimatedParameter);
    }

    public void clearPlots() {
        estimatedParameters.clear();
    }

    public void updatePlots() {

        plotContainers.clear();
        GridPanel gridPanel = new GridPanel();
        int nColMax = 4;
        int nPlots = estimatedParameters.size();
        int nCol = nColMax;
        // since Java 18: int Math.ceilDiv(int, int) could be used
        int nRow = (int) Math.ceil(((double) nPlots) / ((double) nCol));

        removeAll();
        addChild(gridPanel, true);

        for (int k = 0; k < nCol; k++) {
            gridPanel.setColWeight(k, 1);
        }
        for (int k = 0; k < nRow + 1; k++) {
            gridPanel.setRowWeight(k, 1);
        }

        boolean anyPriorCurve = false;

        int r = 0;
        int c = 0;
        for (int k = 0; k < estimatedParameters.size(); k++) {

            EstimatedParameterWrapper estimParam = estimatedParameters.get(k);

            Plot plot = new Plot(false, isTimeSeries);

            FixedTextAnnotation title = new FixedTextAnnotation(estimParam.htmlName, 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            if (estimParam.shouldDisplayPrior()) {
                List<double[]> priorDensityData = estimParam.parameter.getPriorDensity();
                if (priorDensityData != null) {
                    int nData = priorDensityData.get(0).length;
                    PlotBand priorDensity = new PlotBand(
                            "",
                            priorDensityData.get(0),
                            priorDensityData.get(1),
                            Calc.zeroes(nData),
                            false,
                            AppSetup.COLORS.PRIOR_ENVELOP,
                            0.9f);
                    plot.addXYItem(priorDensity);
                    anyPriorCurve = true;
                }
            }

            double maxpost = estimParam.parameter.getMaxpost();
            PlotInfiniteLine maxpostLine = new PlotInfiniteLine(
                    "",
                    maxpost,
                    AppSetup.COLORS.POSTERIOR_LINE,
                    new BasicStroke(2f));

            PlotBar postDensity = new PlotBar(
                    "",
                    estimParam.parameter.mcmc,
                    20,
                    AppSetup.COLORS.POSTERIOR_ENVELOP,
                    0.7f);

            plot.addXYItem(postDensity);
            plot.addXYItem(maxpostLine);

            PlotContainer pc = new PlotContainer(plot, false);
            plotContainers.add(pc);
            T.updateHierarchy(this, pc);
            gridPanel.insertChild(pc, c, r);

            c++;
            if (c >= nColMax) {
                c = 0;
                r++;
            }
        }

        Legend legend = new Legend();

        PlotContainer pc = new PlotContainer(legend.getLegendPlot(), false);
        plotContainers.add(pc);
        T.updateHierarchy(this, pc);

        gridPanel.insertChild(pc, c, r);

        final boolean anyPriorCurveFinal = anyPriorCurve;
        T.t(this, () -> {
            legend.clearLegend();
            if (anyPriorCurveFinal) {
                legend.addLegendItem(PlotBand.buildLegendItem(
                        T.text("prior_density"),
                        AppSetup.COLORS.PRIOR_ENVELOP));
            }
            legend.addLegendItem(PlotBand.buildLegendItem(
                    T.text("posterior_density"),
                    AppSetup.COLORS.POSTERIOR_ENVELOP));
            legend.addLegendItem(PlotLine.buildLegendItem(
                    T.text("maxpost"),
                    AppSetup.COLORS.POSTERIOR_LINE,
                    new BasicStroke(3f)));

            legend.getLegendPlot().update();
        });

    }
}
