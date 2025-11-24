package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.baratinage.AppSetup;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

public class StackedPlot implements IPlot, LegendItemSource {

  private final Plot mainPlot;
  private final List<Plot> subplots;
  private final HashMap<Plot, Integer> weights;

  public StackedPlot(Plot mainPlot, int weight) {
    this.mainPlot = mainPlot;
    subplots = new ArrayList<>();
    weights = new HashMap<>();
    weights.put(mainPlot, weight);
  }

  public void addSubplot(Plot subplot, int weight) {
    subplots.add(subplot);
    subplot.plot.setDomainAxis(mainPlot.plot.getDomainAxis());
    weights.put(subplot, weight);
  }

  @Override
  public JFreeChart getChart() {

    CombinedDomainXYPlot combined = new CombinedDomainXYPlot(mainPlot.plot.getDomainAxis());
    combined.add(mainPlot.plot, weights.get(mainPlot));
    for (Plot p : subplots) {
      combined.add(p.plot, weights.get(p));
    }

    JFreeChart chart = new JFreeChart(combined);
    chart.setBackgroundPaint(Color.WHITE);
    chart.removeLegend();

    chart.removeLegend();

    RectangleEdge position = RectangleEdge.RIGHT;
    LegendTitle currentLegend = new LegendTitle(this);
    currentLegend.setPosition(RectangleEdge.RIGHT);
    currentLegend.setPadding(
        position == RectangleEdge.TOP ? 10 : 0,
        position == RectangleEdge.LEFT ? 10 : 0,
        position == RectangleEdge.BOTTOM ? 10 : 0,
        position == RectangleEdge.RIGHT ? 10 : 0);
    chart.addLegend(currentLegend);

    Font font = new Font("SansSerif", Font.PLAIN, AppSetup.CONFIG.FONT_SIZE.get());

    mainPlot.plot.getDomainAxis().setLabelFont(font);
    for (Plot p : subplots) {
      p.plot.getRangeAxis().setLabelFont(font);
    }

    currentLegend.setItemFont(font);

    return chart;
  }

  public void restoreAutoBounds() {
    Range domainBounds = mainPlot.getDomainBounds();
    StackedPlot.restoreAutoDomainBounds(mainPlot, domainBounds);
    StackedPlot.restoreAutoRangeBounds(mainPlot);
    for (Plot p : subplots) {
      StackedPlot.restoreAutoDomainBounds(p, domainBounds);
      StackedPlot.restoreAutoRangeBounds(p);
    }
  }

  private static void restoreAutoDomainBounds(Plot plot, Range domainBounds) {
    // Range domainBounds = plot.getDomainBounds();
    if (domainBounds != null) {
      if (domainBounds.getLength() == 0) {
        double value = domainBounds.getCentralValue();
        double offset = Math.abs(value * 0.1);
        domainBounds = new Range(value - offset, value + offset);
      }
      plot.plot.getDomainAxis().setRange(domainBounds);
    }

  }

  private static void restoreAutoRangeBounds(Plot plot) {
    Range rangeBounds = plot.getRangeBounds();
    if (rangeBounds != null) {
      if (rangeBounds.getLength() == 0) {
        double value = rangeBounds.getCentralValue();
        double offset = Math.abs(value * 0.1);
        rangeBounds = new Range(value - offset, value + offset);
      }
      plot.plot.getRangeAxis().setRange(rangeBounds);
    }
  }

  @Override
  public IPlot getCopy() {
    Plot mainPlotCopy = mainPlot.getCopy();
    List<Plot> subplotsCopy = new ArrayList<>();
    for (Plot p : subplots) {
      subplotsCopy.add(p.getCopy());
    }
    StackedPlot stackedPlotCopy = new StackedPlot(mainPlotCopy, weights.get(mainPlot));
    for (Plot p : subplotsCopy) {
      stackedPlotCopy.addSubplot(p, weights.get(p));
    }
    return stackedPlotCopy;
  }

  @Override
  public LegendItemCollection getLegendItems() {
    return mainPlot.getLegendItems();
  }

}
