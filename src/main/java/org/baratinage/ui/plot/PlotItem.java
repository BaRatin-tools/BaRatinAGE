package org.baratinage.ui.plot;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.AbstractXYDataset;

public abstract class PlotItem {

    public abstract AbstractXYDataset getDataset();

    public abstract AbstractXYItemRenderer getRenderer();

    public abstract LegendItem getLegendItem();

    public abstract void setLabel(String label);

    public Range getDomainBounds() {
        return DatasetUtils.findDomainBounds(getDataset());
    }

    public Range getRangeBounds() {
        return DatasetUtils.findRangeBounds(getDataset());
    }

    public void setPlot(XYPlot plot) {
    }

    public static Ellipse2D.Double buildCircleShape() {
        return buildCircleShape(10);
    }

    public static Ellipse2D.Double buildCircleShape(int size) {
        Ellipse2D.Double shape = new Ellipse2D.Double();

        shape.width = size;
        shape.height = size;
        shape.x = -size / 2;
        shape.y = -size / 2;
        return shape;

    }

    public static Rectangle buildSquareShape() {
        return buildSquareShape(10);
    }

    public static Rectangle buildSquareShape(int size) {
        Rectangle shape = new Rectangle(
                -size / 2,
                -size / 2,
                size, size);
        return shape;

    }

    public static Line2D buildLineShape() {
        return buildLineShape(7);
    }

    public static Line2D buildLineShape(int size) {
        double s = (double) size;
        return new Line2D.Double(-s, 0.0, s, 0.0);
    }

    public static Shape buildEmptyShape() {
        return buildLineShape(0);
    }

    public static Stroke buildEmptyStroke() {
        return buildStroke(0);
    }

    public static Stroke buildStroke() {
        return buildStroke(1);
    }

    public static Stroke buildStroke(int lineWidth) {
        return new BasicStroke(lineWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                1, new float[] { 1F }, 0);
    }
}
