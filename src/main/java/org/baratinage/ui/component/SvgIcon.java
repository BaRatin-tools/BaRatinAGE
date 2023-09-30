package org.baratinage.ui.component;

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

public class SvgIcon extends ImageIcon {

    static public ImageIcon buildCustomAppImageIcon(String name, float size) {
        return buildNoScalingIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "custom", name).toString(), size);
    }

    static public ImageIcon buildFeatherAppImageIcon(String name, float size) {
        return buildNoScalingIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "feather", name).toString(), size);
    }

    static public NoScalingIcon buildNoScalingIcon(String path, float width, float height) {
        return new NoScalingIcon(new SvgIcon(path, width, height));
    }

    static public NoScalingIcon buildNoScalingIcon(String path, float size) {
        return new NoScalingIcon(new SvgIcon(path, size, size));
    }

    public SvgIcon(String path, float width, float height) {
        super();
        // source: https://stackoverflow.com/a/20664243
        try {
            BufferedImageTranscoder imgTranscoder = new BufferedImageTranscoder();
            imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
            imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
            InputStream inputStream = new FileInputStream(new File(path));
            TranscoderInput transcoderInput = new TranscoderInput(inputStream);
            imgTranscoder.transcode(transcoderInput, null);
            setImage(imgTranscoder.getBufferedImage());
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
