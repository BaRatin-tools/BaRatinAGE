package org.baratinage.ui.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

public class LogAxis extends LogarithmicAxis {

    /**
     * this class overrides switchedLog10 to remove negative values
     * and refreshTicksVertical and refreshTicksHorizontal to get
     * intermediate ticks when zoom is precise.
     */

    public LogAxis() {
        super("-");
    }

    static private final double MIN_VALUE = 1e-20;
    private double minValue = MIN_VALUE;

    @Override
    protected double switchedLog10(double value) {
        if (value <= 0) {
            value = minValue;
        }
        return super.switchedLog10(value);
    }

    @Override
    protected List<NumberTick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        @SuppressWarnings("unchecked")
        List<NumberTick> rawTicks = super.refreshTicksVertical(g2, dataArea, edge);

        return processTicks(rawTicks);
    }

    @Override
    protected List<NumberTick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        @SuppressWarnings("unchecked")
        List<NumberTick> rawTicks = super.refreshTicksHorizontal(g2, dataArea, edge);

        return processTicks(rawTicks);
    }

    public void setMinValue(double minValue) {
        if (minValue > 0) {
            this.minValue = minValue;
        }
    }

    public Range getRange() {
        Range range = super.getRange();
        if (range.getLowerBound() < 0) {
            return new Range(minValue, range.getUpperBound());
        }
        return range;
    }

    private List<NumberTick> processTicks(List<NumberTick> rawTicks) {

        int minimumTicks = 2;
        int minimumLabels = 3;

        int nTicks = rawTicks.size();

        if (nTicks < minimumTicks) {

            Range range = getRange();
            int n = (int) Math.floor(Math.log10(range.getLength()));
            double lowerBound = range.getLowerBound();
            double upperBound = range.getUpperBound();
            double boundRange = range.getLength();
            double val = boundRange;
            for (int offset = 0; offset < 2; offset++) {
                double step = Math.pow(10, Math.floor(Math.log10(val)) - offset);
                String template = "%." + (Math.max(0, (n - offset) * -1)) + "f";

                List<NumberTick> newTicks = new ArrayList<>();

                for (Double k = step; k < upperBound; k += step) {
                    if (k >= lowerBound) {
                        newTicks.add(new NumberTick(k, String.format(template, k),
                                TextAnchor.CENTER_RIGHT,
                                TextAnchor.CENTER_RIGHT,
                                0));
                    }
                }
                if (newTicks.size() >= minimumTicks) {
                    return newTicks;
                }

            }
        }

        int nLabels = 0;
        for (NumberTick t : rawTicks) {
            nLabels += t.getText().equals("") ? 0 : 1;
        }

        if (nLabels < minimumLabels) {
            List<NumberTick> addedLabelsTicks = new ArrayList<>();
            for (NumberTick t : rawTicks) {
                Double d = t.getValue();
                // String s = String.format("%f", d).replaceAll("([.]*0+)(?!.*\\d)", "");
                String s = BigDecimal.valueOf(d).setScale(7, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                addedLabelsTicks.add(new NumberTick(
                        t.getTickType(),
                        d,
                        s,
                        t.getTextAnchor(),
                        t.getRotationAnchor(),
                        t.getAngle()));
            }
            return addedLabelsTicks;
        }

        return rawTicks;
    }

}
