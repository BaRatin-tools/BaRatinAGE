package org.baratinage.ui.plot;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.AbstractXYDataset;

public abstract class PlotItem {
    public static enum SHAPE {
        CIRCLE,
        SQUARE,
        NONE
    }

    // protected AbstractXYDataset dataset;
    // protected AbstractXYItemRenderer renderer;

    public abstract AbstractXYDataset getDataset();

    public abstract AbstractXYItemRenderer getRenderer();

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
