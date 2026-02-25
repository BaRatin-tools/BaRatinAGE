package org.baratinage.utils.equation;

// import java.util.ArrayList;
// import java.util.List;

public abstract class Expr implements LatexRenderable {
  public abstract String toString();

  abstract int precedence();

  public abstract String toLatex(int parentPrecedence);

  public String toLatex() {
    return toLatex(0);
  }

  // public static boolean isLong(Expr e) {
  // return e.toLatex().length() > 120;
  // }

  // public static String formatExpression(Expr expr) {
  // String inline = expr.toLatex(0);
  // if (inline.length() <= 120) {
  // return inline;
  // }

  // List<Expr> terms = new ArrayList<>();
  // List<String> ops = new ArrayList<>();
  // collectTerms(expr, terms, ops);

  // if (terms.size() <= 1) {
  // return inline;
  // }

  // StringBuilder sb = new StringBuilder();
  // sb.append("\\begin{aligned}\n");
  // sb.append(" ").append(terms.get(0).toLatex(0)).append(" \\\\\n");

  // for (int i = 1; i < terms.size(); i++) {
  // sb.append(" &\\quad ")
  // .append(ops.get(i - 1))
  // .append(" ")
  // .append(terms.get(i).toLatex(0))
  // .append(" \\\\\n");
  // }

  // sb.append("\\end{aligned}");
  // return sb.toString();
  // }

  // public static String formatEquation(EquationExpr eq) {
  // String lhs = eq.left.toLatex(0);
  // String rhsInline = eq.right.toLatex(0);

  // if (rhsInline.length() <= 120) {
  // return lhs + " = " + rhsInline;
  // }

  // String rhs = formatExpression(eq.right);

  // return "\\begin{aligned}\n"
  // + lhs + " &= "
  // + rhs.replace("\\begin{aligned}", "")
  // .replace("\\end{aligned}", "")
  // + "\n\\end{aligned}";
  // }

  // private static void collectTerms(Expr expr, List<Expr> terms, List<String>
  // ops) {
  // if (expr instanceof BinaryExpr b &&
  // (b.op.equals("+") || b.op.equals("-"))) {

  // collectTerms(b.left, terms, ops);
  // ops.add(b.op);
  // collectTerms(b.right, terms, ops);
  // } else {
  // terms.add(expr);
  // }
  // }
}