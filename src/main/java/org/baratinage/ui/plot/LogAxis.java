package org.baratinage.ui.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

public class LogAxis extends LogarithmicAxis {

    public LogAxis() {
        super("-");
    }

    @Override
    protected List<NumberTick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        @SuppressWarnings("unchecked")
        List<NumberTick> rawTicks = super.refreshTicksVertical(g2, dataArea, edge);

        int nTicks = rawTicks.size();

        if (nTicks < 3) {

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
                if (newTicks.size() >= 3) {
                    return newTicks;
                }

            }
        }

        int nLabels = 0;
        for (NumberTick t : rawTicks) {
            nLabels += t.getText().equals("") ? 0 : 1;
        }

        if (nLabels < 2) {
            List<NumberTick> addedLabelsTicks = new ArrayList<>();
            for (NumberTick t : rawTicks) {
                Double d = t.getValue();
                addedLabelsTicks.add(new NumberTick(
                        t.getTickType(),
                        d,
                        d.toString(),
                        t.getTextAnchor(),
                        t.getRotationAnchor(),
                        t.getAngle()));
            }
            return addedLabelsTicks;
        }

        return rawTicks;

        // Range range = getRange();
        // int n = (int) Math.floor(Math.log10(range.getLength()));

        // if (n <= 2) {
        // double lowerBound = range.getLowerBound();
        // double upperBound = range.getUpperBound();
        // double boundRange = range.getLength();
        // double val = boundRange;
        // for (int offset = 0; offset < 2; offset++) {
        // double step = Math.pow(10, Math.floor(Math.log10(val)) - offset);
        // String template = "%." + (Math.max(0, (n - offset) * -1)) + "f";
        // List<NumberTick> arr = new ArrayList<>();
        // for (Double k = step; k < upperBound; k += step) {
        // if (k >= lowerBound) {
        // arr.add(new NumberTick(k, String.format(template, k),
        // TextAnchor.CENTER_RIGHT,
        // TextAnchor.CENTER_RIGHT,
        // 0));
        // }
        // }
        // if (arr.size() >= 3) {
        // return arr;
        // }
        // }
        // }

        // return res;
    }

}
