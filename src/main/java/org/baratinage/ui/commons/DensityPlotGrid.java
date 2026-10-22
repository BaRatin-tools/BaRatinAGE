package org.baratinage.ui.commons;

import java.awt.BasicStroke;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.ResultColumn;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.GridPlotContainer;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.Calc;
import org.baratinage.translation.T;

public class DensityPlotGrid extends SimpleFlowPanel {

    private GridPlotContainer gridPanel;
    public final List<ResultColumn> parameters = new ArrayList<>();

    public boolean isTimeSeries = false;

    public void addPlot(ResultColumn p) {
        parameters.add(p);
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
        T.updateHierarchy(this, gridPanel);

        removeAll();
        addChild(gridPanel, true);

        boolean anyPriorCurve = false;

        for (int k = 0; k < parameters.size(); k++) {

            ResultColumn p = parameters.get(k);

            Plot plot = new Plot(false, isTimeSeries);

            FixedTextAnnotation title = new FixedTextAnnotation(p.symbol.toHtml(), 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            if (p.shouldDisplayPrior()) {
                List<double[]> priorDensityData = p.getPriorDensity();
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

            double maxpost = p.getMaxpost();
            PlotInfiniteLine maxpostLine = new PlotInfiniteLine(
                    "",
                    maxpost,
                    AppSetup.COLORS.POSTERIOR_LINE,
                    new BasicStroke(2f));

            PlotBar postDensity = new PlotBar(
                    "",
                    p.samples,
                    20,
                    AppSetup.COLORS.POSTERIOR_ENVELOP,
                    0.7f);

            if (p.parameter != null) {
                if (p.parameter.distribution.type == DistributionType.FIXED) {
                    plot.plot.getRangeAxis().setVisible(false);
                }
            }

            plot.addXYItem(postDensity);
            plot.addXYItem(maxpostLine);

            gridPanel.addPlot(plot);
        }

        Legend legend = new Legend();

        gridPanel.addPlot(legend.getLegendPlot());

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

    public IExportablePlot getPlot() {
        return gridPanel;
    }
}
