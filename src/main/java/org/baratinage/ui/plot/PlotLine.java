package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.BasicStroke;

import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class PlotLine extends PlotItem {

    protected DefaultXYDataset dataset;
    protected DefaultXYItemRenderer renderer;

    public PlotLine(String label, double[] x, double[] y, Paint paint, int lineWidth) {
        this(label, x, y, paint, lineWidth, null, 0);
    }

    public PlotLine(String label, double[] x, double[] y, Paint paint, int lineWidth, SHAPE shape, int shapeSize) {
        if (x.length != y.length)
            throw new IllegalArgumentException("x and y must have the same length!");

        dataset = new DefaultXYDataset();
        dataset.addSeries(label, new double[][] { x, y });
        renderer = new DefaultXYItemRenderer();

        if (shape == SHAPE.CIRCLE) {
            renderer.setSeriesShape(0, buildCircleShape(10));
        } else if (shape == SHAPE.SQUARE) {
            renderer.setSeriesShape(0, buildSquareShape(10));
        } else {
            renderer.setSeriesShape(0, buildCircleShape(0));
            renderer.setSeriesShapesVisible(0, false);
        }

        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, new BasicStroke(lineWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                1, new float[] { 1F }, 0));

    }

    @Override
    public DefaultXYDataset getDataset() {
        return dataset;
    }

    @Override
    public DefaultXYItemRenderer getRenderer() {
        return renderer;
    }

}
