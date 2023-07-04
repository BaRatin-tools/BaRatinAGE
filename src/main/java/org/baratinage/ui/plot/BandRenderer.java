package org.baratinage.ui.plot;

import java.awt.Paint;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.DeviationRenderer;

public class BandRenderer extends DeviationRenderer {

    private int legendSquareSize = 10;
    private Paint paint;

    public BandRenderer(Paint paint, int legendSquareSize) {
        this.legendSquareSize = legendSquareSize;
        this.paint = paint;
    }

    public BandRenderer(Paint paint) {
        this.paint = paint;
    }

    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {
        LegendItem defaultLegendItem = super.getLegendItem(datasetIndex, series);
        if (legendSquareSize > 0) {
            return new LegendItem(
                    defaultLegendItem.getLabel(),
                    defaultLegendItem.getDescription(),
                    defaultLegendItem.getToolTipText(),
                    defaultLegendItem.getURLText(),
                    PlotItem.buildSquareShape(legendSquareSize), paint);
        }
        return defaultLegendItem;
    }
}
