package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotExporter;
import org.baratinage.ui.plot.PlotExporter.IExportablePlot;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.equation.EquationParser;
import org.baratinage.utils.equation.Expr;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class EquationLabel extends JLabel implements IExportablePlot {

  private String latexEquation = "";

  public EquationLabel() {
    setPopupMenu();
  }

  public EquationLabel(String equation) {
    setEquation(equation);
    setPopupMenu();
  }

  public void setEquation(String equation) {
    setLatexEquation(convertToLatex(equation));
  }

  public void setLatexEquation(String latex) {
    latexEquation = latex;
    updateLabel();
  }

  private TeXIcon getTeXIcon(int scale) {
    try {
      TeXFormula formula = new TeXFormula(latexEquation);
      TeXIcon icon = formula.createTeXIcon(
          TeXFormula.SANSSERIF,
          AppSetup.CONFIG.FONT_SIZE.get() * scale);
      return icon;
    } catch (Exception e) {
      ConsoleLogger.error(e);
      return null;
    }
  }

  private void updateLabel() {
    TeXIcon icon = getTeXIcon(1);
    if (icon != null) {
      setIcon(icon);
    } else {
      setText(latexEquation);
    }
  }

  public static String convertToLatex(String plainEquation) {
    String latex = plainEquation;
    try {
      EquationParser ep = new EquationParser(plainEquation);
      Expr expr = ep.parse();
      latex = expr.toLatex();
    } catch (Exception e) {
      ConsoleLogger.log("Cannot parse equation '%s'".formatted(plainEquation));
    }
    return latex;
  }

  private void setPopupMenu() {
    JPopupMenu menu = PlotExporter.buildExportPopupMenu(this);
    JMenuItem menuCopyLatexToClipboard = new JMenuItem();
    T.t(this, () -> {
      menuCopyLatexToClipboard.setText(T.text("latex_to_clipboard"));
    });
    menuCopyLatexToClipboard.setIcon(AppSetup.ICONS.LATEX);
    menuCopyLatexToClipboard.addActionListener((e) -> {
      Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      systemClipboard.setContents(new StringSelection(latexEquation), null);
    });
    menu.add(menuCopyLatexToClipboard, 0);
    setComponentPopupMenu(menu);
  }

  public JToolBar getExportToolbar() {
    JToolBar toolbar = PlotExporter.buildExportToolBar(this);
    JButton btnCopyLatexToClipboard = new JButton();
    btnCopyLatexToClipboard.addActionListener(l -> {
      Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      systemClipboard.setContents(new StringSelection(latexEquation), null);
    });
    btnCopyLatexToClipboard.setIcon(AppSetup.ICONS.LATEX);
    toolbar.add(btnCopyLatexToClipboard, 0);
    return toolbar;
  }

  @Override
  public IExportablePlot getCopy() {
    EquationLabel el = new EquationLabel();
    el.setLatexEquation(latexEquation);
    return el;
  }

  @Override
  public JPanel getPanel() {
    EquationLabel el = (EquationLabel) getCopy();
    SimpleFlowPanel panel = new SimpleFlowPanel(true);
    panel.addChild(el.getExportToolbar(), false);
    panel.addChild(el);
    return panel;
  }

  @Override
  public String getSvgString() {

    Icon icon = getIcon();

    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document = domImpl.createDocument(
        "http://www.w3.org/2000/svg", "svg", null);

    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);

    SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, true);

    svgGenerator.setSVGCanvasSize(
        new Dimension(
            icon.getIconWidth(),
            icon.getIconHeight()));

    icon.paintIcon(null, svgGenerator, 0, 0);

    StringWriter writer = new StringWriter();
    try {
      svgGenerator.stream(writer, true);
    } catch (SVGGraphics2DIOException e) {
      ConsoleLogger.error(e);
    }

    return writer.toString();
  }

  @Override
  public String getSvgString(Dimension dim) {
    return getSvgString();
  }

  private BufferedImage getBufferedImage(TeXIcon icon) {
    if (icon == null) {
      return null;
    }

    BufferedImage image = new BufferedImage(
        icon.getIconWidth(),
        icon.getIconHeight(),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2 = image.createGraphics();
    g2.setColor(Color.WHITE);
    g2.fillRect(0, 0, image.getWidth(), image.getHeight());
    icon.paintIcon(null, g2, 0, 0);
    g2.dispose();

    return image;
  }

  @Override
  public BufferedImage getBufferedImage() {
    return getBufferedImage(null, 4);
  }

  @Override
  public BufferedImage getBufferedImage(Dimension dim, int scale) {
    return getBufferedImage(getTeXIcon(scale));
  }

  @Override
  public boolean isPlotValid() {
    return getIcon() != null && getText() == "";
  }
}
