package org.baratinage.ui.plot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.svg.SVGGraphics2D;

public class MultiPlotContainer extends SimpleFlowPanel implements IExportablePlot {

  private static record PlotConfig(Plot plot, CustomChartPanel chart, float weight) {

  }

  private final List<PlotConfig> plots;

  private final SimpleFlowPanel topPanel;
  private final SimpleFlowPanel bottomPanel;
  private final SimpleFlowPanel plotPanel;

  public final SimpleFlowPanel topLeftPanel;
  public final SimpleFlowPanel topRightPanel;
  public final SimpleFlowPanel bottomLeftPanel;
  public final SimpleFlowPanel bottomRightPanel;

  private final JPopupMenu popupMenu;

  private final String id = Misc.getTimeStampedId();

  public MultiPlotContainer() {
    super(true);
    setGap(5);
    setPadding(5);

    boolean isVertical = true;

    plots = new ArrayList<>();

    topPanel = new SimpleFlowPanel();
    bottomPanel = new SimpleFlowPanel();
    plotPanel = new SimpleFlowPanel(isVertical);

    topLeftPanel = new SimpleFlowPanel();
    topRightPanel = PlotExporter.buildExportPanel(this);
    bottomLeftPanel = new SimpleFlowPanel();
    bottomRightPanel = new SimpleFlowPanel();

    topPanel.addChild(topLeftPanel, false);
    topPanel.addExtensor();
    topPanel.addChild(topRightPanel, false);

    bottomPanel.addChild(bottomLeftPanel, false);
    bottomPanel.addExtensor();
    bottomPanel.addChild(bottomRightPanel, false);

    addChild(topPanel, false);
    addChild(plotPanel, true);
    addChild(bottomPanel, false);

    popupMenu = PlotExporter.buildExportPopupMenu(this);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        TimedActions.throttle(id + "_resized", 50, () -> {
          updateMargins();
        });
      }
    });

  }

  Range currentDomainBounds = null;
  Range targetDomainBounds = null;
  boolean listenersDisabled = false;

  public void addPlot(Plot plot, float weight) {
    CustomChartPanel chart = new CustomChartPanel(plot);
    chart.setPopupMenu(popupMenu);
    plots.add(new PlotConfig(plot, chart, weight));

    plotPanel.addChild(chart, weight);

    updateDomainBounds(getMaxDomainBounds());

    plot.getChart().addChangeListener((l) -> {
      if (listenersDisabled) {
        return;
      }
      TimedActions.throttle(id + "_plot_changed", 50, () -> {
        listenersDisabled = true;
        updatePlotDomaineBounds(plot);
        updateMargins();
        listenersDisabled = false;
      });
    });

    updateMargins();

    TimedActions.throttle(id + "_fixing_misalignment", 1000, () -> {
      updateMargins();
    });

  }

  private void updatePlotDomaineBounds(Plot plot) {
    Range newDomainBounds = plot.plot.getDomainAxis().getRange();
    updateDomainBounds(newDomainBounds);

    double legendWidth = 0;
    for (PlotConfig p : plots) {
      LegendTitle t = p.plot.getChart().getLegend();
      if (t == null) {
        continue;
      }
      double w = t.getBounds().getWidth();
      if (w > legendWidth) {
        legendWidth = w;
      }
    }
  }

  // ****************************************************************
  // handle plot resizing and possible adjustement in legends and axis
  // that need to be reflected in all plots to keep them in sync

  private void updateMargins() {
    Insets maxInsets = new Insets(0, 0, 0, 0);
    List<Insets> insets = new ArrayList<>();
    List<Insets> paddings = new ArrayList<>();
    for (PlotConfig p : plots) {
      Insets chartInsets = p.chart.getMaxInsets();
      Insets chartPadding = p.chart.getPadding();
      Insets paddingFreeInsets = getDiffInsets(chartPadding, chartInsets);
      Insets paddingFreeInsetsTruncated = truncateAtZero(paddingFreeInsets);
      insets.add(paddingFreeInsetsTruncated);
      paddings.add(chartPadding);
      maxInsets = getMaxInsets(maxInsets, paddingFreeInsetsTruncated);
    }
    for (int k = 0; k < plots.size(); k++) {
      PlotConfig p = plots.get(k);
      Insets plotInsets = insets.get(k);
      Insets neededCorrection = getDiffInsets(plotInsets, maxInsets);
      p.chart.setPadding(neededCorrection);
    }
  }

  private static Insets getMaxInsets(Insets insets1, Insets insets2) {
    int top = Math.max(insets1.top, insets2.top);
    int left = Math.max(insets1.left, insets2.left);
    int bottom = Math.max(insets1.bottom, insets2.bottom);
    int right = Math.max(insets1.right, insets2.right);
    return new Insets(top, left, bottom, right);
  }

  private static Insets getDiffInsets(Insets small, Insets large) {
    int top = large.top - small.top;
    int left = large.left - small.left;
    int bottom = large.bottom - small.bottom;
    int right = large.right - small.right;
    return new Insets(top, left, bottom, right);
  }

  private static Insets truncateAtZero(Insets insets) {
    return new Insets(
        Math.max(0, insets.top),
        Math.max(0, insets.left),
        Math.max(0, insets.bottom),
        Math.max(0, insets.right));
  }

  // ****************************************************************
  // make domain axis bounds match

  public Range getCurrentDomainRange() {
    if (plots.size() == 0) {
      return new Range(0, 0);
    }
    return plots.get(0).plot.plot.getDomainAxis().getRange();
  }

  public void setDomainRange(Range range) {
    updateDomainBounds(range);
  }

  private Range getMaxDomainBounds() {
    double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
    for (PlotConfig p : plots) {
      Range r = p.plot.plot.getDomainAxis().getRange();
      if (r.getLowerBound() < min) {
        min = r.getLowerBound();
      }
      if (r.getUpperBound() > max) {
        max = r.getUpperBound();
      }
    }
    return new Range(min, max);
  }

  private void updateDomainBounds(Range targetRange) {
    targetDomainBounds = targetRange;
    for (PlotConfig p : plots) {
      Range r = p.plot.plot.getDomainAxis().getRange();
      if (!r.equals(targetRange)) {
        p.plot.plot.getDomainAxis().setRange(targetRange);
      }
    }
    currentDomainBounds = targetRange;
  }

  // ****************************************************************
  // PlotExporter methods

  @Override
  public String getSvgString() {
    float totalWeight = 0;
    for (PlotConfig p : plots) {
      totalWeight += p.weight;
    }
    Dimension d = getSize();
    SVGGraphics2D svg2d = new SVGGraphics2D(d.width, d.height);
    int currentHeight = 0;
    for (PlotConfig p : plots) {
      int h = (int) (d.height * p.weight / totalWeight);
      p.chart.getChart().draw(
          svg2d, new Rectangle2D.Double(
              0, currentHeight,
              d.width, h));
      currentHeight += h;
    }
    return svg2d.getSVGElement();
  }

  @Override
  public BufferedImage getBufferedImage() {
    float totalWeight = 0;
    for (PlotConfig p : plots) {
      totalWeight += p.weight;
    }

    Dimension d = getSize();
    int scale = 2;
    List<BufferedImage> images = new ArrayList<>();
    for (PlotConfig p : plots) {
      BufferedImage img = PlotExporter.buildImgFromChart(
          p.chart.getChart(),
          d.width,
          (int) (d.height * p.weight / totalWeight),
          scale,
          scale);
      images.add(img);
    }
    BufferedImage combined = new BufferedImage(
        d.width * scale,
        d.height * scale,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = combined.createGraphics();
    int currentHeight = 0;
    for (BufferedImage img : images) {
      g2d.drawImage(img, 0, currentHeight, null);
      currentHeight += img.getHeight();
    }
    g2d.dispose();
    return combined;
  }

  @Override
  public JPanel getPanel() {
    return this;
  }

  @Override
  public IExportablePlot getCopy() {
    MultiPlotContainer container = new MultiPlotContainer();
    for (PlotConfig p : plots) {
      container.addPlot(p.plot.getCopy(), p.weight);
    }
    return container;
  }

}
