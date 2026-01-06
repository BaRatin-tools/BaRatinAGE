package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.svg.SVGGraphics2D;

public class GridPlotContainer extends SimpleFlowPanel implements IExportablePlot {

  private final JPanel gridPlotPanel;
  private final int rows;
  private final int cols;
  private final Dimension minCellSize;

  private boolean toolbar;

  public final JToolBar toolsPanel;
  private final JToolBar actionPanel;

  private final List<IPlot> plots = new ArrayList<>();

  public GridPlotContainer(int rows, int cols) {
    this(rows, cols, true);
  }

  public GridPlotContainer(int rows, int cols, boolean toolbar) {
    this(rows, cols, new Dimension(10, 10), toolbar);
  }

  public GridPlotContainer(int rows, int cols, Dimension minCellSize, boolean toolbar) {
    super(true);
    if (rows <= 0 || cols <= 0) {
      throw new IllegalArgumentException("Rows and columns must be > 0");
    }
    if (minCellSize == null) {
      throw new IllegalArgumentException("minCellSize must not be null");
    }

    this.toolbar = toolbar;
    this.rows = rows;
    this.cols = cols;
    this.minCellSize = new Dimension(minCellSize);

    gridPlotPanel = new JPanel();
    gridPlotPanel.setLayout(new GridPlotLayout());
    gridPlotPanel.setBackground(Color.WHITE);

    SimpleFlowPanel topPanel = new SimpleFlowPanel();
    topPanel.setPadding(10);

    if (toolbar) {
      addChild(topPanel, false);
    }

    toolsPanel = new JToolBar();
    actionPanel = PlotExporter.buildExportToolBar(this);

    topPanel.addChild(toolsPanel, false);
    topPanel.addExtensor();
    topPanel.addChild(actionPanel, false);

    addChild(topPanel, false);
    addChild(gridPlotPanel, true);

  }

  public void addPlot(IPlot plot) {
    if (plot == null) {
      throw new IllegalArgumentException("chart must not be null");
    }
    if (plots.size() >= rows * cols) {
      throw new IllegalStateException("Grid is already full");
    }

    plots.add(plot);

    ChartPanel panel = new ChartPanel(plot.getChart(), false);
    panel.setMouseZoomable(false);
    panel.setPopupMenu(PlotExporter.buildExportPopupMenu(this));

    panel.setRefreshBuffer(true);
    panel.setMaximumDrawWidth(Integer.MAX_VALUE);
    panel.setMaximumDrawHeight(Integer.MAX_VALUE);
    panel.setMinimumDrawWidth(0);
    panel.setMinimumDrawHeight(0);

    plot.restoreAutoBounds();

    gridPlotPanel.add(panel);
    gridPlotPanel.revalidate();
    gridPlotPanel.repaint();

  }

  private class GridPlotLayout implements LayoutManager {

    @Override
    public void layoutContainer(Container parent) {
      Dimension cell = computeCellSize(parent.getSize());

      int i = 0;
      for (Component c : parent.getComponents()) {
        int r = i / cols;
        int col = i % cols;

        int x = col * cell.width;
        int y = r * cell.height;

        c.setBounds(x, y, cell.width, cell.height);
        i++;
      }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(
          cols * minCellSize.width,
          rows * minCellSize.height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
  }

  private Dimension computeCellSize(Dimension available) {
    int cellW = Math.max(minCellSize.width, available.width / cols);
    int cellH = Math.max(minCellSize.height, available.height / rows);
    return new Dimension(cellW, cellH);
  }

  @Override
  public IExportablePlot getCopy() {
    GridPlotContainer copy = new GridPlotContainer(rows, cols, minCellSize, toolbar);
    for (IPlot plot : plots) {
      copy.addPlot(plot);
    }
    return copy;
  }

  @Override
  public JPanel getPanel() {
    return this;
  }

  @Override
  public boolean isPlotValid() {
    return !plots.isEmpty();
  }

  @Override
  public BufferedImage getBufferedImage() {
    Dimension pref = gridPlotPanel.getSize();
    return getBufferedImage(pref, 2);
  }

  // @Override
  // public BufferedImage getBufferedImage(Dimension dim, int scale) {
  // BufferedImage img = new BufferedImage(
  // dim.width * scale,
  // dim.height * scale,
  // BufferedImage.TYPE_INT_ARGB);

  // Graphics2D g2 = img.createGraphics();
  // try {
  // g2.scale(scale, scale);
  // g2.setColor(Color.WHITE);
  // g2.fillRect(0, 0, dim.width, dim.height);
  // gridPlotPanel.setSize(dim);
  // gridPlotPanel.doLayout();
  // validate();
  // gridPlotPanel.paint(g2);
  // } finally {
  // g2.dispose();
  // }

  // return img;
  // }

  @Override
  public BufferedImage getBufferedImage(Dimension dim, int scale) {

    // Final image size in pixels
    int imgW = dim.width * scale;
    int imgH = dim.height * scale;

    BufferedImage image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);

    Graphics2D g2 = image.createGraphics();
    try {
      // White background
      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, imgW, imgH);

      // Apply global scaling once
      if (scale != 1) {
        g2.scale(scale, scale);
      }

      // Compute cell size in "logical" coordinates
      Dimension cell = computeCellSize(dim);

      int index = 0;
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {

          if (index >= plots.size()) {
            return image;
          }

          IPlot plot = plots.get(index++);
          JFreeChart chart = plot.getChart();

          Graphics2D gChild = (Graphics2D) g2.create();
          try {
            gChild.translate(c * cell.width, r * cell.height);

            chart.draw(
                gChild,
                new Rectangle2D.Double(
                    0,
                    0,
                    cell.width,
                    cell.height),
                null,
                null);
          } finally {
            gChild.dispose();
          }
        }
      }
    } finally {
      g2.dispose();
    }

    return image;
  }

  @Override
  public String getSvgString() {
    return getSvgString(getSize());
  }

  @Override
  public String getSvgString(Dimension dim) {
    SVGGraphics2D g2 = new SVGGraphics2D(dim.width, dim.height);

    g2.setColor(Color.WHITE);
    g2.fillRect(0, 0, dim.width, dim.height);

    Dimension cell = computeCellSize(dim);

    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (index >= plots.size()) {
          break;
        }

        IPlot plot = plots.get(index++);
        JFreeChart chart = plot.getChart();
        Graphics2D gChild = (Graphics2D) g2.create();
        try {
          gChild.translate(c * cell.width, r * cell.height);
          chart.draw(
              gChild,
              new Rectangle(0, 0, cell.width, cell.height));
        } finally {
          gChild.dispose();
        }
      }
    }

    return g2.getSVGElement();
  }
}
