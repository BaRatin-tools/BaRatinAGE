package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.plot.PlotItem.ShapeType;

public class PointHighlight implements PlotItemGroup {

    private final PlotPoints point;
    private final PlotInfiniteLine hLine;
    private final PlotInfiniteLine vLine;

    private Paint paint;
    private int lineWidth;
    private int shapeSize;

    public PointHighlight(int lineWidth, int shapeSize, Paint paint) {
        point = new PlotPoints("", new double[] {}, new double[] {}, paint,
                PlotItem.buildCircleShape(shapeSize));
        hLine = new PlotInfiniteLine("", Double.NaN, Double.NaN, paint,
                PlotItem.buildStroke(lineWidth));
        vLine = new PlotInfiniteLine("", Double.NaN, paint, PlotItem.buildStroke(lineWidth));

        point.setLegendVisible(false);
        hLine.setLegendVisible(false);
        vLine.setLegendVisible(false);

        this.paint = paint;
        this.lineWidth = lineWidth;
        this.shapeSize = shapeSize;
    }

    public void setPosition(double x, double y) {
        double[] xArr = new double[] { x };
        double[] yArr = new double[] { y };
        point.updateDataset(xArr, xArr, xArr, yArr, yArr, yArr);
        hLine.updateDataset(0, y);
        vLine.updateDataset(x);
    }

    public void setVisible(boolean visible) {
        RendererSetting rendererSettings = new RendererSetting();
        rendererSettings.visible = visible;
        vLine.configureRenderer(rendererSettings);
        hLine.configureRenderer(rendererSettings);
        point.configureRenderer(rendererSettings);
    }

    @Override
    public List<PlotItem> getPlotItems() {
        List<PlotItem> items = new ArrayList<>();
        items.add(point);
        items.add(hLine);
        items.add(vLine);
        return items;
    }

    private class RendererSetting implements IPlotItemRendererSettings {

        public boolean visible;

        private static Color TRANSPARENT = new Color(0, 0, 0, 0);

        @Override
        public Paint getLinePaint() {
            return visible ? paint : TRANSPARENT;
        }

        @Override
        public float getLineWidth() {
            return visible ? lineWidth : 0;
        }

        @Override
        public float[] getLineDashArray() {
            return new float[] { 1F };
        }

        @Override
        public int getShapeSize() {
            return visible ? shapeSize : 0;
        }

        @Override
        public ShapeType getShapeType() {
            return PlotItem.ShapeType.CIRCLE;
        }

        @Override
        public Paint getFillPaint() {
            return visible ? paint : TRANSPARENT;
        }

        @Override
        public float getFillAlpha() {
            return 1;
        }

    }

}
