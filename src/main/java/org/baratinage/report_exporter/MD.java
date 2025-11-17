package org.baratinage.report_exporter;

import java.util.ArrayList;
import java.util.List;

public class MD {

  public static String h(int level, String title) {
    return "%s %s\n".formatted("#".repeat(level), title);
  }

  public static String img(String src, String alt) {
    return "![%s](%s)".formatted(alt, src);
  }

  public static String p(String... text) {
    String txt = String.join("\n", text);
    return "\n%s\n".formatted(txt);
  }

  public static String code(String lang, String code) {
    return "\n```%s\n%s\n```\n".formatted(lang, code);
  }

  private static String tableCell(String str, int width) {
    return " %s%s ".formatted(str, " ".repeat(width - str.length()));
  }

  public static String rows(List<String[]> rows, boolean hasHeaders) {
    if (rows.isEmpty()) {
      return "";
    }
    int nCol = rows.get(0).length;
    int nRow = rows.size();
    // get column widths
    List<Integer> widths = new ArrayList<>();
    for (int i = 0; i < nCol; i++) {
      int s = 0;
      for (int j = 0; j < nRow; j++) {
        s = Math.max(s, rows.get(j)[i].length());
      }
      widths.add(s);
    }

    // Build table
    StringBuilder mdTbl = new StringBuilder();

    mdTbl.append("|");
    for (int i = 0; i < nCol; i++) {
      mdTbl.append(hasHeaders ? tableCell(rows.get(0)[i], widths.get(i)) : " ".repeat(widths.get(i) + 2));
      mdTbl.append("|");
    }
    mdTbl.append("\n");
    mdTbl.append("|");
    for (int k = 0; k < nCol; k++) {
      mdTbl.append("-".repeat(widths.get(k) + 2));
      mdTbl.append("|");
    }
    mdTbl.append("\n");

    for (int j = hasHeaders ? 1 : 0; j < nRow; j++) {
      mdTbl.append("|");
      for (int i = 0; i < nCol; i++) {
        mdTbl.append(tableCell(rows.get(j)[i], widths.get(i)));
        mdTbl.append("|");
      }
      mdTbl.append("\n");
    }
    return mdTbl.toString();
  }

  public static String columns(List<String[]> columns, boolean hasHeaders) {
    if (columns.isEmpty()) {
      return "";
    }
    int nCol = columns.size();
    int nRow = columns.get(0).length;
    // get column widths
    List<Integer> widths = new ArrayList<>();
    for (String[] col : columns) {
      int s = 0;
      for (String cell : col) {
        s = Math.max(s, cell.length());
      }
      widths.add(s);
    }

    // Build table
    StringBuilder mdTbl = new StringBuilder();

    // header
    mdTbl.append("|");
    for (int k = 0; k < nCol; k++) {
      String[] col = columns.get(k);
      mdTbl.append(hasHeaders ? tableCell(col[0], widths.get(k)) : " ".repeat(widths.get(k) + 2));
      mdTbl.append("|");
    }
    mdTbl.append("\n");
    mdTbl.append("|");
    for (int k = 0; k < nCol; k++) {
      mdTbl.append("-".repeat(widths.get(k) + 2));
      mdTbl.append("|");
    }
    mdTbl.append("\n");

    // content
    for (int i = hasHeaders ? 1 : 0; i < nRow; i++) {
      mdTbl.append("|");
      for (int k = 0; k < nCol; k++) {
        String[] col = columns.get(k);
        mdTbl.append(tableCell(col[i], widths.get(k)));
        mdTbl.append("|");
      }
      mdTbl.append("\n");
    }
    return mdTbl.toString();
  }

}
