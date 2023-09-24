package org.baratinage.ui.plot;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;

public class Legend implements LegendItemSource {

    private LegendItemCollection legendItems = new LegendItemCollection();

    public void addLegendItem(LegendItem legendItem) {
        legendItems.add(legendItem);
    }

    @Override
    public LegendItemCollection getLegendItems() {
        return legendItems;
    }

    public LegendTitle getLegendTitle() {
        FlowArrangement arrangement = new FlowArrangement();
        LegendTitle legendTitle = new LegendTitle(this, arrangement, arrangement);
        return legendTitle;
    }

    public XYAnnotation getAnnotation(double x, double y, RectangleAnchor anchor) {
        return new XYTitleAnnotation(x, y, getLegendTitle(), anchor);
    }

    public Plot getLegendPlot() {
        Plot p = new Plot(false, false);
        p.axisX.setVisible(false);
        p.axisY.setVisible(false);
        p.plot.setOutlinePaint(null);
        p.plot.addAnnotation(getAnnotation(0.5, 0.5, RectangleAnchor.CENTER));
        return p;
    }

}
