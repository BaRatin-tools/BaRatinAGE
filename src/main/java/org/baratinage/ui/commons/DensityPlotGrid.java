package org.baratinage.ui.commons;

import java.awt.BasicStroke;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.translation.T;

public class DensityPlotGrid extends RowColPanel {

    private final List<EstimatedParameter> estimatedParameters = new ArrayList<>();

    public void addPlot(EstimatedParameter estimatedParameter) {
        estimatedParameters.add(estimatedParameter);
    }

    public void clearPlots() {
        estimatedParameters.clear();
    }

    public void updatePlot() {
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
        for (int k = 0; k < estimatedParameters.size(); k++) {

            EstimatedParameter estimParam = estimatedParameters.get(k);

            Plot plot = new Plot(false, false);

            FixedTextAnnotation title = new FixedTextAnnotation(estimParam.name, 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0.05, 0.05);

            List<double[]> priorDensityData = estimParam.getPriorDensity();

            if (priorDensityData != null) {
                int nData = priorDensityData.get(0).length;
                PlotBand priorDensity = new PlotBand(
                        "",
                        priorDensityData.get(0),
                        priorDensityData.get(1),
                        DensityData.buildZeroArray(nData),
                        AppConfig.AC.PRIOR_ENVELOP_COLOR,
                        0.9f);
                plot.addXYItem(priorDensity);

            }

            DensityData plotItem = new DensityData(estimParam.mcmc);
            plotItem.update(50, 5);
            List<double[]> lineData = plotItem.getLineData();

            double maxpost = estimParam.mcmc[estimParam.maxpostIndex];
            PlotInfiniteLine maxpostLine = new PlotInfiniteLine(
                    "",
                    maxpost,
                    AppConfig.AC.POSTERIOR_LINE_COLOR,
                    new BasicStroke(2f));

            PlotBand postDensity = new PlotBand(
                    "",
                    lineData.get(0),
                    lineData.get(1),
                    lineData.get(2),
                    AppConfig.AC.POSTERIOR_ENVELOP_COLOR,
                    0.7f);

            plot.addXYItem(postDensity);
            plot.addXYItem(maxpostLine);

            PlotContainer pc = new PlotContainer(plot, false);
            gridPanel.insertChild(pc, c, r);

            c++;
            if (c >= nColMax) {
                c = 0;
                r++;
            }
        }

        Legend legend = new Legend();

        PlotContainer pc = new PlotContainer(legend.getLegendPlot(), false);
        gridPanel.insertChild(pc, c, r);

        T.t(legend, (lgd) -> {
            lgd.clearLegend();

            lgd.addLegendItem(PlotBand.buildLegendItem(
                    T.text("prior_density"),
                    AppConfig.AC.PRIOR_ENVELOP_COLOR));
            lgd.addLegendItem(PlotBand.buildLegendItem(
                    T.text("posterior_density"),
                    AppConfig.AC.POSTERIOR_ENVELOP_COLOR));
            lgd.addLegendItem(PlotLine.buildLegendItem(
                    T.text("maxpost"),
                    AppConfig.AC.POSTERIOR_LINE_COLOR,
                    new BasicStroke(3f)));

            lgd.getLegendPlot().update();
        });

    }
}
