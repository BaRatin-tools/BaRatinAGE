package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class PlotBand extends PlotItem {

    private YIntervalSeriesCollection dataset;
    private DeviationRenderer renderer;

    public PlotBand(String label,
            double[] x, double[] ylow, double[] yhigh,
            Paint fillPaint) {
        this(label, x, ylow, ylow, yhigh, fillPaint, Color.WHITE, 0, SHAPE.NONE, 0);
    }

    public PlotBand(String label,
            double[] x, double[] y, double[] ylow, double[] yhigh,
            Paint fillPaint,
            Paint linePaint, int lineWidth) {
        this(label, x, y, ylow, yhigh, fillPaint, linePaint, lineWidth, SHAPE.NONE, 0);
    }

    public PlotBand(
            String label,
            double[] x, double[] y, double[] ylow, double[] yhigh,
            Paint fillPaint,
            Paint linePaint, int lineWidth,
            SHAPE shape, int shapeSize) {
        if (x.length != y.length)
            throw new IllegalArgumentException("x and y must have the same length!");

        YIntervalSeries series = new YIntervalSeries(label);

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

        for (int k = 0; k < n; k++) {
            series.add(x[k], y[k], ylow[k], yhigh[k]);
        }

        dataset = new YIntervalSeriesCollection();
        dataset.addSeries(series);

        renderer = new BandRenderer(fillPaint, lineWidth == 0 ? 10 : 0);

        renderer.setSeriesStroke(0,
                new BasicStroke(lineWidth,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1, new float[] { 1F }, 0));
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, linePaint);
        if (shape == SHAPE.CIRCLE) {
            renderer.setSeriesShape(0, buildCircleShape(10));
        } else if (shape == SHAPE.SQUARE) {
            renderer.setSeriesShape(0, buildSquareShape(10));
        } else {
            renderer.setSeriesShape(0, buildCircleShape(0));
            renderer.setSeriesShapesVisible(0, false);
        }

    }

    @Override
    public AbstractXYDataset getDataset() {
        return this.dataset;
    }

    @Override
    public AbstractXYItemRenderer getRenderer() {
        return this.renderer;
    }

}
