package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;

public class PlotUtils {

    public static double[] getDistancesFromPoint(PlotContainer plotContainer, double[] x, double[] y,
            Point screenPoint) {

        IPlot iplot = plotContainer.getPlot();
        if (!(iplot instanceof Plot)) {
            return new double[] {};
        }
        Plot plot = (Plot) iplot;

        ChartPanel chartPanel = plotContainer.getChartPanel();

        ValueAxis domainAxis = plot.plot.getDomainAxis();
        ValueAxis rangeAxis = plot.plot.getRangeAxis();

        Point2D p = chartPanel.translateScreenToJava2D(screenPoint);

        int n = x.length;
        double[] distances = new double[n];
        for (int k = 0; k < n; k++) {
            double xJava2D = domainAxis.valueToJava2D(x[k], chartPanel.getScreenDataArea(),
                    plot.plot.getDomainAxisEdge());
            double yJava2D = rangeAxis.valueToJava2D(y[k], chartPanel.getScreenDataArea(),
                    plot.plot.getRangeAxisEdge());
            distances[k] = p.distance(xJava2D, yJava2D);
        }

        return distances;
    }

    public static Image buildImageFromShape(Shape shape, Color color, int width, int height) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(color);
        g2.translate(width / 2, height / 2);
        g2.fill(shape);
        g2.translate(-width / 2, -height / 2);

        g2.dispose();

        return image;
    }

    public static Image buildImage(Shape shape, Stroke stroke, Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(color);
        g2.translate(width / 2, height / 2);
        if (shape != null) {
            g2.fill(shape);
        }
        if (stroke != null) {
            g2.setStroke(stroke);
        }
        g2.draw(shape);
        g2.translate(-width / 2, -height / 2);
        g2.dispose();

        return image;
    }

    public static Icon getPlotItemIcon(PlotItem item, int width, int height) {
        return getLegendItemIcon(item.getLegendItem(), width, height);
    }

    public static Icon getLegendItemIcon(LegendItem legendItem, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape shape = legendItem.getShape();

        if (shape != null) {
            g2.setPaint(legendItem.getFillPaint());
            g2.translate(width / 2, height / 2);
            g2.fill(shape);
            g2.translate(-width / 2, -height / 2);
        }

        Stroke stroke = legendItem.getLineStroke();
        Paint paint = legendItem.getLinePaint();
        Shape line = legendItem.getLine();

        if (line != null && stroke != null && paint != null) {
            g2.setStroke(stroke);
            g2.setPaint(paint);
            g2.translate(width / 2, height / 2);
            g2.draw(line);
            g2.translate(-width / 2, -height / 2);
        }

        g2.dispose();

        return new ImageIcon(image);
    }
}
