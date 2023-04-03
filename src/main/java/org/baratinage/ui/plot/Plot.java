package org.baratinage.ui.plot;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;

public class Plot {

    private XYPlot plot;
    private JFreeChart chart;

    private NumberAxis axisX;
    private NumberAxis axisY;
    private ArrayList<PlotLine> lines;
    private ArrayList<PlotItem> items;

    public Plot(String xAxisLabel, String yAxisLabel, boolean includeLegend) {
        lines = new ArrayList<>();
        items = new ArrayList<>();

        axisX = new NumberAxis(xAxisLabel);
        axisX.setAutoRangeIncludesZero(false);
        axisY = new NumberAxis(yAxisLabel);

        axisY.setAutoRangeIncludesZero(false);

        plot = new XYPlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setDomainAxis(axisX);
        plot.setRangeAxis(axisY);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chart = new JFreeChart(plot);
        chart.setBackgroundPaint(Color.WHITE);
        chart.removeLegend();

        // Change legend location

        if (includeLegend) {

            LegendTitle legendTitle = new LegendTitle(plot);
            legendTitle.setBackgroundPaint(Color.WHITE);
            legendTitle.setPadding(5, 5, 5, 5);
            legendTitle.setFrame(new BlockBorder(0.25, 0.25, 0.25, 0.25, Color.BLACK));
            // legendTitle.setPosition(RectangleEdge.LEFT);
            legendTitle.setPosition(RectangleEdge.TOP);
            XYTitleAnnotation titleAnnot = new XYTitleAnnotation(
                    0.5, 1,
                    legendTitle,
                    RectangleAnchor.TOP);
            plot.addAnnotation(titleAnnot);
        }

    }

    public void setAxisLogX(boolean log) {
        if (log) {
            LogarithmicAxis newAxisX = new LogarithmicAxis(axisX.getLabel());
            newAxisX.setAutoRangeIncludesZero(false);
            newAxisX.setAllowNegativesFlag(true);
            axisX = newAxisX;
        } else {
            axisX = new NumberAxis(axisX.getLabel());
            axisX.setAutoRangeIncludesZero(false);
        }
        plot.setDomainAxis(axisX);
    }

    public void setAxisLogY(boolean log) {
        if (log) {
            LogarithmicAxis newAxisY = new LogarithmicAxis(axisY.getLabel());
            newAxisY.setAutoRangeIncludesZero(false);
            newAxisY.setAllowNegativesFlag(true);
            axisY = newAxisY;
        } else {
            axisY = new NumberAxis(axisY.getLabel());
            axisY.setAutoRangeIncludesZero(false);
        }
        plot.setRangeAxis(axisY);
    }

    public JFreeChart getChart() {
        return this.chart;
    }

    @Deprecated
    public void addLine(PlotLine line) {
        plot.setDataset(lines.size(), line.getDataset());
        plot.setRenderer(lines.size(), line.getRenderer());
        lines.add(line);
    }

    public void addXYItem(PlotItem item) {
        plot.setDataset(items.size(), item.getDataset());
        plot.setRenderer(items.size(), item.getRenderer());
        items.add(item);
    }
}
