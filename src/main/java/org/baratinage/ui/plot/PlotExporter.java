package org.baratinage.ui.plot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

public class PlotExporter {

  public static interface ExportablePlot {
    public ExportablePlot getCopy();

    public JPanel getPanel();

    public String getSvgString();

    public BufferedImage getBufferedImage();
  }

  public static SimpleFlowPanel buildExportPanel(ExportablePlot plot) {

    JButton btnWindowPlot = new JButton();
    btnWindowPlot.setIcon(AppSetup.ICONS.EXTERNAL);
    btnWindowPlot.addActionListener((e) -> {
      windowPlot(plot);
    });

    JButton btnSaveAsSvg = new JButton();
    btnSaveAsSvg.setIcon(AppSetup.ICONS.SAVE);
    btnSaveAsSvg.setText("SVG");
    btnSaveAsSvg.addActionListener((e) -> {
      saveAsSvg(plot);
    });

    JButton btnSaveAsPng = new JButton();
    btnSaveAsPng.setIcon(AppSetup.ICONS.SAVE);
    btnSaveAsPng.setText("PNG");
    btnSaveAsPng.addActionListener((e) -> {
      saveAsPng(plot);
    });

    JButton btnCopyToClipboard = new JButton();
    btnCopyToClipboard.setIcon(AppSetup.ICONS.COPY);
    btnCopyToClipboard.addActionListener((e) -> {
      copyToClipboard(plot.getBufferedImage());
    });

    SimpleFlowPanel panel = new SimpleFlowPanel();
    panel.setGap(5);
    panel.addChild(btnWindowPlot, false);
    panel.addChild(btnSaveAsSvg, false);
    panel.addChild(btnSaveAsPng, false);
    panel.addChild(btnCopyToClipboard, false);

    T.t(plot, () -> {
      btnWindowPlot.setToolTipText(T.text("window_plot"));
      btnSaveAsSvg.setText("SVG");
      btnSaveAsSvg.setToolTipText(T.text("to_svg"));
      btnSaveAsPng.setText("PNG");
      btnSaveAsPng.setToolTipText(T.text("to_png"));
      btnCopyToClipboard.setToolTipText(T.text("to_clipboard"));
    });

    return panel;
  }

  public static JPopupMenu buildExportPopupMenu(ExportablePlot plot) {

    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem menuWindowPlot = new JMenuItem();
    menuWindowPlot.setIcon(AppSetup.ICONS.EXTERNAL);
    menuWindowPlot.addActionListener((e) -> {
      windowPlot(plot);
    });
    popupMenu.add(menuWindowPlot);

    JMenuItem menuSaveSvg = new JMenuItem();
    menuSaveSvg.setIcon(AppSetup.ICONS.SAVE);
    menuSaveSvg.addActionListener((e) -> {
      saveAsSvg(plot);
    });
    popupMenu.add(menuSaveSvg);

    JMenuItem menuSavePng = new JMenuItem();
    menuSavePng.setIcon(AppSetup.ICONS.SAVE);
    menuSavePng.addActionListener((e) -> {
      saveAsPng(plot);
    });
    popupMenu.add(menuSavePng);

    JMenuItem menuCopyClipboard = new JMenuItem();
    menuCopyClipboard.setIcon(AppSetup.ICONS.COPY);
    menuCopyClipboard.addActionListener((e) -> {
      copyToClipboard(plot.getBufferedImage());
    });
    popupMenu.add(menuCopyClipboard);

    T.t(plot, () -> {
      menuWindowPlot.setText(T.text("window_plot"));
      menuSaveSvg.setText(T.text("to_svg"));
      menuSavePng.setText(T.text("to_png"));
      menuCopyClipboard.setText(T.text("to_clipboard"));
    });

    return popupMenu;
  }

  public static BufferedImage buildImgFromChart(
      JFreeChart chart,
      int width,
      int height,
      int widthScaleFactor,
      int heightScaleFactor) {
    double desiredWidth = width * widthScaleFactor;
    double desiredHeight = height * heightScaleFactor;
    double defaultWidth = width;
    double defaultHeight = height;
    boolean scale = false;

    if ((widthScaleFactor != 1) || (heightScaleFactor != 1)) {
      scale = true;
    }

    double scaleX = desiredWidth / defaultWidth;
    double scaleY = desiredHeight / defaultHeight;

    BufferedImage image = new BufferedImage((int) desiredWidth,
        (int) desiredHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();

    if (scale) {
      AffineTransform saved = g2.getTransform();
      g2.transform(AffineTransform.getScaleInstance(scaleX, scaleY));
      chart.draw(g2, new Rectangle2D.Double(0, 0, defaultWidth,
          defaultHeight), null, null);
      g2.setTransform(saved);
      g2.dispose();
    } else {
      chart.draw(g2, new Rectangle2D.Double(0, 0, defaultWidth,
          defaultHeight), null, null);
    }

    return image;
  }

  private static void windowPlot(ExportablePlot plot) {
    ExportablePlot ep = plot.getCopy();

    JFrame f = new JFrame();
    f.add(ep.getPanel());

    f.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        T.clear(f);
      }
    });

    f.setPreferredSize(new Dimension(1000, 500));
    f.setIconImage(AppSetup.MAIN_FRAME.getIconImage());

    f.pack();
    f.setVisible(true);
  }

  private static void saveAsSvg(ExportablePlot plot) {
    String svgString = plot.getSvgString();
    File f = CommonDialog.saveFileDialog(
        null,
        T.text("svg_format"),
        "svg");
    if (f == null) {
      ConsoleLogger.error("cannot save to SVG, selected file is null.");
      return;
    }
    saveToSvg(svgString, f.getAbsolutePath());
  }

  private static void saveAsPng(ExportablePlot plot) {
    BufferedImage img = plot.getBufferedImage();
    File f = CommonDialog.saveFileDialog(
        null,
        T.text("png_format"),
        "png");

    if (f == null) {
      ConsoleLogger.error("cannot save to PNG, selected file is null.");
      return;
    }
    saveToPng(img, f.getAbsolutePath());
  }

  public static void saveToSvg(String svgString, String filePath) {
    try {
      FileWriter fileWriter = new FileWriter(new File(filePath), StandardCharsets.UTF_8);
      fileWriter.write(svgString);
      fileWriter.close();
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
  }

  public static void saveToPng(BufferedImage img, String filePath) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      bytes.write(ChartUtils.encodeAsPNG(img));
      Files.write(Path.of(filePath), bytes.toByteArray());
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
  }

  public static void copyToClipboard(BufferedImage img) {
    ImageTransferable chartTransferable = new ImageTransferable(img);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(chartTransferable, null);
  }

  private static class ImageTransferable implements Transferable {

    private BufferedImage image;

    public ImageTransferable(BufferedImage image) {
      this.image = image;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return DataFlavor.imageFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (isDataFlavorSupported(flavor)) {
        return image;
      } else {
        throw new UnsupportedFlavorException(flavor);
      }
    }

  }

}
