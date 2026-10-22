package org.baratinage.ui.baratin.rating_curve;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.baratin.rating_curve.RatingCurveParameters.Ctrl;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.equation.EquationParser;
import org.baratinage.utils.equation.Expr;
import org.baratinage.utils.equation.LatexFormatter;

/**
 * Builds the plain text and latex equations of the rating curve.
 *
 * The BaRatin and BaRatinBAC equations are built from the hydraulic controls
 * and the control matrix. Text file models define their equation as a formula
 * string, which is converted to latex by parsing it.
 */
public class RatingCurveEquationBuilder {

  /**
   * A stage range segment of the piecewise rating curve. A null lower (resp.
   * upper) bound means that the segment is not bounded on that side, i.e. there
   * is no transition stage below (resp. above) it.
   */
  private record Segment(Double lower, Double upper, List<Ctrl> controls) {
  };

  /**
   * The only syntactic differences between the plain text and the latex
   * equation.
   */
  private record Syntax(String product, String powOpen, String powClose, boolean isLatex) {
  };

  /**
   * The plain text and latex serializations of the same equation.
   */
  public record Equations(String text, String latex) {
  };

  private static final Syntax TEXT = new Syntax(" * ", " ^ ", "", false);
  private static final Syntax LATEX = new Syntax(" \\cdot ", " ^ {", "}", true);

  /**
   * When null, no rounding is applied, i.e. numbers are displayed with all the
   * digits of their double representation. NOTE: this will later be set from a
   * user preference.
   */
  private static final MathContext NUMBER_ROUNDING = null;

  /**
   * Build the piecewise rating curve equation from the hydraulic controls and
   * the control matrix.
   *
   * @param controls      the hydraulic controls
   * @param controlMatrix each row is a stage range segment and each column is a
   *                      hydraulic control
   */
  public static Equations fromControls(List<Ctrl> controls, boolean[][] controlMatrix) {
    List<Segment> segments = getSegments(controls, controlMatrix);
    return new Equations(toTextEquation(segments), toLatexEquation(segments));
  }

  /**
   * Convert the formula of a text file model to latex.
   *
   * @param formula the formula as written in the model configuration, i.e.
   *                without the left-hand side
   * @return the latex equation or null if the formula cannot be parsed
   */
  public static String latexFromFormula(String formula) {
    // the same conversion is applied to the user equation in the model
    // configuration panel (see QFHTextFileEquation.updateEquationLabel)
    try {
      Expr expr = new EquationParser("Q = " + formula).parse();
      return new LatexFormatter(50).format(expr);
    } catch (RuntimeException e) {
      // the formula cannot be parsed, no latex equation is displayed
      ConsoleLogger.warn(e);
      return null;
    }
  }

  /**
   * Build the piecewise equation as a list of segments. The first segment
   * corresponds to stages below the first transition stage, for which the
   * discharge is null.
   */
  private static List<Segment> getSegments(List<Ctrl> controls, boolean[][] controlMatrix) {
    int nSegments = controlMatrix.length;
    if (nSegments != controls.size()) {
      throw new IllegalArgumentException(
          String.format(
              "Rating curve equation: inconsistent control matrix (%d segments) and number of controls (%d)",
              nSegments, controls.size()));
    }

    List<Segment> segments = new ArrayList<>();
    // below the first transition stage the discharge is null
    segments.add(new Segment(null, controls.get(0).k().getMaxpost(), List.of()));

    for (int i = 0; i < nSegments; i++) { // for each stage range segment
      Double lower = controls.get(i).k().getMaxpost();
      Double upper = i + 1 < nSegments ? controls.get(i + 1).k().getMaxpost() : null;

      List<Ctrl> activeControls = new ArrayList<>();
      for (int j = 0; j <= i; j++) { // for each possibly active control
        if (controlMatrix[i][j]) {
          activeControls.add(controls.get(j));
        }
      }
      if (activeControls.isEmpty()) {
        ConsoleLogger.warn(
            String.format(
                "Rating curve equation: no active control for stage range %d, the discharge is displayed as null",
                i + 1));
      }
      segments.add(new Segment(lower, upper, activeControls));
    }
    return segments;
  }

