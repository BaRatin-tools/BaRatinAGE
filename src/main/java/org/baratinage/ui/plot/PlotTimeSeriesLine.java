package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import java.time.LocalDateTime;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class PlotTimeSeriesLine extends PlotItem {

    String label;
    Paint paint;
    Stroke stroke;

    TimeSeriesCollection tsCollection;
    DefaultXYItemRenderer renderer;

    public PlotTimeSeriesLine(String label, Second[] time, double[] values, Paint paint, Stroke stroke) {
        this.label = label;
        this.paint = paint;
        this.stroke = stroke;

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
        renderer.setSeriesShape(0, buildEmptyShape());
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
        Shape lineShape = buildLineShape(7);
        Shape emptyShape = buildEmptyShape();
        return new LegendItem(
                label,
                null,
                label,
                null,
                false,
                emptyShape,
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
