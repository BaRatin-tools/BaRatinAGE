package org.baratinage.utils;

import java.util.List;
import java.util.Map;

/**
 * Display symbol of a parameter, e.g. {@code tau} with subscripts {@code 1, 2}.
 *
 * <p>
 * The base is stored as Unicode ({@code "τ"}) and rendered as HTML, LaTeX or
 * plain text. Instances are immutable and have value equality.
 * </p>
 *
 * <p>
 * The plain-text form uses {@code _} as the subscript separator
 * ({@code "τ_1_2"}), matching the convention already used for hydraulic
 * control symbols ({@code "K_s"}, {@code "B_p"}, {@code "γ_1"}).
 * </p>
 */
public record ParameterSymbol(String base, List<String> subscripts) {

  private static final String HTML_SUBSCRIPT_SEPARATOR = ",";
  private static final String LATEX_SUBSCRIPT_SEPARATOR = ",";
  private static final String PLAIN_TEXT_SUBSCRIPT_SEPARATOR = "_";

  private static final Map<String, String> LATEX_GREEK = Map.ofEntries(
      Map.entry("α", "\\alpha"),
      Map.entry("β", "\\beta"),
      Map.entry("γ", "\\gamma"),
      Map.entry("δ", "\\delta"),
      Map.entry("ε", "\\epsilon"),
      Map.entry("ζ", "\\zeta"),
      Map.entry("η", "\\eta"),
      Map.entry("θ", "\\theta"),
      Map.entry("ι", "\\iota"),
      Map.entry("κ", "\\kappa"),
      Map.entry("λ", "\\lambda"),
      Map.entry("μ", "\\mu"),
      Map.entry("ν", "\\nu"),
      Map.entry("ξ", "\\xi"),
      Map.entry("π", "\\pi"),
      Map.entry("ρ", "\\rho"),
      Map.entry("σ", "\\sigma"),
      Map.entry("τ", "\\tau"),
      Map.entry("υ", "\\upsilon"),
      Map.entry("φ", "\\varphi"),
      Map.entry("χ", "\\chi"),
      Map.entry("ψ", "\\psi"),
      Map.entry("ω", "\\omega"),
      Map.entry("Γ", "\\Gamma"),
      Map.entry("Δ", "\\Delta"),
      Map.entry("Θ", "\\Theta"),
      Map.entry("Λ", "\\Lambda"),
      Map.entry("Ξ", "\\Xi"),
      Map.entry("Π", "\\Pi"),
      Map.entry("Σ", "\\Sigma"),
      Map.entry("Υ", "\\Upsilon"),
      Map.entry("Φ", "\\Phi"),
      Map.entry("Ψ", "\\Psi"),
      Map.entry("Ω", "\\Omega"));

  public ParameterSymbol {
    if (base == null || base.isBlank()) {
      throw new IllegalArgumentException("ParameterSymbol base must not be blank");
    }
    subscripts = subscripts == null ? List.of() : List.copyOf(subscripts);
  }

  public static ParameterSymbol of(String base, String... subscripts) {
    return new ParameterSymbol(base, subscripts == null ? List.of() : List.of(subscripts));
  }

  /**
   * Convenience for purely numeric subscripts, e.g. {@code ofIndices("τ", 1, 2)}.
   */
  public static ParameterSymbol ofIndices(String base, int... indices) {
    String[] subscripts = new String[indices.length];
    for (int k = 0; k < indices.length; k++) {
      subscripts[k] = Integer.toString(indices[k]);
    }
    return of(base, subscripts);
  }

  /**
   * Fallback for columns whose semantics are unknown
   */
  public static ParameterSymbol guess(String header) {
    if (header == null || header.isBlank()) {
      return of("?");
    }
    int firstUnderscore = header.indexOf('_');
    if (firstUnderscore >= 0) {
      String base = header.substring(0, firstUnderscore);
      String rest = header.substring(firstUnderscore + 1);
      return rest.isEmpty() ? of(base) : of(base, rest.split("_"));
    }
    int digitStart = header.length();
    while (digitStart > 0 && Character.isDigit(header.charAt(digitStart - 1))) {
      digitStart--;
    }
    if (digitStart > 0 && digitStart < header.length()) {
      return of(header.substring(0, digitStart), header.substring(digitStart));
    }
    return of(header);
  }

  public boolean hasSubscripts() {
    return !subscripts.isEmpty();
  }

  /**
   * HTML, ready for Swing labels. Same contract as the former {@code htmlName}.
   */
  public String toHtml() {
    StringBuilder sb = new StringBuilder("<html>").append(escapeHtml(base));
    if (hasSubscripts()) {
      sb.append("<sub>");
      for (int k = 0; k < subscripts.size(); k++) {
        if (k > 0) {
          sb.append(HTML_SUBSCRIPT_SEPARATOR);
        }
        sb.append(escapeHtml(subscripts.get(k)));
      }
      sb.append("</sub>");
    }
    return sb.append("</html>").toString();
  }

  /** LaTeX math fragment, e.g. {@code \tau_{1,2}}. */
  public String toLatex() {
    StringBuilder sb = new StringBuilder(toLatexBase());
    if (hasSubscripts()) {
      sb.append("_{")
          .append(String.join(LATEX_SUBSCRIPT_SEPARATOR, subscripts))
          .append("}");
    }
    return sb.toString();
  }

  /** Plain text, e.g. {@code τ_1_2}. */
  public String toPlainText() {
    return hasSubscripts()
        ? base + PLAIN_TEXT_SUBSCRIPT_SEPARATOR
            + String.join(PLAIN_TEXT_SUBSCRIPT_SEPARATOR, subscripts)
        : base;
  }

  private String toLatexBase() {
    String greek = LATEX_GREEK.get(base);
    if (greek != null) {
      return greek;
    }
    // multi-letter ASCII names are set upright, not as a product of variables
    if (base.length() > 1 && base.matches("[A-Za-z]+")) {
      return "\\mathrm{" + base + "}";
    }
    return base.replace("_", "\\_");
  }

  private static String escapeHtml(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  @Override
  public String toString() {
    return toPlainText();
  }
}
