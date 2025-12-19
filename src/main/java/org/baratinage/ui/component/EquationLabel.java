package org.baratinage.ui.component;

import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.equation.EquationParser;
import org.baratinage.utils.equation.Expr;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class EquationLabel extends JLabel {

  private String latexEquation = "";

  public EquationLabel() {

  }

  public EquationLabel(String equation) {
    setEquation(equation);
  }

  public void setEquation(String equation) {
    setLatexEquation(convertToLatex(equation));
  }

  public void setLatexEquation(String latex) {
    latexEquation = latex;
    updateLabel();
  }

  private void updateLabel() {
    try {
      TeXFormula formula = new TeXFormula(latexEquation);
      TeXIcon icon = formula.createTeXIcon(
          TeXFormula.SANSSERIF,
          AppSetup.CONFIG.FONT_SIZE.get());
      setIcon(icon);
    } catch (Exception e) {
      ConsoleLogger.error(e);
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
      ConsoleLogger.warn(e);
    }
    return latex;
  }
}
