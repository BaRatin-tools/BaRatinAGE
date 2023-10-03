package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class NoScalingIcon extends ImageIcon {

    private double scaleX;
    private double scaleY;

    public NoScalingIcon() {
        super();
        init();
    }

    public NoScalingIcon(Image image) {
        super(image);
        init();
    }

    public NoScalingIcon(ImageIcon icon) {
        super(icon.getImage());
        init();
    }

    private void init() {

        // See: https://stackoverflow.com/a/52693087
        GraphicsConfiguration asdf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();

        AffineTransform asfd2 = asdf.getDefaultTransform();

        scaleX = asfd2.getScaleX();
        scaleY = asfd2.getScaleY();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

        // See: https://stackoverflow.com/a/65742492

        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform aT = g2d.getTransform();

        scaleX = aT.getScaleX();
        scaleY = aT.getScaleY();

        g2d.scale(1 / scaleX, 1 / scaleY);

        int iconHeight = super.getIconHeight();
        int componentHeight = c.getHeight();

        y = scale(componentHeight, scaleY, false) / 2 - iconHeight / 2;

        super.paintIcon(c, g2d, x, y);
        g2d.dispose();

    }

    private static int scale(int value, double factor, boolean inverse) {
        if (inverse) {
            return (int) Math.round(value / factor);
        } else {
            return (int) Math.round(value * factor);
        }
    }

    @Override
    public int getIconWidth() {
        int d = super.getIconWidth();
        d = scale(d, scaleX, true);
        return d;
    }

    @Override
    public int getIconHeight() {
        int d = super.getIconHeight();
        d = scale(d, scaleY, true);
        return d;
    }
}
