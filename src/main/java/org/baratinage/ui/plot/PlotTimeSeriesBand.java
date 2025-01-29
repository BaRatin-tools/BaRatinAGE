package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.data.time.Second;

public class PlotTimeSeriesBand extends PlotBand {

    public PlotTimeSeriesBand(String label,
            Second[] time, double[] ylow, double[] yhigh,
            Paint paint) {
        super(label, secondToDouble(time), ylow, yhigh, false, paint, 0.9f);
    }

    public PlotTimeSeriesBand(String label,
            Second[] time, double[] y, double[] ylow, double[] yhigh,
            Paint fillPaint, float alpha,
            Paint linePaint, Stroke lineStroke) {
        super(label, secondToDouble(time), ylow, yhigh, false, fillPaint, 0.9f, linePaint, lineStroke);
    }

}
