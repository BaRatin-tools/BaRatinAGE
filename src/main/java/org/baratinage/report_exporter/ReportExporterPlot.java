package org.baratinage.report_exporter;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;

public class ReportExporterPlot {
  public final String id;
  public final String name;
  public final String svg;
  public final BufferedImage png;

  public ReportExporterPlot(String id, String name, IExportablePlot plotContainer) {
    this(id, name, plotContainer, 900, 500);
  }

  public ReportExporterPlot(String id, String name, IExportablePlot plotContainer, int width, int height) {
    this.id = id;
    this.name = name;
    Dimension dim = new Dimension(width, height);
    svg = plotContainer.getSvgString(dim);
    png = plotContainer.getBufferedImage(dim, 2);
  }

  public String getMarkdownPNG(String assetDirName) {
    return "![%s](%s)".formatted(name, "./%s/%s.png".formatted(assetDirName, id));
  }

  public String getMarkdownSVG(String assetDirName) {
    return "![%s](%s)".formatted(name, "./%s/%s.svg".formatted(assetDirName, id));
  }
}
