package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class PlotPoints extends PlotItem {

    private String label;
    private Paint paint;
    private Shape shape;

    private XYIntervalSeriesCollection dataset;
    private XYErrorRenderer renderer;

    public PlotPoints(String label,
            double[] x,
            double[] y,
            Paint paint) {
        this(label, x, x, x, y, y, y, paint, buildCircleShape(), buildEmptyStroke());
    }

    public PlotPoints(String label,
            double[] x,
            double[] y,
            Paint paint, Shape shape) {
        this(label, x, x, x, y, y, y, paint, shape, buildEmptyStroke());
    }

    public PlotPoints(String label,
            double[] x, double[] xLow, double[] xHigh,
            double[] y, double[] yLow, double[] yHigh,
            Paint paint) {
        this(label, x, xLow, xHigh, y, yLow, yHigh, paint, buildCircleShape(), buildStroke());
    }

    public PlotPoints(String label,
            double[] x, double[] xLow, double[] xHigh,
            double[] y, double[] yLow, double[] yHigh,
            Paint paint, Shape shape, Stroke errorLineStroke) {

        int n = x.length;

        if (y.length != n || xLow.length != n || xHigh.length != n ||
                yLow.length != n || yHigh.length != n)
            throw new IllegalArgumentException("x, y, xStart, xEnd, yStart, yEnd must all have the same length!");

        this.label = label;
        this.paint = paint;
        this.shape = shape;

        dataset = new XYIntervalSeriesCollection();

        XYIntervalSeries series = new XYIntervalSeries(label);
        dataset.addSeries(series);
        for (int k = 0; k < n; k++) {
            series.add(x[k], xLow[k], xHigh[k], y[k], yLow[k], yHigh[k]);
        }

        renderer = new XYErrorRenderer();

        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, errorLineStroke);
        renderer.setSeriesShape(0, shape);

    }

    @Override
    public XYIntervalSeriesCollection getDataset() {
        return dataset;
    }

    @Override
    public XYErrorRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        return buildLegendItem(label, null, null, shape, paint);
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

}
