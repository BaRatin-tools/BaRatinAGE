package org.baratinage.utils.equation;

public class EquationExpr extends Expr {
  Expr left;
  Expr right;

  EquationExpr(Expr left, Expr right) {
    this.left = left;
    this.right = right;
  }

  @Override
  int precedence() {
    return 0; // lowest possible
  }

  @Override
  public String toLatex(int parentPrecedence) {
    return left.toLatex(0) + " = " + right.toLatex(0);
  }

  @Override
  public String toString() {
    return "(= " + left + " " + right + ")";
  }
}
