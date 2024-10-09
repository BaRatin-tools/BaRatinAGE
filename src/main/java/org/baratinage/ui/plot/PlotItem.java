package org.baratinage.ui.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;

public abstract class PlotItem {

    public abstract XYDataset getDataset();

    public abstract XYItemRenderer getRenderer();

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

    public static Second[] localDateTimeToSecond(LocalDateTime[] time) {
        int n = time.length;
        Second[] s = new Second[n];
        for (int k = 0; k < n; k++) {
            Date d = new Date(time[k].toEpochSecond(ZoneOffset.UTC) * 1000);
            s[k] = new Second(d);
        }
        return s;
    }

    protected static double[] secondToDouble(Second[] seconds) {
        int n = seconds.length;
        double[] d = new double[n];
        for (int k = 0; k < n; k++) {
            d[k] = seconds[k].getMiddleMillisecond();
        }
        return d;
    }

    public static LegendItem buildLegendItem(String label, Paint linePaint, Stroke lineStroke, Shape shape,
            Paint shapePaint) {
        boolean showLine = true;
        boolean showShape = true;
        if (shapePaint == null) {
            shapePaint = linePaint;
            showShape = false;
        }
        if (shape == null) {
            shape = buildEmptyShape();
            showShape = false;
        }
        if (linePaint == null) {
            linePaint = Color.BLACK;
            showLine = false;
        }
        if (lineStroke == null) {
            lineStroke = new BasicStroke();
            showLine = false;
        }
        Shape lineShape = showLine ? buildLineShape(7) : buildEmptyShape();
        return new LegendItem(
                label,
                null,
                label,
                null,
                showShape,
                shape,
                showShape,
                shapePaint,
                false,
                shapePaint,
                lineStroke,
                showLine,
                lineShape,
                lineStroke,
                linePaint);
    }
}
