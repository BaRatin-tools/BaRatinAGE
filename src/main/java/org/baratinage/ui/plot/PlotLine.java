package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

public class PlotLine extends PlotItem {

    private Paint paint;
    private Stroke stroke;
    private Shape shape;

    protected DefaultXYDataset dataset;
    protected XYLineAndShapeRenderer renderer;

    public PlotLine(String label, double[] x, double[] y, Paint paint) {
        this(label, x, y, paint, buildStroke());
    }

    public PlotLine(String label, double[] x, double[] y, Paint paint, int lineWidth) {
        this(label, x, y, paint, buildStroke(lineWidth));
    }

    public PlotLine(String label, double[] x, double[] y, Paint paint, Stroke stroke) {
        if (x.length != y.length)
            throw new IllegalArgumentException("x and y must have the same length!");

        setLabel(label);
        this.paint = paint;
        this.stroke = stroke;
        this.shape = buildEmptyShape();

        dataset = new DefaultXYDataset();
        dataset.addSeries(label, new double[][] { x, y });

        renderer = new DefaultXYItemRenderer();
        renderer.setDrawSeriesLineAsPath(true);
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, this.shape);

    }

    public void setSplineRenderer(int precision) {
        XYSplineRenderer splineRenderer = new XYSplineRenderer(precision);
        splineRenderer.setSeriesPaint(0, paint);
        splineRenderer.setSeriesStroke(0, stroke);
        splineRenderer.setSeriesShape(0, buildEmptyShape());
        renderer = splineRenderer;

    }

    @Override
    public XYDataset getDataset() {
        return dataset;
    }

    @Override
    public XYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        return PlotLine.buildLegendItem(label, paint, stroke);
    }

    public static LegendItem buildLegendItem(String label, Paint paint, Stroke stroke) {
        return PlotItem.buildLegendItem(label, paint, stroke, null, null);
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        stroke = buildStroke(
                rendererSettings.getLineWidth(),
                rendererSettings.getLineDashArray());
        paint = rendererSettings.getLinePaint();

        renderer = new DefaultXYItemRenderer();
        renderer.setDrawSeriesLineAsPath(true);
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, shape);
    }

}
