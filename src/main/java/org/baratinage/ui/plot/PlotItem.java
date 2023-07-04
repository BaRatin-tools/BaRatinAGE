package org.baratinage.ui.plot;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.AbstractXYDataset;

public abstract class PlotItem {
    public static enum SHAPE {
        CIRCLE,
        SQUARE,
        NONE
    }

    public abstract AbstractXYDataset getDataset();

    public abstract AbstractXYItemRenderer getRenderer();

    public Range getDomainBounds() {
        return DatasetUtils.findDomainBounds(getDataset());
    }

    public Range getRangeBounds() {
        return DatasetUtils.findRangeBounds(getDataset());
    }

    public void setPlot(XYPlot plot) {
    }

    protected static Ellipse2D.Double buildCircleShape(int size) {
        Ellipse2D.Double shape = new Ellipse2D.Double();

        shape.width = size;
        shape.height = size;
        shape.x = -size / 2;
        shape.y = -size / 2;
        return shape;

    }

    protected static Rectangle buildSquareShape(int size) {
        Rectangle shape = new Rectangle(
                -size / 2,
                -size / 2,
                size, size);
        return shape;

    }

}
