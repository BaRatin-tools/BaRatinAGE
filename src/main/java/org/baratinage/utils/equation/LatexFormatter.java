package org.baratinage.utils.equation;

import java.util.ArrayList;
import java.util.List;

public class LatexFormatter {

  class LatexLine {
    final String prefix; // e.g. "", "&", "&\\quad"
    final String content;

    LatexLine(String prefix, String content) {
      this.prefix = prefix;
      this.content = content;
    }
  }

  private final int maxWidth;

  public LatexFormatter(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public String format(Expr expr) {
    if (expr instanceof EquationExpr eq) {
      return formatEquation(eq);
    }
    return formatExpression(expr, "", false);
  }

  private String formatEquation(EquationExpr eq) {
    String lhs = eq.left.toLatex(0);

    List<LatexLine> rhsLines = formatExprLines(eq.right, "&", true);

    StringBuilder sb = new StringBuilder();
    sb.append("\\begin{aligned}\n");

    sb.append(lhs).append(" &= ").append(rhsLines.get(0).content).append(" \\\\\n");

    for (int i = 1; i < rhsLines.size(); i++) {
      LatexLine line = rhsLines.get(i);
      sb.append(" ").append(line.prefix).append(" ").append(line.content).append(" \\\\\n");
    }

    sb.append("\\end{aligned}");
    return sb.toString();
  }

  private List<LatexLine> formatExprLines(Expr expr,
      String alignPrefix,
      boolean allowBreak) {

    String inline = expr.toLatex(0);
    if (inline.length() <= maxWidth || !allowBreak) {
      return List.of(new LatexLine(alignPrefix, inline));
    }

    // Break sums at any depth
    if (expr instanceof BinaryExpr b &&
        (b.op.equals("+") || b.op.equals("-"))) {

      List<LatexLine> leftLines = formatExprLines(b.left, alignPrefix, true);

      List<LatexLine> rightLines = formatExprLines(b.right, alignPrefix + "\\quad", true);

      List<LatexLine> result = new ArrayList<>();
      result.addAll(leftLines);

      // First right line gets operator
      LatexLine firstRight = rightLines.get(0);
      result.add(new LatexLine(
          alignPrefix,
          b.op + " " + firstRight.content));

      // Remaining right lines
      for (int i = 1; i < rightLines.size(); i++) {
        result.add(rightLines.get(i));
      }

      return result;
    }

    // Do not break products, powers, or functions
    return List.of(new LatexLine(alignPrefix, inline));
  }

  private String formatExpression(Expr expr, String align, boolean allowBreak) {
    List<LatexLine> lines = formatExprLines(expr, align, allowBreak);

    if (lines.size() == 1) {
      return lines.get(0).content;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("\\begin{aligned}\n");
    for (LatexLine line : lines) {
      sb.append(line.prefix).append(" ").append(line.content).append(" \\\\\n");
    }
    sb.append("\\end{aligned}");
    return sb.toString();
  }

}
