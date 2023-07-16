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

        Range range = getRange();
        int n = (int) Math.floor(Math.log10(range.getLength()));

        if (n <= 2) {
            double lowerBound = range.getLowerBound();
            double upperBound = range.getUpperBound();
            double boundRange = range.getLength();
            double val = boundRange;
            for (int offset = 0; offset < 2; offset++) {
                double step = Math.pow(10, Math.floor(Math.log10(val)) - offset);
                String template = "%." + (Math.max(0, (n - offset) * -1)) + "f";
                List<NumberTick> arr = new ArrayList<>();
                for (Double k = step; k < upperBound; k += step) {
                    if (k >= lowerBound) {
                        arr.add(new NumberTick(k, String.format(template, k),
                                TextAnchor.CENTER_RIGHT,
                                TextAnchor.CENTER_RIGHT,
                                0));
                    }
                }
                if (arr.size() >= 3) {
                    return arr;
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<NumberTick> res = super.refreshTicksVertical(g2, dataArea, edge);
        return res;
    }

}
