package org.baratinage.utils.equation;

public class BinaryExpr extends Expr {
  String op;
  Expr left, right;

  BinaryExpr(String op, Expr left, Expr right) {
    this.op = op;
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return "(" + op + " " + left + " " + right + ")";
  }

  @Override
  int precedence() {
    return switch (op) {
      case "+", "-" -> 1;
      case "*", "/" -> 2;
      case "^" -> 3;
      default -> 0;
    };
  }

  @Override
  public String toLatex(int parentPrecedence) {
    String result;

    if (op.equals("/")) {
      // result = "\\frac{" + left.toLatex(0) + "}{" + right.toLatex(0) + "}";
      result = left.toLatex(0) + " / " + right.toLatex(0);
    } else if (op.equals("*")) {
      result = left.toLatex(0) + " \\cdot " + right.toLatex(0);
    } else if (op.equals("^")) {
      result = left.toLatex(precedence()) +
          "^{" + right.toLatex(0) + "}";
    } else {
      String l = left.toLatex(precedence());
      String r = right.toLatex(precedence() + (op.equals("-") ? 1 : 0));
      result = l + " " + op + " " + r;
    }

    if (precedence() < parentPrecedence) {
      return "\\left(" + result + "\\right)";
    }
    return result;
  }

}
