package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;
import org.jfree.svg.SVGGraphics2D;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class PlotContainer extends RowColPanel {

    Plot plot;
    JFreeChart chart;
    ChartPanel chartPanel;
    boolean logYaxis;
    Color backgroundColor = Color.WHITE;

    public PlotContainer(Plot plot) {
        this(plot, true);
    }

    public PlotContainer(Plot plot, boolean toolbar) {
        super(AXIS.COL);

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

        RowColPanel topPanel = new RowColPanel();

        if (toolbar) {
            appendChild(topPanel, 0);
        }

        appendChild(chartPanel, 1);

        RowColPanel toolsPanel = new RowColPanel(AXIS.ROW, ALIGN.START);
        RowColPanel actionPanel = new RowColPanel(AXIS.ROW, ALIGN.END);

        topPanel.appendChild(toolsPanel, 1);
        topPanel.appendChild(actionPanel, 1);

        logYaxis = false;
        JButton toggleYaxisLogButton = new JButton(logYaxis ? "Axe Y linéaire" : "Axe Y logarithmique");

        toolsPanel.appendChild(toggleYaxisLogButton, 0);
        toggleYaxisLogButton.addActionListener((e) -> {
            logYaxis = !logYaxis;
            Lg.updateRegisteredObject(toggleYaxisLogButton);
            plot.setAxisLogY(logYaxis);
            chartPanel.restoreAutoBounds();
        });

        String saveIconPath = Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "feather", "save.svg").toString();
        String copyIconPath = Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "feather", "copy.svg").toString();
        String windowPlotIconPath = Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "feather", "external-link.svg")
                .toString();
        ImageIcon saveIcon = SvgIcon.buildNoScalingIcon(saveIconPath, 24);
        ImageIcon copyIcon = SvgIcon.buildNoScalingIcon(copyIconPath, 24);
        ImageIcon windowPlotIcon = SvgIcon.buildNoScalingIcon(windowPlotIconPath, 24);

        JButton btnWindowPlot = new JButton();

        btnWindowPlot.setIcon(windowPlotIcon);
        btnWindowPlot.addActionListener((e) -> {
            windowPlot();
        });

        JButton btnSaveAsSvg = new JButton();

        btnSaveAsSvg.setIcon(saveIcon);
        btnSaveAsSvg.setText("SVG");
        btnSaveAsSvg.addActionListener((e) -> {
            saveAsSvg();
        });

        JButton btnSaveAsPng = new JButton();

        btnSaveAsPng.setIcon(saveIcon);
        btnSaveAsPng.setText("PNG");
        btnSaveAsPng.addActionListener((e) -> {
            saveAsPng();
        });

        JButton btnCopyToClipboard = new JButton();

        btnCopyToClipboard.setIcon(copyIcon);
        btnCopyToClipboard.addActionListener((e) -> {
            copyToClipboard();
        });

        actionPanel.appendChild(btnWindowPlot);
        actionPanel.appendChild(btnSaveAsSvg);
        actionPanel.appendChild(btnSaveAsPng);
        actionPanel.appendChild(btnCopyToClipboard);

        JPopupMenu popupMenu = new JPopupMenu();

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

        this.chartPanel.setPopupMenu(popupMenu);

        setBackground(backgroundColor);
        topPanel.setBackground(backgroundColor);
        topPanel.setPadding(10);

        toolsPanel.setBackground(backgroundColor);
        actionPanel.setBackground(backgroundColor);
        toolsPanel.setGap(5);
        actionPanel.setGap(5);

        Lg.register(this, () -> {
            toggleYaxisLogButton.setText(logYaxis ? Lg.text("linear_y_axis") : Lg.text("log_y_axis"));
            btnWindowPlot.setToolTipText(Lg.text("window_plot"));
            btnSaveAsSvg.setText("SVG");
            btnSaveAsSvg.setToolTipText(Lg.text("to_svg"));
            btnSaveAsPng.setText("PNG");
            btnSaveAsPng.setToolTipText(Lg.text("to_png"));
            btnCopyToClipboard.setToolTipText(Lg.text("to_clipboard"));
            menuWindowPlot.setText(Lg.text("window_plot"));
            menuSaveSvg.setText(Lg.text("to_svg"));
            menuSavePng.setText(Lg.text("to_png"));
            menuCopyClipboard.setText(Lg.text("to_clipboard"));
        });

    }

    private String getSvgXML() {
        Dimension dim = getSize();
        SVGGraphics2D svg2d = new SVGGraphics2D(dim.width, dim.height);
        chart.draw(svg2d, new Rectangle2D.Double(0, 0, dim.width, dim.height));
        String svgElement = svg2d.getSVGElement();
        return svgElement;
    }

    private byte[] getImageBytes() {
        Dimension dim = getSize();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int scale = 3; // chart will be rendered at thrice the resolution.
        try {
            ChartUtils.writeScaledChartAsPNG(bout, chart, dim.width, dim.height, scale, scale);
        } catch (IOException e) {
            System.err.println("PlotContainer Error: \n" + e);
        }
        return bout.toByteArray();
    }

    // FIXME: refactorization needed!
    public void saveAsSvg() {
        if (chart == null)
            return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        Lg.text("svg_format"),
                        "svg"));

        fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file == null)
            return;
        if (file.exists()) {
            int response = JOptionPane.showConfirmDialog(this,
                    Lg.text("file_already_exists_overwrite"),
                    Lg.text("overwrite_file"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String filePath = file.toString();
        if (!file.getName().toLowerCase().endsWith(".svg")) {
            filePath += ".svg";
        }

        saveToSvg(filePath);
    }

    public void saveAsPng() {
        if (chart == null)
            return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        Lg.text("png_format"),
                        "png"));

        fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file == null)
            return;
        if (file.exists()) {
            int response = JOptionPane.showConfirmDialog(this,
                    Lg.text("file_already_exists_overwrite"),
                    Lg.text("overwrite_file"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String filePath = file.toString();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            filePath += ".png";
        }

        saveToPng(filePath);
    }

    public void saveToSvg(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath));
            String svg = getSvgXML();
            fileWriter.write(svg);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("PlotContainer Error: \n" + e);
        }
    }

    public void saveToPng(String filePath) {
        try {
            Files.write(Path.of(filePath), getImageBytes());
        } catch (IOException e) {
            System.err.println("PlotContainer Error: \n" + e);
        }
    }

    public void copyToClipboard() {
        if (chart == null)
            return;
        chartPanel.doCopy();
    }

    public void windowPlot() {
        JFrame f = new JFrame();
        PlotContainer p = new PlotContainer(plot);

        f.setPreferredSize(new Dimension(1000, 500));
        f.setIconImage(AppConfig.AC.APP_MAIN_FRAME.getIconImage());
        f.add(p);
        f.pack();
        f.setVisible(true);

    }

}
