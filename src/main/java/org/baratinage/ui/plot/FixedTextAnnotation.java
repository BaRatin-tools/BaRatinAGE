package org.baratinage.ui.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

public class FixedTextAnnotation extends JLabel implements XYAnnotation {

        private final float x;
        private final float y;

        public FixedTextAnnotation(String text, double x, double y) {
                super();

                setText(text);
                this.x = (float) x;
                this.y = (float) y;

        }

        @Override
        public void addChangeListener(AnnotationChangeListener listener) {

        }

        @Override
        public void removeChangeListener(AnnotationChangeListener listener) {

        }

        @Override
        public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis,
                        ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {

                setSize(getPreferredSize());

                float xStart = (float) dataArea.getX();
                float yStart = (float) dataArea.getY();

                float xTranslate = xStart + x;
                float yTranslate = yStart + y;
                g2.translate(xTranslate, yTranslate);
                paint(g2);
                g2.translate(-xTranslate, -yTranslate);

        }

}
