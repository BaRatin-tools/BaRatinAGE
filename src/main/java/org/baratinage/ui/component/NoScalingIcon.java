package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Frame;
import javax.swing.ImageIcon;

public class NoScalingIcon extends ImageIcon {

    private Component component;

    /**
     * FIXME: I may wan't to use different version (different resolution)
     * FIXME: of an image file to adapt to different resolution instead?
     * Currently, the image stays small even if the user preferences are
     * to have everything zoomed 200%... But at least, it's no longer
     * pixelated...
     */

    public NoScalingIcon(String iconFilePath) {
        super(iconFilePath);
        this.setComponent();
    }

    private void setComponent() {
        Frame[] frames = Frame.getFrames();
        Component component = frames[0].getComponent(0);
        this.component = component;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Many thanks to: https://stackoverflow.com/a/65742492
        // This idea solve my problem
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform at = g2d.getTransform();

        // Reset scaling to 1.0 by concatenating an inverse scale transfom
        AffineTransform scaled = AffineTransform.getScaleInstance(
                1.0 / at.getScaleX(),
                1.0 / at.getScaleY());

        at.concatenate(scaled);
        g2d.setTransform(at);

        super.paintIcon(c, g2d, x, y);
        g2d.dispose();

    }

    private AffineTransform getAffineTransform() {
        Graphics g = component.getGraphics();
        if (g == null) {
            return new AffineTransform();
        }
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform aT = g2d.getTransform();
        g2d.dispose();
        return aT;
    }

    @Override
    public int getIconWidth() {
        int d = super.getIconWidth();
        AffineTransform aT = getAffineTransform();
        return (int) (d / aT.getScaleX());
    }

    @Override
    public int getIconHeight() {
        int d = super.getIconHeight();
        AffineTransform aT = getAffineTransform();
        return (int) (d / aT.getScaleY());
    }
}
