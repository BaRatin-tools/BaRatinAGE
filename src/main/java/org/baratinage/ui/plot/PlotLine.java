package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class PlotLine extends PlotItem {

    private String label;
    private Paint paint;
    private Stroke stroke;
    private Shape shape;

    protected DefaultXYDataset dataset;
    protected DefaultXYItemRenderer renderer;

    public PlotLine(String label, double[] x, double[] y, Paint paint) {
        this(label, x, y, paint, buildStroke());
    }

    public PlotLine(String label, double[] x, double[] y, Paint paint, int lineWidth) {
        this(label, x, y, paint, buildStroke(lineWidth));
    }

    public PlotLine(String label, double[] x, double[] y, Paint paint, Stroke stroke) {
        if (x.length != y.length)
            throw new IllegalArgumentException("x and y must have the same length!");

        this.label = label;
        this.paint = paint;
        this.stroke = stroke;
        this.shape = buildEmptyShape();

        dataset = new DefaultXYDataset();
        dataset.addSeries(label, new double[][] { x, y });

        renderer = new DefaultXYItemRenderer();
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, shape);

    }

    @Override
    public DefaultXYDataset getDataset() {
        return dataset;
    }

    @Override
    public DefaultXYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        Shape lineShape = buildLineShape(7);
        return new LegendItem(
                label,
                null,
                label,
                null,
                false,
                shape,
                false,
                paint,
                false,
                paint,
                stroke,
                true,
                lineShape,
                stroke,
                paint);
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

}
