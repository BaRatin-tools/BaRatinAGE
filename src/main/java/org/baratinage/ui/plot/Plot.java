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

    public final XYPlot plot;
    public final JFreeChart chart;

    public final NumberAxis axisX;
    public final NumberAxis axisY;
    public final LogAxis axisYlog;
    public final DateAxis axisXdate;

    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private record PlotItemConfig(PlotItem item, boolean visibleInLegend, boolean useBounds) {
    };

    public final List<PlotItemConfig> items = new ArrayList<>();

    // private List<Integer> ignoreInRangeAndDomainIndices = new ArrayList<>();

    public Plot() {
        this(true, false);
    }

    public Plot(boolean includeLegend) {
        this(includeLegend, false);
    }

    public Plot(boolean includeLegend, boolean timeseries) {

        axisX = new NumberAxis();
        axisX.setAutoRangeIncludesZero(false);
        axisY = new NumberAxis();
        axisY.setAutoRangeIncludesZero(false);
        axisYlog = new LogAxis();
        axisYlog.setAutoRangeIncludesZero(false);
        axisYlog.setAutoRange(true);
        axisYlog.setAutoRangeIncludesZero(false);
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

    public void setAxisLogY(boolean log) {
        if (log) {
            plot.setRangeAxis(axisYlog);
        } else {
            plot.setRangeAxis(axisY);
        }
    }

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

    public static Range bufferRange(Range range, double p) {
        double d = range.getLength();
        double buffer = d * p;
        return new Range(range.getLowerBound() - buffer, range.getUpperBound() + buffer);
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
        return bufferRange(range, 0.01);
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
        return bufferRange(range, 0.01);
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
}
