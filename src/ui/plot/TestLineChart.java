package ui.plot;

import javax.swing.JPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.BasicStroke;

// import java.awt.event.ComponentAdapter;
// import java.awt.event.ComponentEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

// import org.jfree.chart.ChartFactory;
// import org.jfree.chart.ChartMouseEvent;
// import org.jfree.chart.ChartMouseListener;
// import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.DatasetRenderingOrder;
// import org.jfree.chart.plot.CrosshairState;
// import org.jfree.chart.plot.Plot;
// import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
// import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
// import org.jfree.chart.plot.PlotOrientation;
// import org.jfree.data.category.DefaultCategoryDataset;
// import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.chart.ChartPanel;

public class TestLineChart extends JPanel {

    ChartPanel panel;

    private Crosshair xCrosshair;
    private Crosshair yCrosshair;

    private int hlSeriesIndex;
    private int hlItemIndex;

    private Point hlPoint;

    public TestLineChart() {

        // LAST TASKS:
        // - get mouse interactions working
        // - try to mix with an area chart...

        int n = 10;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] z = new double[n];
        double[] u = new double[n];
        double[] u_min = new double[n];
        double[] u_max = new double[n];
        for (int k = 0; k < n; k++) {
            x[k] = k + 1;
            y[k] = Math.random() * 10;
            z[k] = Math.random() * 5 + 5;
            u[k] = Math.random() * 5;
            u_min[k] = u[k] * 0.9 - 1;
            u_max[k] = u[k] * 1.2 + 1;
        }

        DefaultXYDataset xyDataset = new DefaultXYDataset();
        xyDataset.addSeries("Y dataset", new double[][] { x, y });
        xyDataset.addSeries("Z dataset", new double[][] { x, z });

        ValueAxis axisX = new NumberAxis("X values");
        ValueAxis axisY = new NumberAxis("Y, Z or U values");

