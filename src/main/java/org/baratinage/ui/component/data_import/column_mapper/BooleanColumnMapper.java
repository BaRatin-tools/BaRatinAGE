package org.baratinage.ui.component.data_import.column_mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.component.data_import.IDataTableColumn;
import org.baratinage.ui.container.BorderedSimpleFlowPanel;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class BooleanColumnMapper extends BorderedSimpleFlowPanel implements IDataTableColumn {

  // private final JLabel columnMapperLabel = new JLabel();
  private final StringColumnMapper columnMapper = new StringColumnMapper();
  private final JLabel trueStringFieldLabel = new JLabel();
  public final SimpleTextField trueStringField = new SimpleTextField();

  private String id = Misc.getTimeStampedId();

  public BooleanColumnMapper() {

    addChild(columnMapper.label, false);
    addChild(columnMapper.combobox, true);
    addChild(trueStringFieldLabel, false);
    addChild(trueStringField, true);

    columnMapper.combobox.addChangeListener(l -> {
      TimedActions.debounce(id + "_cb", AppSetup.CONFIG.DEBOUNCED_DELAY_MS, this::updateData);
    });
    trueStringField.addChangeListener(l -> {
      TimedActions.debounce(id + "_tf", AppSetup.CONFIG.DEBOUNCED_DELAY_MS, this::updateData);
    });

    T.t(this, columnMapper.label, false, "validity");
    T.t(this, trueStringFieldLabel, false, "validity_code");
  }

  private HashSet<Integer> mvIndices = new HashSet<>();

  private List<String[]> data;
  private String[] unparsed;
  private boolean[] parsed;
  private String mv;

  public void setData(List<String[]> data, String[] headers, String mv) {
    this.data = data;
    this.mv = mv;
    columnMapper.setData(data, headers, mv);
  }

  private void updateData() {
    int colIndex = columnMapper.getIndex();
    if (colIndex < 0 || colIndex >= data.size()) {
      unparsed = null;
      parsed = null;
      return;
    }
    String trueString = trueStringField.getText();
    unparsed = data.get(colIndex);
    // columnMapper
    parsed = new boolean[unparsed.length];
    for (int k = 0; k < unparsed.length; k++) {
      if (unparsed[k].equals(mv)) {
        mvIndices.add(k);
        parsed[k] = false;
      } else {
        Boolean i = trueString == null ? false : unparsed[k].equals(trueString);
        parsed[k] = i;
      }
    }
    fireChangeListeners();
  }

  public int getIndex() {
    return columnMapper.getIndex();
  }

  @Override
  public boolean isMissing(int colIndex, int rowIndex) {
    return mvIndices.contains(rowIndex);
  }

  @Override
  public boolean isInvalid(int colIndex, int rowIndex) {
    return false;
  }

  @Override
  public String getLabelText(int colIndex, int rowIndex) {
    if (unparsed == null) {
      return "";
    }
    return unparsed[rowIndex];
  }

  @Override
  public String getSecondaryLabelText(int colIndex, int rowIndex) {
    if (parsed == null) {
      return "";
    }
    boolean v = parsed[rowIndex];
    return String.format(
        "<html> <span style=\"color: %s\">%s</span></html>",
        v ? "green" : "red",
        v ? "\u2714" : "\u2716");

  }

  public boolean[] getParsedColumn() {
    return parsed;
  }

  private final List<ChangeListener> changeListeners = new ArrayList<>();

  public void addChangeListener(ChangeListener l) {
    changeListeners.add(l);
  }

  public void removeChangeListener(ChangeListener l) {
    changeListeners.remove(l);
  }

  public void fireChangeListeners() {
    for (ChangeListener l : changeListeners) {
      l.stateChanged(new ChangeEvent(this));
    }
  }

}
