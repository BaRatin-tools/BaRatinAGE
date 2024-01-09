package org.baratinage.ui.plot;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

public class Plot implements LegendItemSource {

    // FIXME: useBounds never set to false
    private static record PlotItemConfig(PlotItem item, boolean visibleInLegend, boolean useBounds) {
    };

    public final XYPlot plot;
    public final JFreeChart chart;

    public final NumberAxis axisX;
    public final NumberAxis axisY;
    public final LogAxis axisXlog;
    public final LogAxis axisYlog;
    public final DateAxis axisXdate;

    private double bufferPercentageTop = 0.01;
    private double bufferPercentageBottom = 0.01;
    private double bufferPercentageLeft = 0.01;
    private double bufferPercentageRight = 0.01;

    private final boolean includeLegend;
    private final boolean timeseries;

    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    public final List<PlotItemConfig> items = new ArrayList<>();

    public Plot() {
        this(true, false);
    }

    public Plot(boolean includeLegend) {
        this(includeLegend, false);
    }

    public Plot(boolean includeLegend, boolean timeseries) {

        this.includeLegend = includeLegend;
        this.timeseries = timeseries;

        axisX = new NumberAxis();
        axisX.setAutoRangeIncludesZero(false);

        axisXlog = new LogAxis();
        axisXlog.setAutoRangeIncludesZero(false);
        axisXlog.setAutoRange(true);
        axisXlog.setAllowNegativesFlag(true);

        axisY = new NumberAxis();
        axisY.setAutoRangeIncludesZero(false);

        axisYlog = new LogAxis();
        axisYlog.setAutoRangeIncludesZero(false);
        axisYlog.setAutoRange(true);
        axisYlog.setAllowNegativesFlag(true);

        axisXdate = new DateAxis();
        axisXdate.setDateFormatOverride(new SimpleDateFormat(dateTimeFormat));

        plot = new XYPlot() {
            @Override
            public XYDataset getDataset(int index) {
                if (index < items.size()) {
                    PlotItem item = items.get(index).item;
                    item.setPlot(this);
                    return item.getDataset();
                } else {
                    return super.getDataset(index);
                }
            }
        };

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setDomainAxis(timeseries ? axisXdate : axisX);
        plot.setRangeAxis(axisY);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chart = new JFreeChart(plot);
        chart.setBackgroundPaint(Color.WHITE);
        chart.removeLegend();

        if (includeLegend) {
            LegendTitle legendTitle = new LegendTitle(this);
            legendTitle.setPosition(RectangleEdge.RIGHT);
            chart.addLegend(legendTitle);
        }

    }

    // public void setAxisLogY(boolean log) {
    // if (log) {
    // plot.setRangeAxis(axisYlog);
    // } else {
    // plot.setRangeAxis(axisY);
    // }
    // }

    public JFreeChart getChart() {
        return this.chart;
    }

    public void addXYItem(PlotItem item) {
        addXYItem(item, true);
    }

    public void addXYItem(PlotItem item, boolean isVisibleInLegend) {
        plot.setDataset(items.size(), item.getDataset());
        plot.setRenderer(items.size(), item.getRenderer());
        items.add(new PlotItemConfig(item, isVisibleInLegend, true));
    }

    private static Range applyBufferToRange(Range r, double lowerBufferPercentage, double upperBufferPercentage) {
        if (r == null) {
            return r;
        }
        double d = r.getLength();
        double lower = d * lowerBufferPercentage;
        double upper = d * upperBufferPercentage;
        return new Range(r.getLowerBound() - lower, r.getUpperBound() + upper);
    }

    public Range getDomainBounds() {
        Range range = null;
        for (int k = 0; k < items.size(); k++) {
            PlotItemConfig itemConfig = items.get(k);
            if (itemConfig.useBounds) {
                Range r = itemConfig.item.getDomainBounds();
                if (r != null) {
                    range = range == null ? r : Range.combine(range, r);
                }
            }
        }
        return applyBufferToRange(range, bufferPercentageLeft, bufferPercentageRight);
    }

    public Range getRangeBounds() {
        Range range = null;
        for (int k = 0; k < items.size(); k++) {
            PlotItemConfig itemConfig = items.get(k);
            if (itemConfig.useBounds) {
                Range r = itemConfig.item.getRangeBounds();
                if (r != null) {
                    range = range == null ? r : Range.combine(range, r);
                }
            }
        }
        return applyBufferToRange(range, bufferPercentageBottom, bufferPercentageTop);
    }

    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection lic = new LegendItemCollection();
        for (PlotItemConfig pic : items) {
            if (pic.visibleInLegend) {
                lic.add(pic.item.getLegendItem());
            }
        }
        return lic;
    }

    public void update() {
        chart.fireChartChanged();
    }

    public void setBufferPercentage(double top, double bottom, double left, double right) {
        bufferPercentageTop = top;
        bufferPercentageBottom = bottom;
        bufferPercentageLeft = left;
        bufferPercentageRight = right;
    }

    public Plot getCopy() {
        Plot plotCopy = new Plot(includeLegend, timeseries);
        for (PlotItemConfig item : items) {
            plotCopy.addXYItem(item.item(), item.visibleInLegend());
        }
        return plotCopy;
    }

}
