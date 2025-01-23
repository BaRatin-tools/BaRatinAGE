package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SvgIcon extends JComponent implements Icon {

    private final float width;
    private final float height;

    private final Document svgDocument;

    private final String sourcePath;

    private static Scales lastUsedScales;
    private static Scales memorizedScales;

    private final ImageIcon actualIcon;

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

        Document doc = null;
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            File file = new File(sourcePath);
            InputStream inputStream = new FileInputStream(file);
            doc = f.createDocument(file.getName(), inputStream);
        } catch (IOException ex) {
            ConsoleLogger.error(ex);
        }

        svgDocument = doc;

        actualIcon = new ImageIcon();

        buildIcon();

    };

    public void setSvgTagAttribute(String attrName, Color attrValue) {
        int r = attrValue.getRed();
        int g = attrValue.getGreen();
        int b = attrValue.getBlue();
        String value = String.format("rgb(%d,%d,%d)", r, g, b);
        setSvgTagAttribute(attrName, value);
    }

    public void setSvgTagAttribute(String attrName, String attrValue) {
        NodeList nodes = svgDocument.getChildNodes();
        int n = nodes.getLength();
        for (int k = 0; k < n; k++) {
            Node node = nodes.item(k);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getTagName().equals("svg")) {
                    element.setAttribute(attrName, attrValue);
                }
            }
        }
    }

    public void setAttribute(Element element, String attrName, String attrValue) {
        String attr = element.getAttribute(attrName);
        if (attr != null) {
            // if (attr.equals("#000") || attr.equals("#000ff") || attr.equals("black")) {
            System.out
                    .println("Found attr '" + attrName + "' set to '" + attr + "' for element " + element.getTagName());
            if (!attr.equals("none") && !attr.equals("")) {
                element.setAttribute(attrName, attrValue);
            }
        }
    }

    public static HashMap<String, String> getCssValues(Element element) {
        HashMap<String, String> cssValues = new HashMap<>();
        String strStyle = element.getAttribute("style");
        if (strStyle == null) {
            return cssValues;
        }
        String[] strStyleArray = strStyle.split(";");
        for (String s : strStyleArray) {
            String[] keyValuePair = s.split(":");
            if (keyValuePair.length == 2) {
                cssValues.put(keyValuePair[0], keyValuePair[1]);
            }
        }
        return cssValues;
    }

    public static void setCssValues(Element element, HashMap<String, String> cssValues) {
        String strStyle = "";
        for (String key : cssValues.keySet()) {
            strStyle += key + ":" + cssValues.get(key) + ";";
        }
        element.setAttribute("style", strStyle);
    }

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
        // h = h + (float) (Math.random() / 1.0);
        // h = h + (float) (Math.random() * 100.0);

        BufferedImageTranscoder imgTranscoder = new BufferedImageTranscoder();
        imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, w);
        imgTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, h);

        try {
            TranscoderInput transcoderInput = new TranscoderInput(svgDocument);
            imgTranscoder.transcode(transcoderInput, null);
        } catch (TranscoderException e) {
            ConsoleLogger.error("error while build raster image from SVG.\n" + e);
            return;
        }
        BufferedImage image = imgTranscoder.getBufferedImage();
        actualIcon.setImage(image);
        // repaint();
        // updateUI();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

        // See: https://stackoverflow.com/a/65742492

        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform aT = g2d.getTransform();

        double sX = aT.getScaleX();
        double sY = aT.getScaleY();

        g2d.scale(1 / sX, 1 / sY);

        int iconHeight = actualIcon.getIconHeight();
        int componentHeight = c.getHeight();

        y = scale(componentHeight, sY, false) / 2 - iconHeight / 2;

        actualIcon.paintIcon(c, g2d, x, y);
        g2d.dispose();

    }

    @Override
    public int getIconWidth() {
        int d = actualIcon.getIconWidth();
        d = scale(d, getScales().x, true);
        return d;
    }

    @Override
    public int getIconHeight() {
        int d = actualIcon.getIconHeight();
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
        if (AppSetup.MAIN_FRAME == null) {
            return new Scales(1, 1);
        }
        Graphics g = AppSetup.MAIN_FRAME.getGraphics();
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

    public Image getImage() {
        return actualIcon.getImage();
    }
}
