package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.io.File;

import javax.swing.ImageIcon;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.baratinage.ui.AppConfig;

public class SvgIcon extends NoScalingIcon {

    static public ImageIcon buildCustomAppImageIcon(String name) {
        return buildCustomAppImageIcon(name, AppConfig.AC.ICON_SIZE);
    }

    private static ImageIcon buildCustomAppImageIcon(String name, float size) {
        return new SvgIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "custom", name).toString(), size, size);
    }

    static public ImageIcon buildFeatherAppImageIcon(String name) {
        return buildFeatherAppImageIcon(name, AppConfig.AC.ICON_SIZE * 0.75f);
    }

    private static ImageIcon buildFeatherAppImageIcon(String name, float size) {
        return new SvgIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "feather", name).toString(), size, size);
    }

    public SvgIcon(String path, float width, float height) {
        super();
        // source: https://stackoverflow.com/a/20664243
        try {

            Frame[] frames = Frame.getFrames();
            Component component = frames[0].getComponent(0);

            Graphics g = component.getGraphics();
            if (g != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                AffineTransform aT = g2d.getTransform();
                g2d.dispose();
                double scaleX = aT.getScaleX();
                double scaleY = aT.getScaleY();
                width = width * (float) scaleX;
                height = height * (float) scaleY;
            }

            BufferedImageTranscoder imgTranscoder = new BufferedImageTranscoder();
            imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
            imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
            InputStream inputStream = new FileInputStream(new File(path));
            TranscoderInput transcoderInput = new TranscoderInput(inputStream);
            imgTranscoder.transcode(transcoderInput, null);
            BufferedImage bSrc = imgTranscoder.getBufferedImage();
            setImage(bSrc);
        } catch (TranscoderException | FileNotFoundException e) {
            e.printStackTrace();
        }

    };

    private class BufferedImageTranscoder extends ImageTranscoder {

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
