package org.baratinage.report_exporter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.RepaintManager;

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
    if (plotContainer.isPlotValid()) {
      svg = plotContainer.getSvgString(dim);
      png = plotContainer.getBufferedImage(dim, 2);
    } else {
      svg = "";
      png = createNoImageIcon();
    }
  }

  public ReportExporterPlot(String id, String name, BufferedImage image) {
    this.id = id;
    this.name = name;
    svg = "";
    png = image;
  }

  public String getMarkdownPNG(String assetDirName) {
    return "![%s](%s)".formatted(name, "./%s/%s.png".formatted(assetDirName, id));
  }

  public String getMarkdownSVG(String assetDirName) {
    return "![%s](%s)".formatted(name, "./%s/%s.svg".formatted(assetDirName, id));
  }

  private static BufferedImage createNoImageIcon() {
    int size = 16;

    BufferedImage img = new BufferedImage(
        size, size, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g = img.createGraphics();

    // Enable better line rendering
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    // Optional: clear background (transparent)
    g.setComposite(AlphaComposite.Clear);
    g.fillRect(0, 0, size, size);
    g.setComposite(AlphaComposite.SrcOver);

    // Draw red cross
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(2f));

    g.drawLine(2, 2, size - 3, size - 3);
    g.drawLine(size - 3, 2, 2, size - 3);

    g.dispose();
    return img;
  }

  public static BufferedImage createImage(JPanel panel, double scale)
      throws Exception {

    final BufferedImage[] result = new BufferedImage[1];

    Dimension size = panel.getPreferredSize();
    panel.setSize(size);
    panel.setOpaque(false);

    BufferedImage image = new BufferedImage(
        (int) (size.width * scale),
        (int) (size.height * scale),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2 = image.createGraphics();

    // Clear background (transparent)
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, image.getWidth(), image.getHeight());
    g2.setComposite(AlphaComposite.SrcOver);

    // High quality
    g2.scale(scale, scale);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

    // Disable double buffering
    RepaintManager rm = RepaintManager.currentManager(panel);
    boolean db = rm.isDoubleBufferingEnabled();
    rm.setDoubleBufferingEnabled(false);

    panel.revalidate();
    panel.doLayout();
    panel.printAll(g2);

    rm.setDoubleBufferingEnabled(db);
    g2.dispose();

    result[0] = image;

    return result[0];
  }

}
