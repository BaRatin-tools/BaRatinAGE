package org.baratinage.ui.plot;

import java.awt.Paint;

import org.baratinage.ui.plot.PlotItem.ShapeType;

public interface IPlotItemRendererSettings {

    public Paint getLinePaint();

    public float getLineWidth();

    public float[] getLineDashArray();

    public double getShapeSize();

    public ShapeType getShapeType();

    public Paint getFillPaint();

    public float getFillAlpha();
}
