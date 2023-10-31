package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.ImageIcon;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.baratinage.ui.AppConfig;
import org.baratinage.utils.ConsoleLogger;

public class SvgIcon extends ImageIcon {

    private final float width;
    private final float height;

    private final byte[] svgBytes;
    private final String sourcePath;

    private static Scales lastUsedScales;
    private static Scales memorizedScales;

    public static void memorizeCurrentScales() {
        memorizedScales = lastUsedScales;
    }

    public static boolean scalesHaveChanged() {
        if (memorizedScales == null) {
            return true;
        }
        Scales s = getScales();
        return !s.toString().equals(memorizedScales.toString());
    }

    public SvgIcon(String path, float width, float height) {
        super();

        this.sourcePath = path;
        this.width = width;
        this.height = height;

        byte[] b = new byte[0];
        try {
            InputStream inputStream = new FileInputStream(new File(sourcePath));
            b = inputStream.readAllBytes();
        } catch (IOException e) {
            ConsoleLogger.error("Error while retrieving SVG file");
            ConsoleLogger.stackTrace(e);
        }

        this.svgBytes = b;

        buildIcon();
    };

    public void rebuildIcon() {
        buildIcon();
    }

    private void buildIcon() {

        float w = width;
        float h = height;

        Scales s = getScales();
        lastUsedScales = s;

        w = width * (float) s.x;
        h = height * (float) s.y;

        BufferedImageTranscoder imgTranscoder = new BufferedImageTranscoder();
        imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, w);
        imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, h);

        try {
            InputStream inputStream = new ByteArrayInputStream(svgBytes);
            TranscoderInput transcoderInput = new TranscoderInput(inputStream);
            imgTranscoder.transcode(transcoderInput, null);
        } catch (TranscoderException e) {
            ConsoleLogger.error("error while build raster image from SVG.");
            ConsoleLogger.stackTrace(e);
            return;
        }
        BufferedImage image = imgTranscoder.getBufferedImage();
        setImage(image);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

        // See: https://stackoverflow.com/a/65742492

        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform aT = g2d.getTransform();

        double sX = aT.getScaleX();
        double sY = aT.getScaleY();

        g2d.scale(1 / sX, 1 / sY);

        int iconHeight = super.getIconHeight();
        int componentHeight = c.getHeight();

        y = scale(componentHeight, sY, false) / 2 - iconHeight / 2;

        super.paintIcon(c, g2d, x, y);
        g2d.dispose();

    }

    @Override
    public int getIconWidth() {
        int d = super.getIconWidth();
        d = scale(d, getScales().x, true);
        return d;
    }

    @Override
    public int getIconHeight() {
        int d = super.getIconHeight();
        d = scale(d, getScales().y, true);
        return d;
    }

    private static record Scales(double x, double y) {
        @Override
        public String toString() {
            return "" + x + "," + y;
        }
    };

    private static int scale(int value, double factor, boolean inverse) {
        if (inverse) {
            return (int) Math.round(value / factor);
        } else {
            return (int) Math.round(value * factor);
        }
    }

    private static Scales getScales() {
        Graphics g = AppConfig.AC.APP_MAIN_FRAME.getGraphics();
        if (g == null) {
            return new Scales(1, 1);
        }
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform aT = g2d.getTransform();
        g2d.dispose();
        double sX = aT.getScaleX();
        double sY = aT.getScaleY();
        return new Scales(sX, sY);
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {

        private BufferedImage bufferedImage = null;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage bufferedImage, TranscoderOutput transcoderOutput)
                throws TranscoderException {
            this.bufferedImage = bufferedImage;
        }

        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

    }
}
