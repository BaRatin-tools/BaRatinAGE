package org.baratinage.ui.component.data_import.column_mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.swing.JLabel;

import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.data_import.IDataTableColumn;
import org.baratinage.ui.container.SimpleFlowPanel;

public abstract class SingleColumnMapper<A> extends SimpleFlowPanel implements IDataTableColumn {

  public final SimpleComboBox combobox;
  public final JLabel label;

  private String[] guessPatterns = new String[0];
  private String[] headers = new String[0];

  protected List<String[]> data = new ArrayList<>();
  protected String mv;

  public SingleColumnMapper() {
    super();
    setGap(5);
    combobox = new SimpleComboBox();
    label = new JLabel();
    addChild(label, false);
    addChild(combobox, true);

    combobox.addChangeListener(l -> {
      updateState();
    });
  }

  public void setGuessPatterns(String... patterns) {
    this.guessPatterns = patterns;
  }

  public void guessColumnIndex(boolean force) {
    if (combobox.getSelectedIndex() != -1 && !force) {
      return;
    }
    setIndex(getIndexGuess(headers, -1, guessPatterns));
  }

  public int getIndex() {
    return combobox.getSelectedIndex();
  }

  public void setIndex(int index) {
    if (index < 0 || index >= headers.length) {
      index = -1;
    }
    combobox.setSelectedItem(index);
  }

  public static int getIndexGuess(String[] strings, int defaultIndex, String... patterns) {
    Predicate<String> allPredicates = (String str) -> true;
    for (String pattern : patterns) {
      allPredicates = allPredicates.and(Pattern.compile(pattern).asMatchPredicate());
    }
    for (int k = 0; k < strings.length; k++) {
      if (allPredicates.test(strings[k])) {
        return k;
      }
    }
    return defaultIndex;
  }

  public void setData(List<String[]> data, String[] headers, String mv) {
    this.data = data;
    this.headers = headers;
    this.mv = mv;
    combobox.resetItems(headers);
    updateState();
  }

  protected abstract void updateState();

  protected abstract Set<Integer> getMissingIndices();

  public abstract Set<Integer> getInvalidIndices();

  public String[] getUnparsedColumn() {
    int index = getIndex();
    return data == null || data.size() == 0 || index < 0 || index >= data.size() ? null : data.get(index);
  }

  public int getRowCount() {
    if (data.size() == 0) {
      return 0;
    }
    return data.get(0).length;
  }

  @Override
  public boolean isMissing(int colIndex, int rowIndex) {
    return getMissingIndices().contains(rowIndex);

  }

  @Override
  public boolean isInvalid(int colIndex, int rowIndex) {
    return getInvalidIndices().contains(rowIndex);
  }

  @Override
  public String getLabelText(int colIndex, int rowIndex) {
    if (isMissing(colIndex, rowIndex)) {
      return "-";
    } else if (isInvalid(colIndex, rowIndex)) {
      return "??";
    } else {
      String[] unparsed = getUnparsedColumn();
      return unparsed == null ? "?" : unparsed[rowIndex];
    }
  }

  @Override
  public String getSecondaryLabelText(int colIndex, int rowIndex) {
    if (isMissing(colIndex, rowIndex) || isInvalid(colIndex, rowIndex)) {
      String[] unparsed = getUnparsedColumn();
      return unparsed == null ? "" : String.format(" (%s) ", unparsed[rowIndex]);
    } else {
      return "";
    }
  }

}
