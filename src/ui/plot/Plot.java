package ui.plot;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;

public class Plot {

    private XYPlot plot;
    private JFreeChart chart;

    NumberAxis axisX;
    NumberAxis axisY;

    private ArrayList<Line> lines;

    public Plot(String xAxisLabel, String yAxisLabel, boolean includeLegend) {
        lines = new ArrayList<>();

        axisX = new NumberAxis(xAxisLabel);
        axisY = new NumberAxis(yAxisLabel);
        axisY.setAutoRangeIncludesZero(false);

        plot = new XYPlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setDomainAxis(axisX);
        plot.setRangeAxis(axisY);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chart = new JFreeChart(plot);
        chart.setBackgroundPaint(Color.WHITE);
        chart.removeLegend();

        // Change legend location

        if (includeLegend) {

            LegendTitle legendTitle = new LegendTitle(plot);
            legendTitle.setBackgroundPaint(Color.WHITE);
            legendTitle.setPadding(5, 5, 5, 5);
            legendTitle.setFrame(new BlockBorder(0.25, 0.25, 0.25, 0.25, Color.BLACK));
            // legendTitle.setPosition(RectangleEdge.LEFT);
            legendTitle.setPosition(RectangleEdge.TOP);
            XYTitleAnnotation titleAnnot = new XYTitleAnnotation(
                    0.5, 1,
                    legendTitle,
                    RectangleAnchor.TOP);
            plot.addAnnotation(titleAnnot);
        }

    }

    public JFreeChart getChart() {
        return this.chart;
    }

    public void addLine(Line line) {
        plot.setDataset(lines.size(), line.getDataset());
        plot.setRenderer(lines.size(), line.getRenderer());
        lines.add(line);
    }
}
