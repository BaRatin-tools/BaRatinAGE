package ui.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;

public class NoScalingIcon extends ImageIcon {

    private Component component;

    public NoScalingIcon(Component component, String iconFilePath) {
        super(iconFilePath);
        this.component = component;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Many thanks to: https://stackoverflow.com/a/65742492
        // This idea solve my problem
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform at = g2d.getTransform();

        // Reset scaling to 1.0 by concatenating an inverse scale transfom
        AffineTransform scaled = AffineTransform.getScaleInstance(1.0 /
                at.getScaleX(), 1.0 / at.getScaleY());

        at.concatenate(scaled);
        g2d.setTransform(at);

        super.paintIcon(c, g2d, x, y);
        g2d.dispose();

    }

    private AffineTransform getAffineTransform() {
        Graphics2D g2d = (Graphics2D) component.getGraphics().create();
        AffineTransform aT = g2d.getTransform();
        g2d.dispose();
        return aT;
    }

    @Override
    public int getIconWidth() {
        int d = super.getIconWidth();
        AffineTransform aT = getAffineTransform();
        return (int) (d / aT.getScaleY());
    }

    @Override
    public int getIconHeight() {
        int d = super.getIconHeight();
        AffineTransform aT = getAffineTransform();
        return (int) (d / aT.getScaleY());
    }
}
