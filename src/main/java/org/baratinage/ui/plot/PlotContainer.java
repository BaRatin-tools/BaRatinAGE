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
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;
import org.jfree.svg.SVGGraphics2D;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class PlotContainer extends RowColPanel {

    private Plot plot;
    private JFreeChart chart;
    private RowColPanel chartPanelContainer;
    private ChartPanel chartPanel;

    private final JPopupMenu popupMenu;
    public final RowColPanel toolsPanel;
    private final RowColPanel actionPanel;

    public PlotContainer() {
        this(true);
    }

    public PlotContainer(Plot plot) {
        this(plot, true);
    }

    public PlotContainer(Plot plot, boolean toolbar) {
        this(toolbar);
        setPlot(plot);
    }

    public PlotContainer(boolean toolbar) {
        super(AXIS.COL);

        RowColPanel topPanel = new RowColPanel();

        if (toolbar) {
            appendChild(topPanel, 0);
        }

        chartPanelContainer = new RowColPanel();
        appendChild(chartPanelContainer, 1);

        toolsPanel = new RowColPanel(AXIS.ROW, ALIGN.START);
        actionPanel = new RowColPanel(AXIS.ROW, ALIGN.END);

        topPanel.appendChild(toolsPanel, 1);
        topPanel.appendChild(actionPanel, 1);

        SimpleCheckbox cbShowLegend = new SimpleCheckbox();
        cbShowLegend.setSelected(true);
        cbShowLegend.addChangeListener((e) -> {
            if (plot != null) {
                plot.setIncludeLegend(cbShowLegend.isSelected());
            }
        });

        JButton btnWindowPlot = new JButton();
        btnWindowPlot.setIcon(AppSetup.ICONS.EXTERNAL);
        btnWindowPlot.addActionListener((e) -> {
            windowPlot();
        });

        JButton btnSaveAsSvg = new JButton();
        btnSaveAsSvg.setIcon(AppSetup.ICONS.SAVE);
        btnSaveAsSvg.setText("SVG");
        btnSaveAsSvg.addActionListener((e) -> {
            saveAsSvg();
        });

        JButton btnSaveAsPng = new JButton();
        btnSaveAsPng.setIcon(AppSetup.ICONS.SAVE);
        btnSaveAsPng.setText("PNG");
        btnSaveAsPng.addActionListener((e) -> {
            saveAsPng();
        });

        JButton btnCopyToClipboard = new JButton();
        btnCopyToClipboard.setIcon(AppSetup.ICONS.COPY);
        btnCopyToClipboard.addActionListener((e) -> {
            copyToClipboard();
        });

        toolsPanel.appendChild(cbShowLegend);
        actionPanel.appendChild(btnWindowPlot);
        actionPanel.appendChild(btnSaveAsSvg);
        actionPanel.appendChild(btnSaveAsPng);
        actionPanel.appendChild(btnCopyToClipboard);

        popupMenu = new JPopupMenu();
        JMenuItem menuWindowPlot = new JMenuItem();
        menuWindowPlot.addActionListener((e) -> {
            windowPlot();
        });
        popupMenu.add(menuWindowPlot);

        JMenuItem menuSaveSvg = new JMenuItem();
        menuSaveSvg.addActionListener((e) -> {
            saveAsSvg();
        });
        popupMenu.add(menuSaveSvg);

        JMenuItem menuSavePng = new JMenuItem();
        menuSavePng.addActionListener((e) -> {
            saveAsPng();
        });
        popupMenu.add(menuSavePng);
        JMenuItem menuCopyClipboard = new JMenuItem();
        menuCopyClipboard.addActionListener((e) -> {
            copyToClipboard();
        });
        popupMenu.add(menuCopyClipboard);

        topPanel.setPadding(10);

        toolsPanel.setGap(5);
        actionPanel.setGap(5);

        T.t(this, () -> {
            cbShowLegend.setText(T.text("show_legend"));
        });
        T.t(this, menuWindowPlot, false, "window_plot");
        T.t(this, menuSaveSvg, false, "to_svg");
        T.t(this, menuSavePng, false, "to_png");
        T.t(this, menuCopyClipboard, false, "to_clipboard");

        T.t(this, () -> {
            btnWindowPlot.setToolTipText(T.text("window_plot"));
            btnSaveAsSvg.setText("SVG");
            btnSaveAsSvg.setToolTipText(T.text("to_svg"));
            btnSaveAsPng.setText("PNG");
            btnSaveAsPng.setToolTipText(T.text("to_png"));
            btnCopyToClipboard.setToolTipText(T.text("to_clipboard"));
        });

    }

    public void setPlot(Plot plot) {
        this.plot = plot;
        chart = plot.getChart();

        // modified restoreAutoBounds methods to ignore
        // dataset marked to be ignored in Plot.
        chartPanel = new ChartPanel(chart) {
            @Override
            public void restoreAutoBounds() {
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                Range domainBounds = plot.getDomainBounds();
                Range rangeBounds = plot.getRangeBounds();
                // FIXME: this approach is not ideal but I don't have any clever idea to fixe
                // the issue...
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

        };

        chartPanel.restoreAutoBounds();

        chartPanel.setMinimumDrawWidth(100);
        chartPanel.setMinimumDrawHeight(100);

        chartPanel.setMaximumDrawWidth(10000);
        chartPanel.setMaximumDrawHeight(10000);

        chartPanel.setPopupMenu(popupMenu);

        chartPanelContainer.clear();
        chartPanelContainer.appendChild(chartPanel, 1);

    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public Plot getPlot() {
        return plot;
    }

    private String getSvgXML() {
        if (chart == null) {
            return "";
        }
        Dimension dim = getSize();
        SVGGraphics2D svg2d = new SVGGraphics2D(dim.width, dim.height);
        chart.draw(svg2d, new Rectangle2D.Double(0, 0, dim.width, dim.height));
        String svgElement = svg2d.getSVGElement();
        return svgElement;
    }

    // FIXME: refactorization needed!
    public void saveAsSvg() {
        if (chart == null)
            return;
        File f = CommonDialog.saveFileDialog(
                null,
                T.text("svg_format"),
                "svg");

        if (f == null) {
            ConsoleLogger.error("cannot save to SVG, selected file is null.");
            return;
        }

        saveToSvg(f.getAbsolutePath());
    }

    public void saveAsPng() {
        if (chart == null)
            return;
        File f = CommonDialog.saveFileDialog(
                null,
                T.text("png_format"),
                "png");

        if (f == null) {
            ConsoleLogger.error("cannot save to PNG, selected file is null.");
            return;
        }
        saveToPng(f.getAbsolutePath());
    }

    public void saveToSvg(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath), StandardCharsets.UTF_8);
            String svg = getSvgXML();
            fileWriter.write(svg);
            fileWriter.close();
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    public void saveToPng(String filePath) {
        try {
            Dimension dim = getSize();
            BufferedImage img = createBufferedImage(chart,
                    dim.width, dim.height,
                    2, 2);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bytes.write(ChartUtils.encodeAsPNG(img));

            Files.write(Path.of(filePath), bytes.toByteArray());
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    public void copyToClipboard() {
        if (chart == null)
            return;

        Dimension dim = getSize();
        BufferedImage img = createBufferedImage(chart,
                dim.width, dim.height,
                1, 1);
        ImageTransferable chartTransferable = new ImageTransferable(img);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(chartTransferable, null);
    }

    private BufferedImage createBufferedImage(
            JFreeChart chart, int width, int height, int widthScaleFactor,
            int heightScaleFactor) {
        double desiredWidth = width * widthScaleFactor;
        double desiredHeight = height * heightScaleFactor;
        double defaultWidth = width;
        double defaultHeight = height;
        boolean scale = false;

        // get desired width and height from somewhere then...
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

    public void windowPlot() {
        if (chart == null)
            return;
        JFrame f = new JFrame();
        PlotContainer p = new PlotContainer(plot.getCopy());

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                T.clear(f);
            }
        });

        f.setPreferredSize(new Dimension(1000, 500));
        f.setIconImage(AppSetup.MAIN_FRAME.getIconImage());
        f.add(p);
        f.pack();
        f.setVisible(true);
    }

}
