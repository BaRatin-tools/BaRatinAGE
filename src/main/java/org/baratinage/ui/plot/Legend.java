package org.baratinage.ui.plot;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.AppSetup;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.Size2D;

public class Legend implements LegendItemSource {

    // private LegendItemCollection legendItems = new LegendItemCollection();
    private final List<LegendItem> legendItems = new ArrayList<>();
    private Plot plot;
    private LegendTitle legendTitle;

    public Legend() {
    }

    public Legend(LegendItemCollection legends) {
        for (int k = 0; k < legends.getItemCount(); k++) {
            legendItems.add(legends.get(k));
        }
    }

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

    public void update() {
        if (legendTitle == null) {
            return;
        }
        Font font = new Font("SansSerif", Font.PLAIN, AppSetup.CONFIG.FONT_SIZE.get());
        legendTitle.setItemFont(font);
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
        return getLegendTitle(true);
    }

    public LegendTitle getLegendTitle(boolean vertical) {
        return getLegendTitle(RectangleEdge.RIGHT, vertical);
    }

    public LegendTitle getLegendTitle(RectangleEdge position, boolean vertical) {
        if (vertical) {
            legendTitle = new LegendTitle(this);
        } else {
            FlowArrangement arrangement = new FlowArrangement();
            legendTitle = new LegendTitle(this, arrangement, arrangement);
        }
        legendTitle.setPosition(position);
        update();
        return legendTitle;
    }

    public Plot getLegendPlot() {
        if (plot != null) {
            return plot;
        }
        return getLegendPlot(getLegendTitle());
    }

    public static Plot getLegendPlot(LegendTitle legendTitle) {
        Plot plot = new Plot(false, false);
        plot.axisX.setVisible(false);
        plot.axisY.setVisible(false);
        plot.plot.setOutlinePaint(null);
        plot.plot.addAnnotation(new XYTitleAnnotation(0.5, 0.5, legendTitle, RectangleAnchor.CENTER));
        return plot;
    }

    public static Dimension getSize(LegendTitle legendTitle) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Size2D size = legendTitle.arrange(g2);
        return new Dimension((int) size.width + 50, (int) size.height + 50);
    }

}
