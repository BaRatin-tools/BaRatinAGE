package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

public class PlotInfiniteLine extends PlotItem {

    private static final double MAX_VALUE = 9999999;
    private static final double MIN_VALUE = -MAX_VALUE;

    private boolean isVerticalLine = false;
    private boolean isHorizontalLine = false;
    private double b;
    private double a;

    private int n = 2000;

    private DefaultXYItemRenderer renderer;

    private XYPlot plot;

    private Paint paint;
    private Stroke stroke;
    private Shape shape;

    public PlotInfiniteLine(String label, double x) {
        this(label, x, Color.BLACK, 1);
    }

    public PlotInfiniteLine(String label, double x, Paint paint, int lineWidth) {
        this(label, x, paint, buildStroke(lineWidth));
    }

    public PlotInfiniteLine(String label, double x, Paint paint, Stroke stroke) {
        this(label, Double.POSITIVE_INFINITY, x, paint, stroke);
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset) {
        this(label, coeffDir, offset, Color.BLACK, 1);
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset, Paint paint, int lineWidth) {
        this(label, coeffDir, offset, paint, buildStroke(lineWidth));
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset, Paint paint, Stroke stroke) {

        setLabel(label);
        this.paint = paint;
        this.stroke = stroke;
        this.shape = buildEmptyShape();

        a = coeffDir;
        b = offset;

        isVerticalLine = false;
        isHorizontalLine = false;
        n = 2000;
        if (Double.isInfinite(coeffDir)) {
            isVerticalLine = true;
            n = 2;
        } else if (coeffDir == 0) {
            isHorizontalLine = true;
            n = 2;
        }

        renderer = new DefaultXYItemRenderer();
        renderer.setDrawSeriesLineAsPath(true);
        renderer.setSeriesShape(0, this.shape);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
    }

    public void updateDataset(double x) {
        updateDataset(Double.POSITIVE_INFINITY, x);
    }

    public void updateDataset(double coeffDir, double offset) {
        a = coeffDir;
        b = offset;
        isVerticalLine = false;
        isHorizontalLine = false;
        n = 2000;
        if (Double.isInfinite(coeffDir)) {
            isVerticalLine = true;
            n = 2;
        } else if (coeffDir == 0) {
            isHorizontalLine = true;
            n = 2;
        }
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
        renderer.setSeriesPaint(0, paint);
    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public Range getDomainBounds() {
        return null;
    }

    @Override
    public Range getRangeBounds() {
        return null;
    }

    @Override
    public AbstractXYDataset getDataset() {
        double[] xValues = new double[] { 0, 1 };
        double[] yValues = new double[] { 0, 1 };
        if (isVerticalLine) {
            xValues = new double[] { b, b };
            yValues = new double[] { MIN_VALUE, MAX_VALUE };
            if (plot != null) {
                double lb = plot.getRangeAxis().getLowerBound();
                double ub = plot.getRangeAxis().getUpperBound();
                yValues = new double[] { lb, ub };
            }
        } else if (isHorizontalLine) {
            xValues = new double[] { MIN_VALUE, MAX_VALUE };
            yValues = new double[] { b, b };
            if (plot != null) {
                double lb = plot.getDomainAxis().getLowerBound();
                double ub = plot.getDomainAxis().getUpperBound();
                xValues = new double[] { lb, ub };
            }
        } else {
            double lb = MIN_VALUE;
            double ub = MAX_VALUE;
            if (plot != null) {
                lb = plot.getDomainAxis().getLowerBound();
                ub = plot.getDomainAxis().getUpperBound();
                yValues = new double[] { lb, ub };
            }
            double step = (ub - lb) / (n - 1);
            xValues = new double[n];
            yValues = new double[n];
            for (int k = 0; k < n; k++) {
                xValues[k] = lb + k * step;
                yValues[k] = a * xValues[k] + b;
            }
        }
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries(getLabel(), new double[][] { xValues, yValues });
        return dataset;
    }

    @Override
    public AbstractXYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        return buildLegendItem(getLabel(), paint, stroke, null, null);
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        stroke = buildStroke(
                rendererSettings.getLineWidth(),
                rendererSettings.getLineDashArray());
        paint = rendererSettings.getLinePaint();

        renderer = new DefaultXYItemRenderer();
        renderer.setDrawSeriesLineAsPath(true);
        renderer.setSeriesShape(0, buildEmptyShape());
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
    }

    @Override
    public PlotInfiniteLine getCopy() {
        return new PlotInfiniteLine(getLabel(), a, b, paint, stroke);
    }

}
