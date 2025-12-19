package org.baratinage.utils.equation;

import java.math.BigDecimal;

public class NumberExpr extends Expr {
  double value;

  NumberExpr(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  @Override
  int precedence() {
    return 100;
  }

  @Override
  public String toLatex(int parentPrecedence) {
    if (Double.isNaN(value))
      return "NaN";
    if (Double.isInfinite(value))
      return value > 0 ? "\\infty" : "-\\infty";

    BigDecimal bd = BigDecimal.valueOf(value).stripTrailingZeros();

    // Plain integer or decimal
    if (bd.scale() <= 0) {
      return bd.toPlainString();
    }

    // Scientific notation
    int exponent = bd.precision() - 1 - bd.scale();
    if (exponent >= 4 || exponent <= -4) {
      BigDecimal mantissa = bd.movePointLeft(exponent);
      return mantissa.toPlainString() + "\\times10^{" + exponent + "}";
    }

    return bd.toPlainString();
  }
}