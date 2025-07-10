package org.baratinage.ui.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

public abstract class PlotItem {

    private boolean visible = true;
    private boolean legend = true;
    private String label = "";

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setLegendVisible(boolean visible) {
        legend = visible;
    }

    public boolean getLegendVisible() {
        return legend;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract XYDataset getDataset();

    public abstract XYItemRenderer getRenderer();

    public abstract LegendItem getLegendItem();

    public abstract void configureRenderer(IPlotItemRendererSettings rendererSettings);

    public abstract PlotItem getCopy();

    public Range getDomainBounds() {
        return DatasetUtils.findDomainBounds(getDataset());
    }

    public Range getRangeBounds() {
        return DatasetUtils.findRangeBounds(getDataset());
    }

    public void setPlot(XYPlot plot) {
    }

    public static enum ShapeType {
        CIRCLE, SQUARE;

        public static ShapeType fromString(String shapeTypeString) {
            if (shapeTypeString.equals("CIRCLE"))
                return CIRCLE;
            if (shapeTypeString.equals("SQUARE"))
                return SQUARE;
            return CIRCLE;
        }

        public String toString() {
            if (this == CIRCLE)
                return "CIRCLE";
            if (this == SQUARE)
                return "SQUARE";
            return "CIRCLE";
        }

        public Shape getShape(int size) {
            if (this == CIRCLE) {
                return buildCircleShape(size);
            } else if (this == SQUARE) {
                return buildSquareShape(size);
            } else {
                return buildEmptyShape();
            }
        }

    }

    public static enum LineType {
        SOLID, DASHED, DOTTED;

        public static LineType fromString(String lineTypeString) {
            if (lineTypeString.equals("SOLID"))
                return SOLID;
            if (lineTypeString.equals("DASHED"))
                return DASHED;
            if (lineTypeString.equals("DOTTED"))
                return DOTTED;
            return SOLID;
        }

        public String toString() {
            if (this == SOLID)
                return "SOLID";
            if (this == DASHED)
                return "DASHED";
            if (this == DOTTED)
                return "DOTTED";
            return "SOLID";
        }

        public float[] getDashArray() {
            return getDashArray(1F);
        }

        public float[] getDashArray(float lineWidth) {
            if (this == SOLID) {
                return new float[] { 1F };
            } else if (this == DASHED) {
                return new float[] { lineWidth * 2F, lineWidth * 2F };
            } else if (this == DOTTED) {
                return new float[] { 1F, lineWidth * 1.5F };
            }
            return new float[] { 1F };
        }

        public static LineType getLineTypeFromStroke(Stroke stroke) {
            if (!(stroke instanceof BasicStroke)) {
                return SOLID;
            }
            BasicStroke basicStroke = (BasicStroke) stroke;
            float[] dashArray = basicStroke.getDashArray();
            if (dashArray.length > 1) {
                float refDash = dashArray[0];
                if (refDash > 3F) {
                    return DASHED;
                }
                return DOTTED;
            }
            return SOLID;
        }
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

    public static Stroke buildStroke(float lineWidth) {
        return buildStroke(lineWidth, new float[] { 1F });
    }

    public static Stroke buildStroke(float lineWidth, float[] dashArray) {
        return new BasicStroke(lineWidth,
                // BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,
                1, dashArray, 0);
    }

    public static LegendItem buildLegendItem(
            String label,
            Paint linePaint,
            Stroke lineStroke,
            Shape shape,
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