  private static String toTextEquation(List<Segment> segments) {
    List<String> lines = new ArrayList<>(segments.size());
    for (Segment segment : segments) {
      String discharge = segment.controls().isEmpty()
          ? "0"
          : getTerms(segment.controls(), TEXT);
      lines.add(String.format("%s: Q = %s", getCondition(segment, TEXT), discharge));
    }
    return String.join("\n", lines);
  }

  private static String toLatexEquation(List<Segment> segments) {
    StringBuilder str = new StringBuilder("Q(h)=\\left\\{\\begin{array}{l l}\n");
    int nSegments = segments.size();
    for (int i = 0; i < nSegments; i++) {
      Segment segment = segments.get(i);
      String discharge = segment.controls().isEmpty()
          ? "0"
          : getTerms(segment.controls(), LATEX);
      str.append(discharge)
          .append(" & ")
          .append(getCondition(segment, LATEX));
      str.append(i == nSegments - 1 ? "\n" : " \\\\\n");
    }
    return str.append("\\end{array}\\right.").toString();
  }

  /**
   * Render the sum of the active controls contributions, i.e. the right-hand
   * side of the equation for one segment.
   */
  private static String getTerms(List<Ctrl> activeControls, Syntax syntax) {
    StringBuilder str = new StringBuilder();
    for (int k = 0; k < activeControls.size(); k++) {
      Ctrl ctrl = activeControls.get(k);
      double a = ctrl.a().getMaxpost();
      double b = ctrl.b().getMaxpost();
      double c = ctrl.c().getMaxpost();

      // the coefficient of the first term keeps its minus sign, the following
      // ones are prefixed by an explicit '+' or '-'
      if (k == 0 && a < 0) {
        str.append("- ");
      } else if (k > 0) {
        str.append(a < 0 ? " - " : " + ");
      }
      str.append(formatNumber(Math.abs(a), syntax))
          .append(syntax.product())
          .append("(h ")
          .append(formatSignedNumber(b, syntax))
          .append(")")
          .append(syntax.powOpen())
          .append(formatNumber(c, syntax))
          .append(syntax.powClose());
    }
    return str.toString();
  }

  private static String getCondition(Segment segment, Syntax syntax) {
    if (segment.lower() == null) {
      return "h < " + formatNumber(segment.upper(), syntax);
    }
    if (segment.upper() == null) {
      return "h > " + formatNumber(segment.lower(), syntax);
    }
    return String.format("%s < h < %s",
        formatNumber(segment.lower(), syntax),
        formatNumber(segment.upper(), syntax));
  }

  /**
   * Format a number, e.g. the b coefficient, as the right-hand side of an
   * addition or a subtraction.
   */
  private static String formatSignedNumber(double value, Syntax syntax) {
    return (value < 0 ? "+ " : "- ") + formatNumber(Math.abs(value), syntax);
  }

  private static String formatNumber(double value, Syntax syntax) {
    // NOTE: when the value is rounded, it is converted to its plain decimal
    // representation to prevent any chance of an exponent notation in latex
    String number = formatNumber(value);
    return syntax.isLatex() ? toLatexNumber(number) : number;
  }

  private static String formatNumber(double value) {
    return NUMBER_ROUNDING == null
        ? Double.toString(value)
        : new BigDecimal(value).round(NUMBER_ROUNDING).stripTrailingZeros().toPlainString();
  }

  private static String toLatexNumber(String number) {
    int exponentIndex = number.indexOf('E');
    if (exponentIndex < 0) {
      return number;
    }
    return String.format("%s \\times 10^{%s}",
        number.substring(0, exponentIndex),
        number.substring(exponentIndex + 1));
  }

  /**
   * Append the prediction-domain bounds to a plain-text equation.
   */
  static String appendStageBounds(String equation, Double hMin, Double hMax) {
    if (hMin == null || hMax == null) {
      return equation;
    }
    return equation
        + "\nh_min = " + formatNumber(hMin)
        + "\nh_max = " + formatNumber(hMax);
  }
}
