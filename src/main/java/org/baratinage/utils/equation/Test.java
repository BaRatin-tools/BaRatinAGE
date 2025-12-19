package org.baratinage.utils.equation;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class Test {

  public static void main(String[] args) {

    EquationParser eq0 = new EquationParser(
        "Q = C_r * B_w * (2 * g) ^ (1 / 2)  * (h-b) ^ c");
    // "Q =
    // ispos(h-b)*ispos(r-(h-b))*K_s*sqrt(S_0)*r^(c+2)*2^(-c)*acos(1-(h-b)/r)*(1 -
    // sin(2*acos(1-(h-b)/r)) / (2*acos(1-(h-b)/r)) )^(c+1) +
    // isspos((h-b)-r)*isspos(2*r-(h-b))*K_s*sqrt(S_0)*r^(c+2)*2^(-c)*(3.1416-acos((h-b)/r-1))*(1
    // + sin(2*acos((h-b)/r-1)) / (2*3.1416 - 2*acos((h-b)/r-1)) )^(c+1)");
    Expr expr0 = eq0.parse();
    System.out.println(expr0);
    String latex = expr0.toLatex();
    LatexFormatter latexFormatter = new LatexFormatter(50);
    String latexMultiLine = latexFormatter.format(expr0);
    System.out.println(latex);
    System.out.println(latexMultiLine);
    System.out.println("----------------");

    EventQueue.invokeLater(() -> {
      try {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 150);

        TeXFormula formula = new TeXFormula(latex);
        TeXIcon icon = formula.createTeXIcon(
            TeXFormula.SANSSERIF,
            16);

        JLabel label = new JLabel();
        label.setIcon(icon);

        TeXFormula formula2 = new TeXFormula(latexMultiLine);
        TeXIcon icon2 = formula2.createTeXIcon(
            TeXFormula.SANSSERIF,
            16);

        JLabel label2 = new JLabel();
        label2.setIcon(icon2);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(label2, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setVisible(true);
      } catch (Exception e) {
        System.err.println(e);
      }
    });

  }
}
