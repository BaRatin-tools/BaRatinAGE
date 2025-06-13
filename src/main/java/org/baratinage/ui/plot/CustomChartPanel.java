package org.baratinage.ui.plot;

import java.awt.Graphics;

// import java.util.ArrayList;
// import java.util.List;
// import javax.swing.event.ChangeEvent;
// import javax.swing.event.ChangeListener;

import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;

/**
 * A modification of the ChartPanel class:
 * 
 * modified restoreAutoBounds methods to ignore
 * dataset marked to be ignored in Plot.
 * 
 * modified paintComponent methods to apply a fixed
 * padding to the plot
 */
public class CustomChartPanel extends ChartPanel {

  private final Plot plot;

  public CustomChartPanel(Plot plot) {
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
    Range domainBounds = plot.getDomainBounds();
    Range rangeBounds = plot.getRangeBounds();
    if (domainBounds != null) {
      if (domainBounds.getLength() == 0) {
        double value = domainBounds.getCentralValue();
        double offset = Math.abs(value * 0.1);
        domainBounds = new Range(value - offset, value + offset);
      }
      plot.plot.getDomainAxis().setRange(domainBounds);
    }
    if (rangeBounds != null) {
      if (rangeBounds.getLength() == 0) {
        double value = rangeBounds.getCentralValue();
        double offset = Math.abs(value * 0.1);
        rangeBounds = new Range(value - offset, value + offset);
      }
      plot.plot.getRangeAxis().setRange(rangeBounds);
    }
  }

  RectangleInsets fixedInsets = null;
  RectangleInsets oldRect = null;

  boolean noLongerChange = false;

  public void setFixedPadding(
      double top, double left, double bottom, double right) {
    fixedInsets = new RectangleInsets(top, left, bottom, right);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (fixedInsets != null) {

      ChartRenderingInfo info = getChartRenderingInfo();
      Rectangle2D chartArea = info.getChartArea();
      Rectangle2D dataArea = info.getPlotInfo().getDataArea();

      RectangleInsets chartInsets = plot.getChart().getPadding();

      RectangleInsets currentInsets = new RectangleInsets(
          dataArea.getY(),
          dataArea.getX(),
          chartArea.getHeight() - dataArea.getY() - dataArea.getHeight(),
          chartArea.getWidth() - dataArea.getX() - dataArea.getWidth());

      RectangleInsets missingInsets = new RectangleInsets(
          fixedInsets.getTop() - currentInsets.getTop() + chartInsets.getTop(),
          fixedInsets.getLeft() - currentInsets.getLeft() + chartInsets.getLeft(),
          fixedInsets.getBottom() - currentInsets.getBottom() + chartInsets.getBottom(),
          fixedInsets.getRight() - currentInsets.getRight() + chartInsets.getRight());

      if (missingInsets.equals(chartInsets)) {
        return;
      }
      plot.getChart().setPadding(missingInsets);
    }
  }
}
