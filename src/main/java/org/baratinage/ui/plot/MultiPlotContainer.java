package org.baratinage.ui.plot;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;

public class MultiPlotContainer extends SimpleFlowPanel {

  private final List<Plot> plots;
  private final List<CustomChartPanel> charts;

  public MultiPlotContainer(boolean isVertical) {
    super(isVertical);
    plots = new ArrayList<>();
    charts = new ArrayList<>();

  }

  Range currentDomainBounds = null;
  Range targetDomainBounds = null;
  boolean listenersDisabled = false;

  public void addPlot(Plot plot, float weight) {
    plots.add(plot);
    CustomChartPanel chart = new CustomChartPanel(plot);
    charts.add(chart);
    addChild(chart, weight);
    updateDomainRange(getMaxDomainRange());
    plot.getChart().addChangeListener((l) -> {
      if (listenersDisabled) {
        return;
      }
      Range newDomainBounds = plot.plot.getDomainAxis().getRange();
      updateDomainRange(newDomainBounds);

      double legendWidth = 0;
      for (Plot p : plots) {
        LegendTitle t = p.getChart().getLegend();
        if (t == null) {
          continue;
        }
        double w = t.getBounds().getWidth();
        if (w > legendWidth) {
          legendWidth = w;
        }
      }
    });

    chart.setFixedPadding(5, 70, 30, 170);
  }

  public Range getCurrentDomainRange() {
    if (plots.size() == 0) {
      return new Range(0, 0);
    }
    return plots.get(0).plot.getDomainAxis().getRange();
  }

  public void setDomainRange(Range range) {
    updateDomainRange(range);
  }

  private Range getMaxDomainRange() {
    double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
    for (Plot p : plots) {
      Range r = p.plot.getDomainAxis().getRange();
      if (r.getLowerBound() < min) {
        min = r.getLowerBound();
      }
      if (r.getUpperBound() > max) {
        max = r.getUpperBound();
      }
    }
    return new Range(min, max);
  }

  private void updateDomainRange(Range targetRange) {
    listenersDisabled = true;
    targetDomainBounds = targetRange;
    for (Plot p : plots) {
      Range r = p.plot.getDomainAxis().getRange();
      if (!r.equals(targetRange)) {
        p.plot.getDomainAxis().setRange(targetRange);
      }
    }
    currentDomainBounds = targetRange;
    listenersDisabled = false;
  }

}
