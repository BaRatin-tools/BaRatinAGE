package org.baratinage.ui.plot;

import org.jfree.chart.JFreeChart;

public interface IPlot {
  public JFreeChart getChart();

  public void restoreAutoBounds();

  public IPlot getCopy();
}
