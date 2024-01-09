package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class PlotContainer extends RowColPanel {

    private Plot plot;
    private JFreeChart chart;
    private RowColPanel chartPanelContainer;
    private ChartPanel chartPanel;
    // private boolean logYaxis;
    private Color backgroundColor = Color.WHITE;
    // private JButton toggleYaxisLogButton;

    private final JPopupMenu popupMenu;
    private final RowColPanel toolsPanel;
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

        // logYaxis = false;
        // toggleYaxisLogButton = new JButton();

        // toolsPanel.appendChild(toggleYaxisLogButton, 0);
        // toggleYaxisLogButton.addActionListener((e) -> {
        // toggleYLogAxis();
        // });

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

        setBackground(backgroundColor);
        topPanel.setBackground(backgroundColor);
        topPanel.setPadding(10);

        toolsPanel.setBackground(backgroundColor);
        actionPanel.setBackground(backgroundColor);
        toolsPanel.setGap(5);
        actionPanel.setGap(5);

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

            // toggleYaxisLogButton.setText(
            // logYaxis ? T.text("linear_y_axis") : T.text("log_y_axis"));
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
                if (domainBounds != null) {
                    plot.plot.getDomainAxis().setRange(domainBounds);
                }
                if (domainBounds != null) {
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

    // private void toggleYLogAxis() {
    // logYaxis = !logYaxis;
    // if (plot != null) {
    // plot.setAxisLogY(logYaxis);
    // chartPanel.restoreAutoBounds();
    // T.updateTranslation(this);
    // }
    // }

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

    private byte[] getImageBytes() {
        if (chart == null) {
            return new byte[0];
        }
        Dimension dim = getSize();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int scale = 3; // chart will be rendered at thrice the resolution.
        try {
            ChartUtils.writeScaledChartAsPNG(bout, chart, dim.width, dim.height, scale, scale);
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return bout.toByteArray();
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
            FileWriter fileWriter = new FileWriter(new File(filePath));
            String svg = getSvgXML();
            fileWriter.write(svg);
            fileWriter.close();
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    public void saveToPng(String filePath) {
        try {
            Files.write(Path.of(filePath), getImageBytes());
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    public void copyToClipboard() {
        if (chart == null)
            return;
        chartPanel.doCopy();
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
