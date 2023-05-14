package org.baratinage.ui.plot;

import java.awt.BasicStroke;
import java.awt.Paint;

import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.AbstractXYDataset;

import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class PlotPoints extends PlotItem {

    private XYIntervalSeriesCollection dataset;
    private XYErrorRenderer renderer;

    // public PlotPoints(String label, double[] x, double[] xLow, double[] xHigh,
    // double[] y, double[] yLow, double[] yHigh,
    // Paint[] paint, SHAPE[] shape, int[] shapeSize, int lineWidth) {

    // }

    public PlotPoints(String label,
            double[] x, double[] xLow, double[] xHigh,
            double[] y, double[] yLow, double[] yHigh,
            Paint paint, SHAPE shape, int shapeSize, int lineWidth) {
        int n = x.length;

        if (y.length != n || xLow.length != n || xHigh.length != n ||
                yLow.length != n || yHigh.length != n)
            throw new IllegalArgumentException("x, y, xStart, xEnd, yStart, yEnd must all have the same length!");

        dataset = new XYIntervalSeriesCollection();
        XYIntervalSeries series = new XYIntervalSeries(label);
        dataset.addSeries(series);
        for (int k = 0; k < n; k++) {
            series.add(x[k], xLow[k], xHigh[k], y[k], yLow[k], yHigh[k]);
        }

        renderer = new XYErrorRenderer();

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
    public AbstractXYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public AbstractXYDataset getDataset() {
        return dataset;
    }

}
