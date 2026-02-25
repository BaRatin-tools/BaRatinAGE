package org.baratinage.utils.equation;

public class VariableExpr extends Expr {
  String name;

  VariableExpr(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  int precedence() {
    return 100;
  }

  @Override
  public String toLatex(int parentPrecedence) {
    if (name.contains("_")) {
      String[] parts = name.split("_", 2);
      return parts[0] + "_{" + parts[1] + "}";
    }
    return name;
  }
}
