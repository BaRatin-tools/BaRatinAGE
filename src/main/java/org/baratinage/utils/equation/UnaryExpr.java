package org.baratinage.utils.equation;

public class UnaryExpr extends Expr {
  String op;
  Expr expr;

  UnaryExpr(String op, Expr expr) {
    this.op = op;
    this.expr = expr;
  }

  @Override
  public String toString() {
    return "(" + op + " " + expr + ")";
  }

  @Override
  int precedence() {
    return 4;
  }

  @Override
  public String toLatex(int parentPrecedence) {
    String body = "-" + expr.toLatex(precedence());
    if (precedence() < parentPrecedence) {
      return "\\left(" + body + "\\right)";
    }
    return body;
  }
}