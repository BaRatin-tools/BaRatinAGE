package org.baratinage.ui.plot;

import java.awt.BasicStroke;
import java.awt.Paint;

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

    private String label;

    private boolean isVerticalLine = false;
    private double b;
    private double a;

    private int n = 2000;

    private DefaultXYItemRenderer renderer;

    private XYPlot plot;

    public PlotInfiniteLine(String label, double x, Paint paint, int lineWidth) {
        this(label, 0, x, paint, lineWidth);
        isVerticalLine = true;
        n = 2;
    }

    public PlotInfiniteLine(String label, double coeffDir, double offset, Paint paint, int lineWidth) {

        this.label = label;

        a = coeffDir;
        b = offset;

        renderer = new DefaultXYItemRenderer();

        renderer.setSeriesShape(0, buildCircleShape(0));
        renderer.setSeriesShapesVisible(0, false);

        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, new BasicStroke(lineWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                1, new float[] { 1F }, 0));
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

}