        // XYItemRenderer renderer = new XYLineAndShapeRenderer();

        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true) {

            @Override
            public Paint getItemPaint(int series, int item) {
                if (series == TestLineChart.this.hlSeriesIndex && item == TestLineChart.this.hlItemIndex) {
                    return Color.yellow;
                }
                return super.getItemPaint(series, item);
            }
        };

        // Shape
        Ellipse2D.Double shape = new Ellipse2D.Double();
        int shapeSize = 5;
        shape.width = shapeSize;
        shape.height = shapeSize;
        shape.x = -shapeSize / 2;
        shape.y = -shapeSize / 2;
        renderer.setSeriesShape(0, shape);
        renderer.setSeriesShape(1,
                new Rectangle(
                        -shapeSize * 2 / 2,
                        -shapeSize * 2 / 2,
                        shapeSize * 2, shapeSize * 2));
        // lines
        BasicStroke stroke = new BasicStroke(1,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                1, new float[] { 5F, 4F }, 0);
        // stroke.setLineWidth(5);
        renderer.setSeriesStroke(0, stroke);

        // Color
        renderer.setSeriesPaint(0, Color.BLUE);
        // renderer.getDefaultShape();

        // CUSTOM HIHGLIGHT

        hlPoint = new Point(5, 5);
        DefaultXYDataset hlDs = new DefaultXYDataset();
        hlDs.addSeries("HL", new double[][] {
                new double[] { hlPoint.x },
                new double[] { hlPoint.y } });
        XYLineAndShapeRenderer hlRenderer = new XYLineAndShapeRenderer(false, true);
        double hlShapeSize = 25;
        Shape hlShape = new Ellipse2D.Double(-hlShapeSize / 2, -hlShapeSize / 2, hlShapeSize, hlShapeSize);
        hlRenderer.setSeriesVisible(0, false);
        hlRenderer.setUseFillPaint(true);
        hlRenderer.setUseOutlinePaint(true);
        hlRenderer.setSeriesShape(0, hlShape);
        hlRenderer.setSeriesOutlineStroke(0, new BasicStroke(2));
        hlRenderer.setSeriesOutlinePaint(0, new Color(0, 0, 0));
        hlRenderer.setSeriesFillPaint(0, new Color(0, 0, 0, 0));
        hlRenderer.setSeriesVisibleInLegend(0, false);

        // // Creating plot with main/default dataset
        // XYPlot xyPlot = new XYPlot(xyDataset, axisX, axisY, renderer);
        // xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        // // Adding highlight dataset
        // xyPlot.setDataset(10, hlDs);
        // xyPlot.setRenderer(10, hlRenderer);

        // Creating plot with main/default dataset
        // XYPlot xyPlot = new XYPlot(hlDs, axisX, axisY, hlRenderer);
        XYPlot xyPlot = new XYPlot(hlDs, axisX, axisY, hlRenderer);
        xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE); // Default

        // Adding highlight dataset
        xyPlot.setDataset(1, xyDataset);
        xyPlot.setRenderer(1, renderer);

        // xyPlot.setDataset(1, xyDataset);

        // xyPlot.addDataset
        // Change the color of the plot
        // xyPlot.setBackgroundPaint(Color.YELLOW);
        // xyPlot.setRangeGridlinePaint(Color.GREEN);
        // xyPlot.setDomainGridlinePaint(Color.ORANGE);
        xyPlot.setBackgroundPaint(new Color(255, 0, 0, 0));

        // make the plot pannable (using Ctrl + dragging)
        xyPlot.setDomainPannable(true);
        xyPlot.setRangePannable(true);

        DeviationRenderer rendererU = new DeviationRenderer();
        // Shape shapeU = rendererU.getSeriesShape(0);
        // shapeU.set
        rendererU.setSeriesShape(0, new Rectangle());
        rendererU.setSeriesStroke(0, new BasicStroke(2));
        rendererU.setSeriesPaint(0, Color.ORANGE);
        rendererU.setSeriesFillPaint(0, Color.ORANGE);
        rendererU.setAlpha(0.25F);

        // Adding secondary dataset
        DefaultIntervalXYDataset uDataset = new DefaultIntervalXYDataset();
        uDataset.addSeries("U", new double[][] { x, x, x, u, u_min, u_max });
        xyPlot.setDataset(2, uDataset);
        xyPlot.setRenderer(2, rendererU);

        // Change legend location
        LegendTitle legendTitle = new LegendTitle(xyPlot);
        // legendTitle.remove
        legendTitle.setBackgroundPaint(Color.WHITE);
        legendTitle.setPadding(5, 5, 5, 5);
        legendTitle.setFrame(new BlockBorder(0.25, 0.25, 0.25, 0.25, Color.BLACK));
        // legendTitle.setPosition(RectangleEdge.LEFT);
        legendTitle.setPosition(RectangleEdge.TOP);
        XYTitleAnnotation titleAnnot = new XYTitleAnnotation(
                0.5, 1,
                legendTitle,
                RectangleAnchor.TOP);
        // titleAnnot.setMaxWidth(0.1);
        xyPlot.addAnnotation(titleAnnot); // need to remove original legend in JFreeChart object

        JFreeChart chart = new JFreeChart(xyPlot);

        chart.removeLegend();
        // chart.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(new Color(255, 0, 0, 0));
        chart.setPadding(new RectangleInsets(10, 10, 10, 20));

        panel = new ChartPanel(
                chart,
                0, 0,
                100, 100,
                7680, 4320,
                true,
                false,
                true,
                false,
                false,
                true);
        // panel = new ChartPanel(
        // chart,
        // true,
        // true,
        // true,
        // true,
        // true);
        // panel.setMinimumDrawWidth(0);
        // panel.setMinimumDrawHeight(0);
        // panel.setMaximumDrawWidth(7680);
        // panel.setMaximumDrawHeight(4320);

        // this.setBackground(Color.GREEN);

        this.setLayout(new GridBagLayout());

        this.add(panel, new GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                1,
                GridBagConstraints.NORTH,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2),
                0,
                0)

        );

        // this.addComponentListener(new ComponentAdapter() {
        // public void componentResized(ComponentEvent e) {
        // Dimension dim = LineChart.this.getSize();
        // System.out.println(String.format("Resized: %d x %d", dim.width, dim.height));
        // // panel.setPreferredSize(dim);
        // // panel.setSize(dim);
        // // panel.setMinimumDrawWidth(0);
        // // panel.setMinimumDrawHeight(0);
        // // panel.setMaximumDrawWidth(dim.width);
        // // panel.setMaximumDrawHeight(dim.height);
        // // panel.repaint();
        // }

        // public void componentMoved(ComponentEvent e) {
        // Point loc = LineChart.this.getLocation();
        // System.out.println(String.format("Relocated: %d, %d", loc.x, loc.y));
        // }
        // });

        CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
        this.xCrosshair = new Crosshair(Double.NaN, Color.BLACK,
                new BasicStroke(1f));
        this.xCrosshair.setLabelVisible(true);
        this.yCrosshair = new Crosshair(Double.NaN, Color.BLACK,
                new BasicStroke(0f));
        this.yCrosshair.setLabelVisible(true);
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        crosshairOverlay.addRangeCrosshair(yCrosshair);
        this.xCrosshair.setLabelGenerator(new StandardCrosshairLabelGenerator() {
            @Override
            public String generateLabel(Crosshair crosshair) {

                // …
                // long volume = item.getValue().longValue();
                // String s = NumberFormat.getInstance().format(volume);
                // return MessageFormat.format(" Volume: {0} ", s);
                return String.format(" X = %.1f ", crosshair.getValue());
            }
        });

        panel.addOverlay(crosshairOverlay);

        // NOT VERY USEFUL SINCE YOU CAN ATTACH THE LISTENER DIRECTY TO THE PANEL
        // USING Awt addMouseMotionListener
        // panel.addChartMouseListener(new ChartMouseListener() {
        // @Override
        // public void chartMouseClicked(ChartMouseEvent event) {
        // }
        // @Override
        // public void chartMouseMoved(ChartMouseEvent event) {
        // }
        // });

        panel.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent mouseEvent) {
                Point p = mouseEvent.getPoint();

                Rectangle2D dataArea = TestLineChart.this.panel.getScreenDataArea();

                XYPlot plot = (XYPlot) TestLineChart.this.panel.getChart().getPlot();
                ValueAxis xAxis = plot.getDomainAxis();
                ValueAxis yAxis = plot.getRangeAxis();

                double x = xAxis.java2DToValue(p.getX(), dataArea, RectangleEdge.BOTTOM);
                double y = yAxis.java2DToValue(p.getY(), dataArea, RectangleEdge.LEFT);
                TestLineChart.this.xCrosshair.setValue(x);
                TestLineChart.this.yCrosshair.setValue(y);

                double size = 25;
                double uX = xAxis.lengthToJava2D(1, dataArea, RectangleEdge.BOTTOM);
                double uY = xAxis.lengthToJava2D(1, dataArea, RectangleEdge.LEFT);
                // System.out.printf("\n\nuX=%.1f; uY=%.1f\n", uX, uY);
                double sizeX = size / uX;
                double sizeY = size / uY;
                Ellipse2D.Double circle = new Ellipse2D.Double(
                        x - sizeX / 2,
                        y - sizeY / 2,
                        sizeX, sizeY);
                XYDataset dataset = plot.getDataset(1);
                int n = dataset.getSeriesCount();
                int hlS = -1;
                int hlI = -1;
                double hlx = 0;
                double hly = 0;
                boolean hl = false;
                for (int i = 0; i < n; i++) {
                    if (n == 0)
                        continue;
                    int m = dataset.getItemCount(i);
                    for (int k = 0; k < m; k++) {
                        double vx = dataset.getXValue(i, k);
                        double vy = dataset.getYValue(i, k);
                        if (circle.contains(vx, vy)) {
                            // System.out.println("Intersection for item " + k);
                            hlS = i;
                            hlI = k;
                            hlx = vx;
                            hly = vy;
                            hl = true;
                            // chart.fireChartChanged();
                        }
                    }
                }
                if (hlS != hlSeriesIndex || hlI != hlItemIndex) {
                    System.out.println("-".repeat(70));
                    hlSeriesIndex = hlS;
                    hlItemIndex = hlI;
                    chart.fireChartChanged();

                    // XYPlot plot = (XYPlot) chart.getPlot();
                    DefaultXYDataset hlds = (DefaultXYDataset) plot.getDataset(0);
                    hlds.addSeries("HL", new double[][] {
                            new double[] { hlx },
                            new double[] { hly } });
                    // plot.getRenderer(2).setSeriesVisible(0, true);

                }
                plot.getRenderer(0).setSeriesVisible(0, hl);

                // double [] data = new double[n];

            }
        });

    }

    public String getSvgXML() {
        JFreeChart chart = this.panel.getChart();

        Dimension dim = panel.getSize();
        System.out.println(dim);

        final SVGGraphics2D svg2d = new SVGGraphics2D(dim.width, dim.height);

        chart.draw(svg2d, new Rectangle2D.Double(0, 0, dim.width, dim.height));

        final String svgElement = svg2d.getSVGElement();
        return svgElement;
    }
}
