package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class PlotBand extends PlotItem {

    private String label;
    private Paint fillPaint;
    private Paint linePaint;
    private Stroke lineStroke;
    private Shape shape;

    private YIntervalSeriesCollection dataset;
    private DeviationRenderer renderer;

    public PlotBand(String label,
            double[] x, double[] ylow, double[] yhigh,
            Paint paint) {
        this(label, x, ylow, ylow, yhigh, paint, paint, buildEmptyStroke());
    }

    public PlotBand(String label,
            double[] x, double[] y, double[] ylow, double[] yhigh,
            Paint fillPaint,
            Paint linePaint, Stroke lineStroke) {

        int n = x.length;

        if (y.length != n) {
            throw new IllegalArgumentException("'x' and 'y' must have the same length!");
        }
        if (ylow.length != n) {
            throw new IllegalArgumentException("'x' and 'ylow' must have the same length!");
        }
        if (yhigh.length != n) {
            throw new IllegalArgumentException("'x' and 'yhigh' must have the same length!");
        }

        this.label = label;
        this.fillPaint = fillPaint;
        this.linePaint = linePaint;
        this.lineStroke = lineStroke;
        this.shape = buildEmptyShape();

        YIntervalSeries series = new YIntervalSeries(label);

        for (int k = 0; k < n; k++) {
            series.add(x[k], y[k], ylow[k], yhigh[k]);
        }

        dataset = new YIntervalSeriesCollection();
        dataset.addSeries(series);

        renderer = new DeviationRenderer();
        renderer.setAlpha(0.9f);

        renderer.setSeriesStroke(0, lineStroke);
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, linePaint);
        renderer.setSeriesShape(0, shape);

    }

    @Override
    public AbstractXYDataset getDataset() {
        return dataset;
    }

    @Override
    public DeviationRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {

        return buildLegendItem(label, fillPaint, linePaint, lineStroke);
    }

    public static LegendItem buildLegendItem(String label, Paint fillPaint,
            Paint linePaint, Stroke lineStroke) {
        Shape squareShape = buildSquareShape();
        return PlotItem.buildLegendItem(label, linePaint, lineStroke, squareShape, linePaint);
    }

    public static LegendItem buildLegendItem(String label, Paint fillPaint) {
        return buildLegendItem(label, fillPaint, fillPaint, buildEmptyStroke());
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

}
