package org.baratinage.ui.container;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class SimpleGridPanel extends JPanel {

  /*
   * ==========================================================
   * Public API
   * ==========================================================
   */

  public SimpleGridPanel() {
    setLayout(null);
  }

  // columns

  public void setColumns(int n) {
    setColumns(n, auto());
  }

  public void setColumns(int n, Track defaultCol) {
    this.columns = new ArrayList<>();
    for (int k = 0; k < n; k++) {
      this.columns.add(defaultCol);
    }
    revalidate();
  }

  public void setColumns(Track... cols) {
    this.columns = List.of(cols);
    revalidate();
  }

  public void setColumn(int index, Track col) {
    this.columns = setAndFill(this.columns, col, index, 1, auto());
    revalidate();
  }

  // rows

  public void setDefaultRow(Track defaultRow) {
    this.defaultRow = defaultRow;
  }

  public void setRow(int index, Track row) {
    this.rows = setAndFill(this.rows, row, index, 1, defaultRow);
  }

  public int getRowCount() {
    return this.rows.size();
  }

  public void removeRows(int from, int to) {
    if (to >= rows.size()) {
      to = rows.size() - 1;
    }
    if (from < 0 || to < from || to >= rows.size()) {
      return;
    }

    int removedCount = to - from + 1;

    // Remove affected components
    Iterator<Map.Entry<Component, CellConstraints>> it = constraintMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry<Component, CellConstraints> e = it.next();
      Component comp = e.getKey();
      CellConstraints cc = e.getValue();

      int compTop = cc.row;
      int compBottom = cc.row + cc.rowSpan - 1;

      // If any intersection, remove component
      if (compBottom >= from && compTop <= to) {
        super.remove(comp);
        it.remove();
      }
    }

    // Shift remaining components upward
    for (CellConstraints cc : constraintMap.values()) {
      if (cc.row > to) {
        cc.row -= removedCount;
      }
    }

    // Remove row definitions
    rows = new ArrayList<>(rows);
    for (int i = 0; i < removedCount; i++) {
      rows.remove(from);
    }

    revalidate();
    repaint();
  }

  // gaps and padding

  public void setGaps(int gap) {
    setGaps(gap, gap);
  }

  public void setGaps(int columnGap, int rowGap) {
    this.columnGap = Math.max(0, columnGap);
    this.rowGap = Math.max(0, rowGap);
    revalidate();
  }

  public void setPadding(int pad) {
    padding = new Insets(pad, pad, pad, pad);
  }

  // tracks and cell

  public static Track fixed(int px) {
    return new Track(TrackType.FIXED, px, 0);
  }

  public static Track grow() {
    return new Track(TrackType.GROW, 0, 1);
  }

  public static Track grow(float weight) {
    return new Track(TrackType.GROW, 0, weight);
  }

  public static Track auto() {
    return new Track(TrackType.AUTO, 0, 0);
  }

  public static CellConstraints cell(int col, int row) {
    return new CellConstraints(col, row);
  }

  // cell

  @Override
  public void add(Component comp, Object constraints) {
    if (!(constraints instanceof CellConstraints cc)) {
      throw new IllegalArgumentException("Constraints must be CellConstraints");
    }
    constraintMap.put(comp, cc);
    this.rows = setAndFill(this.rows, defaultRow, cc.row, cc.rowSpan, defaultRow);
    super.add(comp);
  }

  @Override
  public Component add(Component comp) {
    CellConstraints cc = getDefaultCellConstraints();
    add(comp, cc);
    return comp;
  }

  public void removeComponent(Component comp) {
    CellConstraints cc = constraintMap.remove(comp);
    if (cc == null) {
      return;
    }
    super.remove(comp);
    revalidate();
    repaint();
  }

  /*
   * ==========================================================
   * Layout
   * ==========================================================
   */

  @Override
  public void doLayout() {
    Insets in = getInsets();

    int availW = getWidth() - in.left - in.right - padding.left - padding.right;
    int availH = getHeight() - in.top - in.bottom - padding.top - padding.bottom;

    int totalColGap = Math.max(0, columns.size() - 1) * columnGap;
    int totalRowGap = Math.max(0, rows.size() - 1) * rowGap;

    int[] colWidths = computeTrackSizes(
        columns,
        availW - totalColGap,
        true);
    int[] rowHeights = computeTrackSizes(
        rows,
        availH - totalRowGap,
        false);

    int[] colX = prefixSum(colWidths, columnGap, in.left + padding.left);
    int[] rowY = prefixSum(rowHeights, rowGap, in.top + padding.top);

    for (Component c : getComponents()) {
      CellConstraints cc = constraintMap.get(c);

      int x = colX[cc.col];
      int y = rowY[cc.row];

      int w = span(colWidths, columnGap, cc.col, cc.colSpan);
      int h = span(rowHeights, rowGap, cc.row, cc.rowSpan);

      Dimension pref = c.getPreferredSize();

      int cw = cc.hAlign.size(w, pref.width);
      int ch = cc.vAlign.size(h, pref.height);

      cw = Math.min(cw, w);
      ch = Math.min(ch, h);

      int dx = cc.hAlign.offset(w, cw);
      int dy = cc.vAlign.offset(h, ch);

      c.setBounds(x + dx, y + dy, cw, ch);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Insets in = getInsets();

    int colCount = columns.size();
    int rowCount = rows.size();

    if (colCount == 0 || rowCount == 0) {
      return new Dimension(
          in.left + in.right + padding.left + padding.right,
          in.top + in.bottom + padding.top + padding.bottom);
    }

    int[] colWidths = new int[colCount];
    int[] rowHeights = new int[rowCount];

    // 1. Fixed tracks
    for (int c = 0; c < colCount; c++) {
      Track t = columns.get(c);
      if (t.type == TrackType.FIXED) {
        colWidths[c] = t.value;
      }
    }

    for (int r = 0; r < rowCount; r++) {
      Track t = rows.get(r);
      if (t.type == TrackType.FIXED) {
        rowHeights[r] = t.value;
      }
    }

    // 2. Non-spanning components (AUTO & GROW behave the same here)
    for (Map.Entry<Component, CellConstraints> e : constraintMap.entrySet()) {
      Component comp = e.getKey();
      CellConstraints cc = e.getValue();

      Dimension pref = comp.getPreferredSize();

      if (cc.colSpan == 1) {
        colWidths[cc.col] = Math.max(colWidths[cc.col], pref.width);
      }

      if (cc.rowSpan == 1) {
        rowHeights[cc.row] = Math.max(rowHeights[cc.row], pref.height);
      }
    }

    // 3. Spanning components
    for (Map.Entry<Component, CellConstraints> e : constraintMap.entrySet()) {
      Component comp = e.getKey();
      CellConstraints cc = e.getValue();

      Dimension pref = comp.getPreferredSize();

      if (cc.colSpan > 1) {
        int span = 0;
        for (int c = 0; c < cc.colSpan; c++) {
          span += colWidths[cc.col + c];
        }
        span += columnGap * (cc.colSpan - 1);

        if (span < pref.width) {
          int extra = pref.width - span;
          distributeExtra(colWidths, columns, cc.col, cc.colSpan, extra);
        }
      }

      if (cc.rowSpan > 1) {
        int span = 0;
        for (int r = 0; r < cc.rowSpan; r++) {
          span += rowHeights[cc.row + r];
        }
        span += rowGap * (cc.rowSpan - 1);

        if (span < pref.height) {
          int extra = pref.height - span;
          distributeExtra(rowHeights, rows, cc.row, cc.rowSpan, extra);
        }
      }
    }

    int totalWidth = Arrays.stream(colWidths).sum()
        + columnGap * Math.max(0, colCount - 1)
        + in.left + in.right + padding.left + padding.right;

    int totalHeight = Arrays.stream(rowHeights).sum()
        + rowGap * Math.max(0, rowCount - 1)
        + in.top + in.bottom + padding.top + padding.bottom;

    return new Dimension(totalWidth, totalHeight);
  }

  /*
   * ==========================================================
   * Internal helpers
   * ==========================================================
   */

  private CellConstraints getDefaultCellConstraints() {

    // Determine how many rows currently exist
    int colCount = columns.size();
    int rowCount = rows.isEmpty() ? 1 : rows.size();

    // Occupancy grid
    boolean[][] occupied = new boolean[rowCount][colCount];

    // Mark occupied cells from existing components
    for (CellConstraints cc : constraintMap.values()) {
      for (int r = cc.row; r < cc.row + cc.rowSpan; r++) {
        for (int c = cc.col; c < cc.col + cc.colSpan; c++) {
          // Expand rows if needed
          if (r >= rowCount) {
            occupied = growOccupied(occupied, rowCount + 1, colCount, rowCount);
            rowCount++;
          }
          occupied[r][c] = true;
        }
      }
    }

    // Find first empty cell
    for (int r = 0;; r++) {
      if (r >= rowCount) {
        occupied = growOccupied(occupied, rowCount + 1, colCount, rowCount);
        rowCount++;
      }

      for (int c = 0; c < colCount; c++) {
        if (!occupied[r][c]) {
          CellConstraints cc = new CellConstraints(c, r);
          return cc;
        }
      }
    }
  }

  private static List<Track> setAndFill(
      List<Track> tracks,
      Track newTrack,
      int index,
      int span,
      Track defaultTrack) {
    for (int k = 0; k < index; k++) {
      if (k >= tracks.size()) {
        tracks.add(defaultTrack);
      }
    }
    if (index < tracks.size()) {
      for (int k = index; k < index + span; k++) {
        tracks.set(k, newTrack);
      }
    } else {
      for (int k = index; k < index + span; k++) {
        tracks.add(newTrack);
      }
    }
    return tracks;
  }

  private void distributeExtra(
      int[] sizes,
      List<Track> tracks,
      int start,
      int count,
      int extra) {
    // Prefer AUTO and GROW tracks, avoid FIXED if possible
    List<Integer> targets = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Track t = tracks.get(start + i);
      if (t.type != TrackType.FIXED) {
        targets.add(start + i);
      }
    }

    if (targets.isEmpty()) {
      // All fixed → distribute evenly anyway
      for (int i = 0; i < count; i++) {
        sizes[start + i] += extra / count;
      }
      return;
    }

    int per = extra / targets.size();
    int rem = extra % targets.size();

    for (int i = 0; i < targets.size(); i++) {
      int idx = targets.get(i);
      sizes[idx] += per + (i < rem ? 1 : 0);
    }
  }

  private static boolean[][] growOccupied(boolean[][] old, int newRows, int cols, int oldRows) {
    boolean[][] n = new boolean[newRows][cols];
    for (int r = 0; r < oldRows; r++) {
      System.arraycopy(old[r], 0, n[r], 0, cols);
    }
    return n;
  }

  private int[] computeTrackSizes(List<Track> tracks, int available, boolean horizontal) {
    int count = tracks.size();
    int[] sizes = new int[count];

    int used = 0;
    float totalGrow = 0;

    // Fixed
    for (int i = 0; i < count; i++) {
      Track t = tracks.get(i);
      if (t.type == TrackType.FIXED) {
        sizes[i] = t.value;
        used += t.value;
      }
    }

    // Auto
    for (int i = 0; i < count; i++) {
      Track t = tracks.get(i);
      if (t.type == TrackType.AUTO) {
        int max = 0;
        for (Map.Entry<Component, CellConstraints> e : constraintMap.entrySet()) {
          CellConstraints cc = e.getValue();
          if ((horizontal && cc.col == i && cc.colSpan == 1) ||
              (!horizontal && cc.row == i && cc.rowSpan == 1)) {
            Dimension d = e.getKey().getPreferredSize();
            max = Math.max(max, horizontal ? d.width : d.height);
          }
        }
        sizes[i] = max;
        used += max;
      }
    }

    // Grow
    for (Track t : tracks) {
      if (t.type == TrackType.GROW) {
        totalGrow += t.weight;
      }
    }

    int remaining = Math.max(0, available - used);

    for (int i = 0; i < count; i++) {
      Track t = tracks.get(i);
      if (t.type == TrackType.GROW) {
        sizes[i] = Math.round(remaining * (t.weight / totalGrow));
      }
    }

    return sizes;
  }

  private int[] prefixSum(int[] sizes, int gap, int start) {
    int[] pos = new int[sizes.length];
    int acc = start;
    for (int i = 0; i < sizes.length; i++) {
      pos[i] = acc;
      acc += sizes[i] + gap;
    }
    return pos;
  }

  private int span(int[] sizes, int gap, int start, int count) {
    int sum = 0;
    for (int i = 0; i < count; i++) {
      sum += sizes[start + i];
    }
    if (count > 1) {
      sum += gap * (count - 1);
    }
    return sum;
  }

  /*
   * ==========================================================
   * Data models
   * ==========================================================
   */

  private Track defaultRow = auto();
  private List<Track> columns = new ArrayList<>();
  private List<Track> rows = new ArrayList<>();
  private final Map<Component, CellConstraints> constraintMap = new HashMap<>();

  private int columnGap = 0;
  private int rowGap = 0;
  private Insets padding = new Insets(0, 0, 0, 0);

  public static class Track {
    final TrackType type;
    final int value;
    final float weight;

    Track(TrackType type, int value, float weight) {
      this.type = type;
      this.value = value;
      this.weight = weight;
    }
  }

  enum TrackType {
    FIXED, AUTO, GROW
  }

  public enum Align {
    START {
      int offset(int cell, int item) {
        return 0;
      }

      int size(int cell, int pref) {
        return pref;
      }
    },
    CENTER {
      int offset(int cell, int item) {
        return (cell - item) / 2;
      }

      int size(int cell, int pref) {
        return pref;
      }
    },
    END {
      int offset(int cell, int item) {
        return cell - item;
      }

      int size(int cell, int pref) {
        return pref;
      }
    },
    STRETCH {
      int offset(int cell, int item) {
        return 0;
      }

      int size(int cell, int pref) {
        return cell;
      }
    };

    abstract int offset(int cell, int item);

    abstract int size(int cell, int pref);
  }

  public static class CellConstraints {
    int col;
    int row;
    int colSpan = 1;
    int rowSpan = 1;
    Align hAlign = Align.STRETCH;
    Align vAlign = Align.STRETCH;

    CellConstraints(int col, int row) {
      this.col = col;
      this.row = row;
    }

    public CellConstraints span(int cols, int rows) {
      this.colSpan = cols;
      this.rowSpan = rows;
      return this;
    }

    public CellConstraints align(Align h, Align v) {
      this.hAlign = h;
      this.vAlign = v;
      return this;
    }
  }
}
