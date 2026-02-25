package org.baratinage.ui.plot;

import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleInsets;

/**
 * A modification of the ChartPanel class:
 * 
 * modified restoreAutoBounds methods to ignore
 * dataset marked to be ignored in IPlot.
 * 
 * modified paintComponent methods to apply a fixed
 * padding to the plot
 */
public class CustomChartPanel extends ChartPanel {

  private final IPlot plot;

  public CustomChartPanel(IPlot plot) {
    super(plot.getChart());
    this.plot = plot;

    restoreAutoBounds();
    setMinimumDrawWidth(100);
    setMinimumDrawHeight(100);
    setMaximumDrawWidth(10000);
    setMaximumDrawHeight(10000);
  }

  @Override
  public void restoreAutoBounds() {
    plot.restoreAutoBounds();
  }

  RectangleInsets fixedInsets = null;
  RectangleInsets oldRect = null;

  boolean noLongerChange = false;

  public Insets getMaxInsets() {
    Insets insets = new Insets(0, 0, 0, 0);
    ChartRenderingInfo chartInfo = getChartRenderingInfo();
    if (chartInfo == null) {
      return insets;
    }
    Rectangle2D chartArea = chartInfo.getChartArea();
    if (chartArea == null) {
      return insets;
    }
    PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();
    if (plotInfo == null) {
      return insets;
    }
    Rectangle2D dataArea = plotInfo.getDataArea();
    if (dataArea == null) {
      return insets;
    }
    RectangleInsets totalInsets = getRectInsets(dataArea, chartArea);
    return toInsets(totalInsets);
  }

  public Insets getPadding() {
    if (plot == null || plot.getChart() == null || plot.getChart().getPadding() == null) {
      return new Insets(0, 0, 0, 0);
    }
    return toInsets(plot.getChart().getPadding());
  }

  public void setPadding(Insets insets) {
    if (plot == null || plot.getChart() == null) {
      return;
    }
    plot.getChart().setPadding(getRectInsets(insets));
  }

  public static Insets toInsets(RectangleInsets rectInsets) {
    return new Insets(
        (int) rectInsets.getTop(),
        (int) rectInsets.getLeft(),
        (int) rectInsets.getBottom(),
        (int) rectInsets.getRight());
  }

  private static RectangleInsets getRectInsets(Insets insets) {
    return new RectangleInsets(
        (double) insets.top,
        (double) insets.left,
        (double) insets.bottom,
        (double) insets.right);
  }

  private static RectangleInsets getRectInsets(Rectangle2D inner, Rectangle2D outer) {
    double top = inner.getY() - outer.getY();
    double left = inner.getX() - outer.getX();
    double bottom = outer.getMaxY() - inner.getMaxY();
    double right = outer.getMaxX() - inner.getMaxX();
    return new RectangleInsets(top, left, bottom, right);
  }
}
