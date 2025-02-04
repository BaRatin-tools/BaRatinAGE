package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class PlotTimeSeriesLine extends PlotItem {

    private Paint paint;
    private Stroke stroke;
    private Shape shape;

    TimeSeriesCollection tsCollection;
    DefaultXYItemRenderer renderer;

    public PlotTimeSeriesLine(String label, Second[] time, double[] values, Paint paint, Stroke stroke) {
        setLabel(label);
        this.paint = paint;
        this.stroke = stroke;
        this.shape = buildEmptyShape();

        int n = time.length;
        TimeSeries ts = new TimeSeries(label);
        for (int k = 0; k < n; k++) {
            ts.add(time[k], values[k]);
        }

        tsCollection = new TimeSeriesCollection();
        tsCollection.addSeries(ts);

        renderer = new DefaultXYItemRenderer();
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, shape);
    }

    @Override
    public XYDataset getDataset() {
        return tsCollection;
    }

    @Override
    public XYItemRenderer getRenderer() {
        return renderer;
    }

    @Override
    public LegendItem getLegendItem() {
        return buildLegendItem(label, paint, stroke, null, null);
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        stroke = buildStroke(
                rendererSettings.getLineWidth(),
                rendererSettings.getLineDashArray());
        paint = rendererSettings.getLinePaint();

        renderer = new DefaultXYItemRenderer();
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesShape(0, shape);
    }

}
