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
import org.jfree.data.xy.XYDataset;

public class Plot {

    public final XYPlot plot;
    private JFreeChart chart;

    private NumberAxis axisX;
    private NumberAxis axisY;

    private List<PlotItem> items = new ArrayList<>();
    private List<Integer> ignoreInRangeAndDomainIndices = new ArrayList<>();

    public Plot(String xAxisLabel, String yAxisLabel, boolean includeLegend) {

        axisX = new NumberAxis(xAxisLabel);
        axisX.setAutoRangeIncludesZero(false);
        axisY = new NumberAxis(yAxisLabel);
        axisY.setAutoRangeIncludesZero(false);

        plot = new XYPlot() {
            @Override
            public XYDataset getDataset(int index) {
                if (index < items.size()) {
                    PlotItem item = items.get(index);
                    item.setPlot(this);
                    return item.getDataset();
                } else {
                    return super.getDataset(index);
                }
            }
        };

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
            legendTitle.setPosition(RectangleEdge.TOP);
            XYTitleAnnotation titleAnnot = new XYTitleAnnotation(
                    0.5, 1,
                    legendTitle,
                    RectangleAnchor.TOP);
            plot.addAnnotation(titleAnnot);
        }

    }

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

    public void addXYItem(PlotItem item) {
        plot.setDataset(items.size(), item.getDataset());
        plot.setRenderer(items.size(), item.getRenderer());
        items.add(item);
    }

    public void addXYItem(PlotItem item, boolean isVisibleInLegend) {
        addXYItem(item);
        item.getRenderer().setSeriesVisibleInLegend(0, isVisibleInLegend);

    }

    public void addXYItem(PlotItem item, boolean isVisibleInLegend, boolean ignoreInRangeAndDomain) {
        addXYItem(item, isVisibleInLegend);
        if (ignoreInRangeAndDomain) {
            ignoreInRangeAndDomainIndices.add(items.size() - 1);
        }
    }

    public Range getDomainBounds() {
        Range range = null;
        for (int k = 0; k < items.size(); k++) {
            PlotItem item = items.get(k);
            if (!ignoreInRangeAndDomainIndices.contains(k)) {
                Range r = item.getDomainBounds();
                if (r != null) {
                    range = range == null ? r : Range.combine(range, r);
                }
            }
        }
        return range;
    }

    public Range getRangeBounds() {
        Range range = null;
        for (int k = 0; k < items.size(); k++) {
            PlotItem item = items.get(k);
            if (!ignoreInRangeAndDomainIndices.contains(k)) {
                Range r = item.getRangeBounds();
                if (r != null) {
                    range = range == null ? r : Range.combine(range, r);
                }
            }
        }
        return range;
    }
}
