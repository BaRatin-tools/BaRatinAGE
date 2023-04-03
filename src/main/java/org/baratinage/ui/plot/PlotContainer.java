package org.baratinage.ui.plot;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.svg.SVGGraphics2D;

import org.baratinage.ui.component.NoScalingIcon;
// import org.baratinage.ui.container.FlexPanel;
import org.baratinage.ui.container.RowColPanel;

public class PlotContainer extends RowColPanel {

    Plot plot;
    JFreeChart chart;
    ChartPanel chartPanel;
    boolean logYaxis;

    public PlotContainer(Plot plot) {
        super(AXIS.COL);
        this.setBackground(Color.WHITE);

        this.plot = plot;
        this.chart = plot.getChart();
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setMinimumDrawWidth(100);
        this.chartPanel.setMinimumDrawHeight(100);

        this.chartPanel.setMaximumDrawWidth(10000);
        this.chartPanel.setMaximumDrawHeight(10000);

        RowColPanel topPanel = new RowColPanel();

        this.appendChild(topPanel, 0);
        this.appendChild(chartPanel, 1);

        RowColPanel toolsPanel = new RowColPanel(AXIS.ROW, ALIGN.START);
        RowColPanel actionPanel = new RowColPanel(AXIS.ROW, ALIGN.END);

        topPanel.appendChild(toolsPanel, 1);
        topPanel.appendChild(actionPanel, 1);

        logYaxis = false;
        JButton toggleYaxisLogButton = new JButton(logYaxis ? "Axe Y linéaire" : "Axe Y logarithmique");
        toolsPanel.appendChild(toggleYaxisLogButton, 0);
        toggleYaxisLogButton.addActionListener((e) -> {
            logYaxis = !logYaxis;
            toggleYaxisLogButton.setText(logYaxis ? "Axe Y linéaire" : "Axe Y logarithmique");
            plot.setAxisLogY(logYaxis);
        });

        // FIXME: use lambda instead
        PlotContainer that = this;
        AbstractAction saveAsSvgAction = new AbstractAction("Save As SVG") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (that.chartPanel != null) {
                    // String svg = that.chart.getSvgXML();
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(
                            new FileNameExtensionFilter(
                                    "Scalable Vector Graphics (SVG)",
                                    "svg"));

                    fileChooser.showSaveDialog(that);
                    File file = fileChooser.getSelectedFile();
                    if (file == null)
                        return;
                    if (file.exists()) {
                        int response = JOptionPane.showConfirmDialog(that,
                                "This file already exist.\nDo you want to overwrite it?",
                                "Overwrite file?",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (response != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    String filePath = file.toString();
                    if (!file.getName().toLowerCase().endsWith(".svg")) {
                        filePath += ".svg";
                    }

                    that.saveToSvg(filePath);
                }
            }
        };
        AbstractAction saveAsPngAction = new AbstractAction("Save As PNG") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (that.chart != null) {
                    // String svg = that.chart.getSvgXML();
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(
                            new FileNameExtensionFilter(
                                    "PNG image",
                                    "png"));

                    fileChooser.showSaveDialog(that);
                    File file = fileChooser.getSelectedFile();
                    if (file == null)
                        return;
                    if (file.exists()) {
                        int response = JOptionPane.showConfirmDialog(that,
                                "This file already exist.\nDo you want to overwrite it?",
                                "Overwrite file?",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (response != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    String filePath = file.toString();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        filePath += ".png";
                    }

                    that.saveToPng(filePath);
                }
            }
        };

        AbstractAction copyToClipboard = new AbstractAction("Copy To Clipboard") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (that.chart != null) {
                    that.copyToClipboard();
                }
            }
        };

        ImageIcon saveIcon = new NoScalingIcon("./resources/icons/save_32x32.png");
        ImageIcon copyIcon = new NoScalingIcon("./resources/icons/copy_32x32.png");

        JButton btnSaveAsSvg = new JButton();

        btnSaveAsSvg.setIcon(saveIcon);
        btnSaveAsSvg.setText("SVG");
        btnSaveAsSvg.setToolTipText("Export to SVG");
        btnSaveAsSvg.addActionListener(saveAsSvgAction);

        JButton btnSaveAsPng = new JButton();
        btnSaveAsPng.setIcon(saveIcon);
        btnSaveAsPng.setText("PNG");
        btnSaveAsPng.setToolTipText("Export to PNG");
        btnSaveAsPng.addActionListener(saveAsPngAction);

        JButton btnCopyToClipboard = new JButton();
        btnCopyToClipboard.setIcon(copyIcon);
        btnCopyToClipboard.setToolTipText("Copy to clipboard");
        btnCopyToClipboard.addActionListener(copyToClipboard);

        actionPanel.appendChild(btnSaveAsSvg);
        actionPanel.appendChild(btnSaveAsPng);
        actionPanel.appendChild(btnCopyToClipboard);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(saveAsSvgAction);
        popupMenu.add(saveAsPngAction);
        popupMenu.add(copyToClipboard);

        this.chartPanel.setPopupMenu(popupMenu);
    }

    private String getSvgXML() {
        Dimension dim = this.getSize();
        SVGGraphics2D svg2d = new SVGGraphics2D(dim.width, dim.height);
        this.chart.draw(svg2d, new Rectangle2D.Double(0, 0, dim.width, dim.height));
        String svgElement = svg2d.getSVGElement();
        return svgElement;
    }

    private byte[] getImageBytes() {
        JFreeChart chart = this.chart;
        Dimension dim = this.getSize();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int scale = 3; // chart will be rendered at thrice the resolution.
        try {
            ChartUtils.writeScaledChartAsPNG(bout, chart, dim.width, dim.height, scale, scale);
        } catch (IOException e) {
            System.err.println(e);
        }
        return bout.toByteArray();
    }

    public void saveToSvg(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath));
            String svg = this.getSvgXML();
            fileWriter.write(svg);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void saveToPng(String filePath) {
        try {
            Files.write(Path.of(filePath), this.getImageBytes());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void copyToClipboard() {
        this.chartPanel.doCopy();
    }

}
