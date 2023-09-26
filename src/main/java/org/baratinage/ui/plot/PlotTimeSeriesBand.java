package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.data.time.Second;

public class PlotTimeSeriesBand extends PlotBand {

    public PlotTimeSeriesBand(String label,
            Second[] time, double[] ylow, double[] yhigh,
            Paint paint) {
        this(label, time, ylow, ylow, yhigh, paint, 0.9f, paint, buildEmptyStroke());
    }

    public PlotTimeSeriesBand(String label,
            Second[] time, double[] y, double[] ylow, double[] yhigh,
            Paint fillPaint, float alpha,
            Paint linePaint, Stroke lineStroke) {
        super(label, secondToDouble(time), y, ylow, yhigh, fillPaint, alpha, linePaint, lineStroke);
    }

}
