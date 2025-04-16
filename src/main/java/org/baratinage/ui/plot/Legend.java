package org.baratinage.ui.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;

public class Legend implements LegendItemSource {

    // private LegendItemCollection legendItems = new LegendItemCollection();
    private final List<LegendItem> legendItems = new ArrayList<>();
    private Plot plot;

    public void addLegendItem(LegendItem legendItem) {
        legendItems.add(legendItem);
    }

    public void addLegendTitleItem(String label) {
        LegendItem item = new LegendItem(label);
        item.setShape(PlotItem.buildEmptyShape());
        addLegendItem(item);
    }

    public void clearLegend() {
        legendItems.clear();
    }

    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection legends = new LegendItemCollection();
        for (LegendItem leg : legendItems) {
            legends.add(leg);
        }
        return legends;
    }

    public LegendTitle getLegendTitle() {
        return getLegendTitle(false);
    }

    public LegendTitle getLegendTitle(boolean vertical) {
        LegendTitle legendTitle;
        if (vertical) {
            legendTitle = new LegendTitle(this);
        } else {
            FlowArrangement arrangement = new FlowArrangement();
            legendTitle = new LegendTitle(this, arrangement, arrangement);
        }
        return legendTitle;
    }

    public LegendTitle getLegendTitle(RectangleEdge position, boolean vertical) {
        LegendTitle legendTitle = getLegendTitle(vertical);
        legendTitle.setPosition(position);
        return legendTitle;
    }

    public XYAnnotation getAnnotation(double x, double y, RectangleAnchor anchor) {
        return new XYTitleAnnotation(x, y, getLegendTitle(), anchor);
    }

    public Plot getLegendPlot() {
        if (plot != null) {
            return plot;
        }
        plot = new Plot(false, false);
        plot.axisX.setVisible(false);
        plot.axisY.setVisible(false);
        plot.plot.setOutlinePaint(null);
        plot.plot.addAnnotation(getAnnotation(0.5, 0.5, RectangleAnchor.CENTER));
        return plot;
    }

}
