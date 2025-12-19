package org.baratinage.utils.equation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionExpr extends Expr {
  String name;
  List<Expr> args;

  FunctionExpr(String name, List<Expr> args) {
    this.name = name;
    this.args = args;
  }

  @Override
  public String toString() {
    return name + args;
  }

  @Override
  int precedence() {
    return 100;
  }

  private static final Map<String, String> KNOWN_FUNCS = Map.of(
      "sqrt", "\\sqrt",
      "tan", "\\tan",
      "sin", "\\sin",
      "cos", "\\cos",
      "acos", "\\arccos");

  @Override
  public String toLatex(int parentPrecedence) {
    String latexName = KNOWN_FUNCS.getOrDefault(name, "\\operatorname{" + name + "}");

    if (latexName.equals("\\sqrt") && args.size() == 1) {
      return "\\sqrt{" + args.get(0).toLatex(0) + "}";
    }

    String joined = args.stream()
        .map(a -> a.toLatex(0))
        .collect(Collectors.joining(", "));

    return latexName + "\\left(" + joined + "\\right)";
  }
}
