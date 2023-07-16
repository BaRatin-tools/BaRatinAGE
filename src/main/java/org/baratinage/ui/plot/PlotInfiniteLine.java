package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

public class PlotInfiniteLine extends PlotItem {

    private static final double MAX_VALUE = 9999999;
    private static final double MIN_VALUE = -MAX_VALUE;

    private boolean isVerticalLine = false;
    private double b;
    private double a;

    private int n = 2000;

    private DefaultXYItemRenderer renderer;

    private XYPlot plot;

    private String label;
    private Paint paint;
    private Stroke stroke;

    public PlotInfiniteLine(String label, double x, Paint paint, int lineWidth) {
        this(label, x, paint, buildStroke(lineWidth));
    }

    public PlotInfiniteLine(String label, double x, Paint paint, Stroke stroke) {
        this(label, 0, x, paint, stroke);
        isVerticalLine = true;
        n = 2;
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset, Paint paint, int lineWidth) {
        this(label, coeffDir, offset, paint, buildStroke(lineWidth));
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset, Paint paint, Stroke stroke) {

        this.label = label;
        this.paint = paint;
        this.stroke = stroke;

        a = coeffDir;
        b = offset;

        renderer = new DefaultXYItemRenderer();

        renderer.setSeriesShape(0, buildEmptyShape());
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public Range getDomainBounds() {
        return isVerticalLine ? new Range(b, b) : null;
        // return null;
    }

    @Override
    public Range getRangeBounds() {
        if (plot == null || isVerticalLine) {
            return null;
        }
        return DatasetUtils.findRangeBounds(getDataset());
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
        dataset.addSeries(label, new double[][] { xValues, yValues });
        return dataset;
    }

    @Override
    public AbstractXYItemRenderer getRenderer() {
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
                buildEmptyShape(),
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
