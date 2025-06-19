package org.baratinage.ui.plot;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.svg.SVGGraphics2D;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotExporter.ExportablePlot;
import org.baratinage.translation.T;

public class PlotContainer extends SimpleFlowPanel implements ExportablePlot {

    private Plot plot;
    private JFreeChart chart;
    private SimpleFlowPanel chartPanelContainer;
    private CustomChartPanel chartPanel;

    public final SimpleFlowPanel toolsPanel;
    private final SimpleFlowPanel actionPanel;

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
        super(true);

        SimpleFlowPanel topPanel = new SimpleFlowPanel();
        topPanel.setPadding(10);

        if (toolbar) {
            addChild(topPanel, false);
        }

        chartPanelContainer = new SimpleFlowPanel();
        addChild(chartPanelContainer, true);

        toolsPanel = new SimpleFlowPanel();
        actionPanel = PlotExporter.buildExportPanel(this);

        topPanel.addChild(toolsPanel, false);
        topPanel.addExtensor();
        topPanel.addChild(actionPanel, false);

        SimpleCheckbox cbShowLegend = new SimpleCheckbox();
        cbShowLegend.setSelected(true);
        cbShowLegend.addChangeListener((e) -> {
            if (plot != null) {
                plot.setIncludeLegend(cbShowLegend.isSelected());
            }
        });

        toolsPanel.addChild(cbShowLegend, false);
        toolsPanel.setGap(5);
        actionPanel.setGap(5);

        T.t(this, () -> {
            cbShowLegend.setText(T.text("show_legend"));
        });

    }

    public void setPlot(Plot plot) {
        this.plot = plot;
        chart = plot.getChart();

        chartPanel = new CustomChartPanel(plot);

        // chartPanel.setPopupMenu(popupMenu);
        chartPanel.setPopupMenu(PlotExporter.buildExportPopupMenu(this));

        chartPanelContainer.removeAll();
        chartPanelContainer.addChild(chartPanel, true);

    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public Plot getPlot() {
        return plot;
    }

    @Override
    public ExportablePlot getCopy() {
        return new PlotContainer(plot.getCopy());
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public String getSvgString() {
        Dimension dim = getSize();
        SVGGraphics2D svg2d = new SVGGraphics2D(dim.width, dim.height);
        chart.draw(svg2d, new Rectangle2D.Double(0, 0, dim.width, dim.height));
        String svgElement = svg2d.getSVGElement();
        return svgElement;
    }

    @Override
    public BufferedImage getBufferedImage() {
        int scale = 2;
        Dimension d = getSize();
        return PlotExporter.buildImgFromChart(
                chart,
                d.width,
                d.width,
                scale,
                scale);
    }

}
