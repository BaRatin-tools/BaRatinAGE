package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

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

    // FIXME: (re) enable when needed
    // public void setAxisLogX(boolean log) {
    // if (log) {
    // LogarithmicAxis newAxisX = new LogarithmicAxis(axisX.getLabel());
    // newAxisX.setAutoRangeIncludesZero(false);
    // newAxisX.setAllowNegativesFlag(true);
    // axisX = newAxisX;
    // } else {
    // axisX = new NumberAxis(axisX.getLabel());
    // axisX.setAutoRangeIncludesZero(false);
    // }
    // plot.setDomainAxis(axisX);
    // }

    public void setAxisLogY(boolean log) {
        if (log) {
            // FIXME: make an actual class from this draft
            // for a better handling of log axis tick values
            LogarithmicAxis newAxisY = new LogarithmicAxis(axisY.getLabel()) {

                @Override
                protected List<NumberTick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
                        RectangleEdge edge) {

                    Range range = getRange();
                    int n = (int) Math.floor(Math.log10(range.getLength()));

                    if (n <= 2) {
                        double lowerBound = range.getLowerBound();
                        double upperBound = range.getUpperBound();
                        double boundRange = range.getLength();
                        double val = boundRange;
                        for (int offset = 0; offset < 2; offset++) {
                            double step = Math.pow(10, Math.floor(Math.log10(val)) - offset);
                            String template = "%." + (Math.max(0, (n - offset) * -1)) + "f";
                            List<NumberTick> arr = new ArrayList<>();
                            for (Double k = step; k < upperBound; k += step) {
                                if (k >= lowerBound) {
                                    arr.add(new NumberTick(k, String.format(template, k),
                                            TextAnchor.CENTER_RIGHT,
                                            TextAnchor.CENTER_RIGHT,
                                            0));
                                }
                            }
                            if (arr.size() >= 3) {
                                return arr;
                            }
                        }
                    }

                    @SuppressWarnings("unchecked")
                    List<NumberTick> res = super.refreshTicksVertical(g2, dataArea, edge);
                    return res;
                }
            };
            newAxisY.setAutoRange(true);
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
