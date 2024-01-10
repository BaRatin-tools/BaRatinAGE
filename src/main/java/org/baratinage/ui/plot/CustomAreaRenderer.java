package org.baratinage.ui.plot;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;

public class CustomAreaRenderer extends AbstractXYItemRenderer {

    private float alpha = 0.9f;

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
            XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
            CrosshairState crosshairState, int pass) {

        // only draw if last item of the series
        if (item != (dataset.getItemCount(series) - 1)) {
            return;
        }

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));
        g2.setPaint(getItemFillPaint(series, item));

        CustomAreaDataset areaDataset = (CustomAreaDataset) dataset;

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        float[] x = convertValues(areaDataset.getXValues(series), domainAxis, dataArea, xAxisLocation);
        float[] y = convertValues(areaDataset.getYValues(series), rangeAxis, dataArea, yAxisLocation);

        int n = x.length;
        GeneralPath area = new GeneralPath(GeneralPath.WIND_NON_ZERO, n + 1);

        area.moveTo(x[0], y[0]);
        for (int i = 1; i < n; i++) {
            area.lineTo(x[i], y[i]);
        }
        area.lineTo(x[0], y[0]);
        area.closePath();

        g2.fill(area);
        g2.setComposite(originalComposite);

    }

    public float[] convertValues(double[] values, ValueAxis axis, Rectangle2D area, RectangleEdge edge) {
        int n = values.length;
        float[] convertedValues = new float[n];
        for (int k = 0; k < n; k++) {
            convertedValues[k] = (float) axis.valueToJava2D(values[k], area, edge);
        }
        return convertedValues;
    }

    @Override
    public Object clone() {
        CustomAreaRenderer cloned = new CustomAreaRenderer();
        cloned.setAlpha(this.alpha);
        return cloned;
    }

}
