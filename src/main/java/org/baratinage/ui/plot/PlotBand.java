package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;

public class PlotBand extends PlotItem {

    private Paint fillPaint;
    private Paint linePaint;
    private Stroke lineStroke;
    private Shape shape;
    private float alpha;

    private CustomAreaDataset dataset;
    private CustomAreaRenderer renderer;

    public PlotBand(String label,
            double[] ref, double[] low, double[] high, boolean vertical,
            Paint fillPaint) {
        this(
                label,
                ref, low, high,
                vertical,
                fillPaint, 0.9f, fillPaint, buildEmptyStroke());
    }

    public PlotBand(String label,
            double[] ref, double[] low, double[] high, boolean vertical,
            Paint fillPaint, float alpha) {
        this(
                label,
                ref, low, high,
                vertical,
                fillPaint, alpha, fillPaint, buildEmptyStroke());
    }

    public PlotBand(String label,
            double[] ref, double[] low, double[] high, boolean vertical,
            Paint fillPaint, float alpha,
            Paint linePaint, Stroke lineStroke) {

        int n = ref.length;

        if (low.length != n) {
            throw new IllegalArgumentException("'ref' and 'low' must have the same length!");
        }
        if (high.length != n) {
            throw new IllegalArgumentException("'ref' and 'high' must have the same length!");
        }

        setLabel(label);
        this.fillPaint = fillPaint;
        this.linePaint = linePaint;
        this.lineStroke = lineStroke;
        this.shape = buildEmptyShape();
        this.alpha = alpha;

        dataset = new CustomAreaDataset();
        if (vertical) {
            dataset.addVerticalBandSeries("", ref, low, high);
        } else {
            dataset.addHorizontalBandSeries("", ref, low, high);
        }

        renderer = new CustomAreaRenderer();
        renderer.setAlpha(alpha);

        renderer.setSeriesStroke(0, lineStroke);
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, linePaint);
        renderer.setSeriesShape(0, shape);

    }

    @Override
    public CustomAreaDataset getDataset() {
        return dataset;
    }

    @Override
    public AbstractXYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {

        return buildLegendItem(label, fillPaint, linePaint, lineStroke);
    }

    public static LegendItem buildLegendItem(String label, Paint fillPaint,
            Paint linePaint, Stroke lineStroke) {
        Shape squareShape = buildSquareShape();
        return PlotItem.buildLegendItem(label, linePaint, lineStroke, squareShape, fillPaint);
    }

    public static LegendItem buildLegendItem(String label, Paint fillPaint) {
        return buildLegendItem(label, fillPaint, fillPaint, buildEmptyStroke());
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        alpha = rendererSettings.getFillAlpha();
        lineStroke = buildStroke(
                rendererSettings.getLineWidth(),
                rendererSettings.getLineDashArray());
        fillPaint = rendererSettings.getFillPaint();
        linePaint = rendererSettings.getLinePaint();

        renderer = new CustomAreaRenderer();
        renderer.setAlpha(alpha);
        renderer.setSeriesStroke(0, lineStroke);
        renderer.setSeriesFillPaint(0, fillPaint);
        renderer.setSeriesPaint(0, linePaint);
        renderer.setSeriesShape(0, shape);
    }

}
