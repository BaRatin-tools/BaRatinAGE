package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class PlotInfiniteBand extends PlotItem {

    private static final double MAX_VALUE = 9999999;
    private static final double MIN_VALUE = -MAX_VALUE;

    private boolean isVerticalBand = false;
    private double bLow;
    private double bHigh;
    private double a;

    private int n = 2000;

    private DeviationRenderer renderer;

    private XYPlot plot;

    private String label;
    private Paint fillPaint;

    public PlotInfiniteBand(String label, double xLow, double yHigh, Paint fillPaint) {
        this(label, 0, xLow, yHigh, fillPaint);
        isVerticalBand = true;
        n = 2;
    }

    public PlotInfiniteBand(String label, double coeffDir, double offsetLow, double offsetHigh, Paint fillPaint) {

        this.label = label;
        this.fillPaint = fillPaint;

        a = coeffDir;
        bLow = offsetLow;
        bHigh = offsetHigh;

        renderer = new DeviationRenderer();
        renderer.setSeriesStroke(0, buildEmptyStroke());
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, fillPaint);
        renderer.setSeriesShape(0, buildEmptyShape());
        renderer.setSeriesShapesVisible(0, false);

    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public Range getDomainBounds() {
        return isVerticalBand ? new Range(bLow, bHigh) : null;
    }

    @Override
    public Range getRangeBounds() {
        if (plot == null || isVerticalBand) {
            return null;
        }
        return DatasetUtils.findRangeBounds(getDataset());
    }

    @Override
    public AbstractXYDataset getDataset() {
        double[] xValues = new double[] { 0, 1 };
        double[] yValuesLow = new double[] { 0, 1 };
        double[] yValuesHight = new double[] { 0, 1 };

        if (isVerticalBand) {
            xValues = new double[] { bLow, bHigh };
            yValuesLow = new double[] { MIN_VALUE, MIN_VALUE };
            yValuesHight = new double[] { MAX_VALUE, MAX_VALUE };
            if (plot != null) {
                double lb = plot.getRangeAxis().getLowerBound();
                double ub = plot.getRangeAxis().getUpperBound();
                yValuesLow = new double[] { lb, lb };
                yValuesHight = new double[] { ub, ub };

            }
        } else {
            double lb = MIN_VALUE;
            double ub = MAX_VALUE;
            if (plot != null) {
                lb = plot.getDomainAxis().getLowerBound();
                ub = plot.getDomainAxis().getUpperBound();
                yValuesLow = new double[] { lb, ub };
                yValuesHight = new double[] { lb, ub };
            }
            double step = (ub - lb) / (n - 1);
            xValues = new double[n];
            yValuesLow = new double[n];
            yValuesHight = new double[n];
            for (int k = 0; k < n; k++) {
                xValues[k] = lb + k * step;
                yValuesLow[k] = a * xValues[k] + bLow;
                yValuesHight[k] = a * xValues[k] + bHigh;
            }
        }

        YIntervalSeries series = new YIntervalSeries(label);
        for (int k = 0; k < n; k++) {
            series.add(xValues[k], yValuesLow[k], yValuesLow[k], yValuesHight[k]);
        }

        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    @Override
    public DeviationRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        Shape squareShape = buildSquareShape();
        Stroke emptyStroke = buildEmptyStroke();
        Shape emptyShape = buildEmptyShape();
        return new LegendItem(
                label,
                null,
                label,
                null,
                true,
                squareShape,
                true,
                fillPaint,
                false,
                fillPaint,
                emptyStroke,
                false,
                emptyShape,
                emptyStroke,
                fillPaint);

    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

}
