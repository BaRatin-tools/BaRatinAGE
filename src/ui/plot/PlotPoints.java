package ui.plot;

import java.awt.Paint;

import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;

public class PlotPoints extends PlotLine {

    public PlotPoints(String label, double[] x, double[] y, Paint paint, SHAPE shape, int shapeSize) {
        super(label, x, y, paint, 0, shape, shapeSize);
        DefaultXYItemRenderer renderer = getRenderer();
        renderer.setSeriesLinesVisible(0, false);
    }

}
