package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class PlotPoints extends PlotItem {

    private Paint paint;
    private Stroke errorLineStroke;
    private Shape shape;

    private double[] x;
    private double[] xLow;
    private double[] xHigh;
    private double[] y;
    private double[] yLow;
    private double[] yHigh;

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

        setLabel(label);

        this.paint = paint;
        this.shape = shape;
        this.errorLineStroke = errorLineStroke;

        updateDataset(x, xLow, xHigh, y, yLow, yHigh);

        // dataset = new XYIntervalSeriesCollection();

        // XYIntervalSeries series = new XYIntervalSeries(label);
        // dataset.addSeries(series);
        // for (int k = 0; k < n; k++) {
        // series.add(x[k], xLow[k], xHigh[k], y[k], yLow[k], yHigh[k]);
        // }

        renderer = new XYErrorRenderer();

        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, errorLineStroke);
        renderer.setSeriesShape(0, shape);

    }

    public boolean hasErrorBars() {
        XYIntervalSeries series = dataset.getSeries(0);
        for (int k = 0; k < series.getItemCount(); k++) {
            Number x = series.getX(k);
            Number xL = series.getXLowValue(k);
            Number xH = series.getXHighValue(k);
            Number y = series.getYValue(k);
            Number yL = series.getYLowValue(k);
            Number yH = series.getYHighValue(k);
            if (!x.equals(xL) || x.equals(xH) || !y.equals(yL) || !y.equals(yH)) {
                return true;
            }
        }
        return false;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
        renderer.setSeriesPaint(0, paint);
    }

    public void updateDataset(double[] x, double[] xLow, double[] xHigh, double[] y, double[] yLow, double[] yHigh) {
        this.x = x;
        this.xLow = xLow;
        this.xHigh = xHigh;
        this.y = y;
        this.yLow = yLow;
        this.yHigh = yHigh;

        int n = x.length;
        dataset = new XYIntervalSeriesCollection();
        XYIntervalSeries series = new XYIntervalSeries(getLabel());
        dataset.addSeries(series);
        for (int k = 0; k < n; k++) {
            series.add(x[k], xLow[k], xHigh[k], y[k], yLow[k], yHigh[k]);
        }
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
        return buildLegendItem(getLabel(), null, null, shape, paint);
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        errorLineStroke = buildStroke(
                rendererSettings.getLineWidth(),
                rendererSettings.getLineDashArray());
        paint = rendererSettings.getLinePaint();

        double shapeSize = rendererSettings.getShapeSize();
        shape = rendererSettings.getShapeType().getShape((int) shapeSize);
        renderer = new XYErrorRenderer();

        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, errorLineStroke);
        renderer.setSeriesShape(0, shape);
    }

    @Override
    protected PlotPoints getPartialCopy() {
        return new PlotPoints(getLabel(), x, xLow, xHigh, y, yLow, yHigh, paint, shape, errorLineStroke);
    }
}
