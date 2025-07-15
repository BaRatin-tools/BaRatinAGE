package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.AppSetup;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

public class Plot implements LegendItemSource {

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

    private boolean includeLegend;
    private boolean timeseries;

    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    public final List<PlotItem> items = new ArrayList<>();

    private LegendTitle currentLegend;

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
                    PlotItem item = items.get(index);
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

        updateLegend();
        updateFont();
    }

    private void updateFont() {
        Font font = new Font("SansSerif", Font.PLAIN, AppSetup.CONFIG.FONT_SIZE.get());

        axisX.setLabelFont(font);
        axisXlog.setLabelFont(font);
        axisY.setLabelFont(font);
        axisYlog.setLabelFont(font);
        axisXdate.setLabelFont(font);

        axisX.setTickLabelFont(font);
        axisXlog.setTickLabelFont(font);
        axisY.setTickLabelFont(font);
        axisYlog.setTickLabelFont(font);
        axisXdate.setTickLabelFont(font);

        LegendTitle legendTitle = chart.getLegend();
        if (legendTitle != null) {
            legendTitle.setItemFont(font);
        }
    }

    public void updateLegend() {
        updateLegend(RectangleEdge.RIGHT);
    }

    public void updateLegend(RectangleEdge position) {
        setLegend(this, position);
    }

    public void setLegend(LegendItemSource legendItemSource, RectangleEdge position) {
        chart.removeLegend();
        if (!includeLegend) {
            return;
        }
        currentLegend = new LegendTitle(legendItemSource);
        currentLegend.setPosition(RectangleEdge.RIGHT);
        currentLegend.setPadding(
                position == RectangleEdge.TOP ? 10 : 0,
                position == RectangleEdge.LEFT ? 10 : 0,
                position == RectangleEdge.BOTTOM ? 10 : 0,
                position == RectangleEdge.RIGHT ? 10 : 0);
        chart.addLegend(currentLegend);
        updateFont();
    }

    public boolean getIncludeLegend() {
        return this.includeLegend;
    }

    public void setIncludeLegend(boolean includeLegend) {
        chart.removeLegend();
        this.includeLegend = includeLegend;
        if (includeLegend && currentLegend != null) {
            chart.addLegend(currentLegend);
        }
    }

    public boolean getTimeseries() {
        return this.timeseries;
    }

    public void setTimeseries(boolean timeseries) {
        this.timeseries = timeseries;
        update();
    }

    public JFreeChart getChart() {
        return this.chart;
    }

    public void setXAxisLabel(String label) {
        axisX.setLabel(label);
        axisXlog.setLabel(label);
    }

    public void setYAxisLabel(String label) {
        axisY.setLabel(label);
        axisYlog.setLabel(label);
    }

    public void addXYItem(PlotItem item) {
        plot.setDataset(items.size(), item.getDataset());
        plot.setRenderer(items.size(), item.getRenderer());
        items.add(item);
    }

    public void addXYItem(PlotItemGroup item) {
        List<PlotItem> items = item.getPlotItems();
        addXYItems(items);
    }

    public void addXYItems(List<? extends PlotItem> items) {
        for (PlotItem i : items) {
            addXYItem(i);
        }
    }

    private int getPlotItemIndex(PlotItem item) {
        int index = -1;
        for (int k = 0; k < items.size(); k++) {
            if (items.get(k).equals(item)) {
                index = k;
                break;
            }
        }
        return index;
    }

    public int removeXYItem(PlotItem item) {
        int index = getPlotItemIndex(item);
        if (index < 0) {
            return -1;
        }
        items.remove(index);
        plot.setDataset(index, null);
        plot.setRenderer(index, null);
        return index;
    }

    public void reorderXYItems(List<PlotItem> plotItems) {
        List<PlotItem> existingPlotItems = new ArrayList<>();
        for (int k = 0; k < plotItems.size(); k++) {
            int index = getPlotItemIndex(plotItems.get(k));
            if (index >= 0) {
                existingPlotItems.add(items.get(index));
            }
        }
        int counter = 0;
        for (int k = 0; k < items.size(); k++) {
            int index = plotItems.indexOf(items.get(k));
            if (index < 0) {
                continue;
            }
            PlotItem pic = existingPlotItems.get(counter);
            items.set(k, pic);
            plot.setDataset(k, pic.getDataset());
            plot.setRenderer(k, pic.getRenderer());
            counter++;
        }
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
            PlotItem itemConfig = items.get(k);
            Range r = itemConfig.getDomainBounds();
            if (r != null) {
                range = range == null ? r : Range.combine(range, r);
            }
        }
        return applyBufferToRange(range, bufferPercentageLeft, bufferPercentageRight);
    }

    public Range getRangeBounds() {
        Range range = null;
        for (int k = 0; k < items.size(); k++) {
            PlotItem pi = items.get(k);
            Range r = pi.getRangeBounds();
            if (r != null) {
                range = range == null ? r : Range.combine(range, r);
            }
        }
        return applyBufferToRange(range, bufferPercentageBottom, bufferPercentageTop);
    }

    public Range getDomainZoom() {
        return plot.getDomainAxis().getRange();
    }

    public Range getRangeZoom() {
        return plot.getRangeAxis().getRange();
    }

    public void setDomainZoom(Range range) {
        plot.getDomainAxis().setRange(range);
    }

    public void setRangeZoom(Range range) {
        plot.getRangeAxis().setRange(range);
    }

    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection lic = new LegendItemCollection();
        for (PlotItem pi : items.reversed()) {
            if (pi.getLegendVisible()) {
                lic.add(pi.getLegendItem());
            }
        }
        return lic;
    }

    private static AbstractXYItemRenderer emptyRenderer = new AbstractXYItemRenderer() {

        @Override
        public void drawItem(Graphics2D arg0, XYItemRendererState arg1, Rectangle2D arg2, PlotRenderingInfo arg3,
                XYPlot arg4, ValueAxis arg5, ValueAxis arg6, XYDataset arg7, int arg8, int arg9,
                CrosshairState arg10, int arg11) {
            // do nothing
        }

    };

    public void update() {
        for (int k = 0; k < items.size(); k++) {
            PlotItem plotItem = items.get(k);
            plot.setDataset(k, plotItem.getDataset());
            plot.setRenderer(k, plotItem.getVisible() ? plotItem.getRenderer() : emptyRenderer);
        }
        chart.fireChartChanged();
        updateFont();
    }

    public void setBufferPercentage(double top, double bottom, double left, double right) {
        bufferPercentageTop = top;
        bufferPercentageBottom = bottom;
        bufferPercentageLeft = left;
        bufferPercentageRight = right;
    }

    public Plot getCopy() {
        Plot plotCopy = new Plot(includeLegend, timeseries);
        // axis labels
        plotCopy.axisX.setLabel(axisX.getLabel());
        plotCopy.axisXlog.setLabel(axisXlog.getLabel());
        plotCopy.axisY.setLabel(axisY.getLabel());
        plotCopy.axisYlog.setLabel(axisYlog.getLabel());
        // x axis / domain axis: log and visibility
        ValueAxis domaineAxis = plot.getDomainAxis();
        if (domaineAxis instanceof LogAxis) {
            plotCopy.plot.setDomainAxis(plotCopy.axisXlog);
        }
        if (domaineAxis != null && plotCopy.plot.getDomainAxis() != null) {
            plotCopy.plot.getDomainAxis().setVisible(domaineAxis.isVisible());
        }
        // y axis / range axis: log and visibility
        ValueAxis rangeAxis = plot.getRangeAxis();
        if (rangeAxis instanceof LogAxis) {
            plotCopy.plot.setRangeAxis(plotCopy.axisYlog);
        }
        if (rangeAxis != null && plotCopy.plot.getRangeAxis() != null) {
            plotCopy.plot.getRangeAxis().setVisible(rangeAxis.isVisible());
        }
        // plot items
        for (PlotItem item : items) {
            plotCopy.addXYItem(item.getCopy());
        }
        // plot legend
        plotCopy.chart.removeLegend();
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            plotCopy.chart.addLegend(legend);
        }
        plotCopy.update();
        return plotCopy;
    }

}
