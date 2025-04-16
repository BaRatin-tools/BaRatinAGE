package org.baratinage.ui.plot;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.RectangularShape;

import org.baratinage.utils.Calc;
import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class PlotBar extends PlotItem {

    private static double MARGIN = 0.1;
    private Paint fillPaint;
    private float alpha;

    protected XYDataset dataset;
    protected XYItemRenderer renderer;

    public PlotBar(String label, double[] x, int nBins, Paint fillPaint, float alpha) {
        this(label, x, nBins, fillPaint, alpha, HistogramType.SCALE_AREA_TO_1);
    }

    public PlotBar(String label, double[] x, int nBins, Paint fillPaint, float alpha, HistogramType histType) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(histType);
        dataset.addSeries(label, x, nBins);
        this.dataset = dataset;

        this.label = label;
        this.fillPaint = fillPaint;
        this.alpha = alpha;

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, fillPaint);
        renderer.setMargin(MARGIN);
        CustomBarPainter barPainter = new CustomBarPainter();
        barPainter.alpha = alpha;
        renderer.setBarPainter(barPainter);

        this.renderer = renderer;
    }

    public PlotBar(String label, double[] x, double[] y, Paint fillPaint, float alpha) {
        int n = x.length;

        if (n != y.length)
            throw new IllegalArgumentException("x and y must have the same length!");

        setLabel(label);
        this.fillPaint = fillPaint;

        double[] xLow = new double[n];
        double[] xHigh = new double[n];
        double[] xRange = Calc.range(x);
        double xHalfBin = (xRange[1] - xRange[0]) / (double) n / 2d;
        for (int k = 0; k < n; k++) {
            xLow[k] = x[k] - xHalfBin;
            xHigh[k] = x[k] + xHalfBin;
        }

        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        dataset.addSeries(label, new double[][] { x, xLow, xHigh, y, y, y });
        this.dataset = dataset;

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, fillPaint);
        renderer.setMargin(MARGIN);
        CustomBarPainter barPainter = new CustomBarPainter();
        barPainter.alpha = alpha;
        renderer.setBarPainter(barPainter);

        this.renderer = renderer;

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
        return PlotBar.buildLegendItem(label, fillPaint);
    }

    public static LegendItem buildLegendItem(String label, Paint fillPaint) {
        Shape squareShape = buildSquareShape();
        return PlotItem.buildLegendItem(label, fillPaint, buildEmptyStroke(), squareShape, fillPaint);
    }

    private static class CustomBarPainter implements XYBarPainter {

        public float alpha;

        @Override
        public void paintBar(Graphics2D g2, XYBarRenderer renderer, int row, int column, RectangularShape bar,
                RectangleEdge base) {

            Paint itemPaint = renderer.getItemPaint(row, column);

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha));

            g2.setPaint(itemPaint);
            g2.fill(bar);
            g2.setComposite(originalComposite);

        }

        @Override
        public void paintBarShadow(Graphics2D g2, XYBarRenderer renderer, int row, int column, RectangularShape bar,
                RectangleEdge base, boolean pegShadow) {
            // disabled
        }
    }

    @Override
    public void configureRenderer(IPlotItemRendererSettings rendererSettings) {

        alpha = rendererSettings.getFillAlpha();
        fillPaint = rendererSettings.getFillPaint();

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, fillPaint);
        renderer.setMargin(MARGIN);
        CustomBarPainter barPainter = new CustomBarPainter();
        barPainter.alpha = alpha;
        renderer.setBarPainter(barPainter);

        this.renderer = renderer;
    }

}
