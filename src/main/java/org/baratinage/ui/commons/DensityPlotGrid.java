package org.baratinage.ui.commons;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.FixedTextAnnotation;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.lg.Lg;

public class DensityPlotGrid extends RowColPanel {

    private record NamedVector(String name, double[] values) {
    };

    private final List<NamedVector> namedVectors = new ArrayList<>();

    private final List<PlotContainer> plotContainers = new ArrayList<>();

    public DensityPlotGrid() {

    }

    public void addPlot(String name, double[] values) {
        namedVectors.add(new NamedVector(name, values));
    }

    public void clearPlots() {
        namedVectors.clear();
    }

    public void updatePlot() {
        GridPanel gridPanel = new GridPanel();
        int nColMax = 4;
        int nPlots = namedVectors.size();
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
        for (PlotContainer pc : plotContainers) {
            Lg.unregister(pc);
        }
        plotContainers.clear();

        int r = 0;
        int c = 0;
        for (int k = 0; k < namedVectors.size(); k++) {

            NamedVector v = namedVectors.get(k);

            Plot plot = new Plot(false, false);

            FixedTextAnnotation title = new FixedTextAnnotation(v.name, 5, 5);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            plot.plot.addAnnotation(title);

            plot.setBufferPercentage(0.1, 0, 0, 0);

            DensityPlotItem plotItem = new DensityPlotItem(v.values);
            plotItem.update(50, 5);
            PlotLine line = plotItem.getPlotLine();
            plot.addXYItem(line);
            plot.addXYItem(plotItem.getPlotBand());

            PlotContainer plotContainer = new PlotContainer(plot, false);

            gridPanel.insertChild(plotContainer, c, r);

            c++;
            if (c >= nColMax) {
                c = 0;
                r++;
            }
        }

        Legend legend = new Legend();
        legend.addLegendItem(
                PlotItem.buildLegendItem(
                        "Posterior density",
                        null, null,
                        PlotItem.buildSquareShape(10),
                        AppConfig.AC.DENSITY_ENVELOP_COLOR));
        legend.addLegendItem(
                PlotItem.buildLegendItem(
                        "Prior density",
                        null, null,
                        PlotItem.buildSquareShape(10),
                        Color.RED));

        PlotContainer pc = new PlotContainer(legend.getLegendPlot(), false);

        gridPanel.insertChild(pc, c, r);
    }
}
