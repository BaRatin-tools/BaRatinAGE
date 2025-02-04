package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;

public class PlotInfiniteBand extends PlotItem {

    private int n = 2000; // necessary for log scales on y

    private Paint fillPaint;
    private Shape shape;
    private float alpha;

    private final boolean isVerticalBand;
    private final boolean isHorizontalBand;
    private final double coeffDir;
    private final double offsetLow;
    private final double offsetHigh;

    private XYPlot plot;

    private CustomAreaRenderer renderer;

    public PlotInfiniteBand(String label, double coeffDir, double offsetLow, double offsetHigh, Paint fillPaint,
            float alpha) {

        setLabel(label);
        this.fillPaint = fillPaint;
        this.shape = buildEmptyShape();
        this.alpha = alpha;

        isVerticalBand = Double.isInfinite(coeffDir);
        isHorizontalBand = coeffDir == 0;
        this.coeffDir = coeffDir;
        this.offsetLow = offsetLow;
        this.offsetHigh = offsetHigh;

        renderer = new CustomAreaRenderer();
        renderer.setAlpha(alpha);

        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, fillPaint);
        renderer.setSeriesShape(0, shape);
    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public Range getDomainBounds() {
        if (isVerticalBand) {
            return new Range(offsetLow, offsetHigh);
        } else if (isHorizontalBand) {
            return null;
        } else {
            return DatasetUtils.findDomainBounds(getDataset());
        }
    }

    @Override
    public Range getRangeBounds() {
        if (isVerticalBand) {
            return null;
        } else if (isHorizontalBand) {
            return new Range(offsetLow, offsetHigh);
        } else {
            return DatasetUtils.findRangeBounds(getDataset());
        }
    }

    @Override
    public CustomAreaDataset getDataset() {

        CustomAreaDataset dataset = new CustomAreaDataset();

        if (plot == null) {
            return dataset;
        }

        if (isVerticalBand) {
            double top = plot.getRangeAxis().getLowerBound();
            double bottom = plot.getRangeAxis().getUpperBound();
            dataset.addVerticalBandSeries(
                    "",
                    new double[] { bottom, top },
                    new double[] { offsetLow, offsetLow },
                    new double[] { offsetHigh, offsetHigh });
        } else if (isHorizontalBand) {
            double left = plot.getDomainAxis().getLowerBound();
            double right = plot.getDomainAxis().getUpperBound();
            dataset.addHorizontalBandSeries(
                    "",
                    new double[] { left, right },
                    new double[] { offsetLow, offsetLow },
                    new double[] { offsetHigh, offsetHigh });
        } else {
            double left = plot.getDomainAxis().getLowerBound();
            double right = plot.getDomainAxis().getUpperBound();

            double step = (right - left) / (n - 1);

            double[] x = new double[n];
            double[] yLow = new double[n];
            double[] yHigh = new double[n];

            for (int k = 0; k < n; k++) {
                x[k] = left + k * step;
                yLow[k] = coeffDir * x[k] + offsetLow;
                yHigh[k] = coeffDir * x[k] + offsetHigh;
            }

            dataset.addHorizontalBandSeries("", x, yLow, yHigh);
        }

        return dataset;
    }

    @Override
    public AbstractXYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        Shape squareShape = buildSquareShape();
        return buildLegendItem(label, null, null, squareShape, fillPaint);
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        alpha = rendererSettings.getFillAlpha();
        fillPaint = rendererSettings.getFillPaint();

        renderer = new CustomAreaRenderer();
        renderer.setAlpha(alpha);
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesShape(0, shape);
    }

}
