package org.baratinage.ui.component.data_import.column_mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimplePopup;
import org.baratinage.ui.component.data_import.IDataTableColumn;
import org.baratinage.ui.container.BorderedSimpleFlowPanel;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class DateTimeColumnMapper extends BorderedSimpleFlowPanel implements IDataTableColumn {

  private final SimpleComboBox modeCombobox = new SimpleComboBox();

  private final StringColumnMapper dateTimeField = new StringColumnMapper();
  private final SimpleComboBox dateTimeFormat = new SimpleComboBox();

  private final StringColumnMapper dateField = new StringColumnMapper();
  private final StringColumnMapper timeField = new StringColumnMapper();
  private final SimpleComboBox dateFormat = new SimpleComboBox();
  private final SimpleComboBox timeFormat = new SimpleComboBox();
  private final IntegerColumnMapper yearField = new IntegerColumnMapper();
  private final IntegerColumnMapper monthField = new IntegerColumnMapper();
  private final IntegerColumnMapper dayField = new IntegerColumnMapper();
  private final IntegerColumnMapper hourField = new IntegerColumnMapper();
  private final IntegerColumnMapper minuteField = new IntegerColumnMapper();
  private final IntegerColumnMapper secondField = new IntegerColumnMapper();

  private final String id = Misc.getTimeStampedId();
  // private final DataParser dataParser;

  // public DateTimeColumnMapper(DataParser dataParser) {
  public DateTimeColumnMapper() {
    super(true);

    // this.dataParser = dataParser;

    // *******************************************
    // connecting listeners
    ChangeListener l = (e) -> {
      TimedActions.debounce(id, 250, this::updateData);
    };
    modeCombobox.addChangeListener(l);
    dateTimeField.combobox.addChangeListener(l);
    dateTimeFormat.addChangeListener(l);
    dateField.combobox.addChangeListener(l);
    dateFormat.addChangeListener(l);
    timeField.combobox.addChangeListener(l);
    timeFormat.addChangeListener(l);
    yearField.combobox.addChangeListener(l);
    monthField.combobox.addChangeListener(l);
    dayField.combobox.addChangeListener(l);
    hourField.combobox.addChangeListener(l);
    minuteField.combobox.addChangeListener(l);
    secondField.combobox.addChangeListener(l);

    // *******************************************
    // one column date/time
    SimpleFlowPanel oneColPanel = new SimpleFlowPanel();
    oneColPanel.setGap(5);

    // JButton dateTimeInfoButton = buildHelpButton(
    // true, true, true, true, true, true, true,
    // "y-M-d H:m:s.SSS = 2005-7-26 14:32:09.000");

    JButton dateTimeInfoButton = buildHelpButton(
        true, true, true,
        "y-M-d H:m:s = 2005-7-26 14:32:09",
        "d.M.y H:m = 26.7.2005 14:32",
        "y/M/d = 2005/07/26",
        "y-M-d H:m:s.SSS = 2005-7-26 14:32:09.000",
        "(ISO 8601) y-M-d'T'H:m:s.SSS'Z' = 2005-07-26T14:32:09.000Z");

    oneColPanel.addChild(dateTimeField, true);
    dateTimeFormat.setEmptyItem(null);
    dateTimeFormat.setEditable(true);
    dateTimeFormat.setItems(new String[] {
        "y/M/d H:m:s",
        "y-M-d H:m:s",
        "d/M/y H:m:s",
        "d-M-y H:m:s",
        "y/M/d H:m",
        "y-M-d H:m",
        "d/M/y H:m",
        "d-M-y H:m",
        "y/M/d",
        "y-M-d",
        "d/M/y",
        "d-M-y",
        "y-M-d'T'H:m:s.SSS'Z'",
    });
    oneColPanel.addChild(dateTimeFormat, true);
    oneColPanel.addChild(dateTimeInfoButton, false);

    // *******************************************
    // two columns date/time
    SimpleFlowPanel twoColPanel = new SimpleFlowPanel(true);
    twoColPanel.setGap(5);

    SimpleFlowPanel datePanel = new SimpleFlowPanel();
    datePanel.setGap(5);
    JLabel dateLabel = new JLabel();
    T.t(this, dateLabel, false, "date");
    dateFormat.setEmptyItem(null);
    dateFormat.setEditable(true);
    dateFormat.setItems(new String[] {
        "y/M/d",
        "y-M-d",
        "d/M/y",
        "d-M-y"
    });
    JButton dateInfoButton = buildHelpButton(
        true, false, false,
        "y-M-d = 2005-07-26", "d/M/y = 26/07/2005");

    datePanel.addChild(dateLabel, false);
    datePanel.addChild(dateField, true);
    datePanel.addChild(dateFormat, true);
    datePanel.addChild(dateInfoButton, false);

    SimpleFlowPanel timePanel = new SimpleFlowPanel();
    timePanel.setGap(5);
    JLabel timeLabel = new JLabel();
    T.t(this, timeLabel, false, "time_only");
    timeFormat.setEmptyItem(null);
    timeFormat.setEditable(true);
    timeFormat.setItems(new String[] {
        "H:m:s",
        "H:m",
    });
    JButton timeInfoButton = buildHelpButton(
        false, true, false,
        "H:m:s = 14:32:09", "H:m= 14:32");
    timePanel.addChild(timeLabel, false);
    timePanel.addChild(timeField, true);
    timePanel.addChild(timeFormat, true);
    timePanel.addChild(timeInfoButton, false);

    twoColPanel.addChild(datePanel, false);
    twoColPanel.addChild(timePanel, false);

    // *******************************************
    // six columns date/time
    GridPanel sixColPanel = new GridPanel();
    sixColPanel.setGap(5);
    sixColPanel.setColWeight(1, 1);
    sixColPanel.setColWeight(3, 1);
    sixColPanel.setColWeight(5, 1);
    sixColPanel.setRowWeight(0, 1);
    sixColPanel.setRowWeight(1, 1);

    JLabel yearLabel = new JLabel();
    T.t(this, yearLabel, false, "year");
    yearField.setGuessPatterns("(?i)^year$");

    JLabel monthLabel = new JLabel();
    T.t(this, monthLabel, false, "month");

    JLabel dayLabel = new JLabel();
    T.t(this, dayLabel, false, "day");

    JLabel hourLabel = new JLabel();
    T.t(this, hourLabel, false, "hour");

    JLabel minuteLabel = new JLabel();
    T.t(this, minuteLabel, false, "minute");

    JLabel secondLabel = new JLabel();
    T.t(this, secondLabel, false, "second");

    sixColPanel.insertChild(yearLabel, 0, 0);
    sixColPanel.insertChild(yearField, 1, 0);
    sixColPanel.insertChild(monthLabel, 2, 0);
    sixColPanel.insertChild(monthField, 3, 0);
    sixColPanel.insertChild(dayLabel, 4, 0);
    sixColPanel.insertChild(dayField, 5, 0);

    sixColPanel.insertChild(hourLabel, 0, 1);
    sixColPanel.insertChild(hourField, 1, 1);
    sixColPanel.insertChild(minuteLabel, 2, 1);
    sixColPanel.insertChild(minuteField, 3, 1);
    sixColPanel.insertChild(secondLabel, 4, 1);
    sixColPanel.insertChild(secondField, 5, 1);

    // *******************************************
    // final layout
    SimpleFlowPanel headingPanel = new SimpleFlowPanel();
    headingPanel.setGap(5);
    JLabel dateTimeLabel = new JLabel();
    T.t(this, dateTimeLabel, false, "date_time");
    headingPanel.addChild(dateTimeLabel, false);
    headingPanel.addChild(modeCombobox, true);

    modeCombobox.setEmptyItem(null);
    T.t(this, () -> {
      modeCombobox.resetItems(new String[] {
          T.text("date_time_one_col"),
          T.text("date_time_two_col"),
          T.text("date_time_six_col")
      });
    });
    modeCombobox.addChangeListener((e) -> {
      removeAll();
      addChild(headingPanel, false);
      int index = modeCombobox.getSelectedIndex();
      if (index == 0) {
        addChild(oneColPanel, false);
      } else if (index == 1) {
        addChild(twoColPanel, false);
      } else if (index == 2) {
        addChild(sixColPanel, false);
      }
    });

    modeCombobox.setSelectedItem(0);
  }

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private List<String[]> data;
  private String mv;

  public void setData(List<String[]> data, String[] headers, String mv) {
    this.data = data;
    this.mv = mv;

    dateTimeField.setData(data, headers, mv);
    dateField.setData(data, headers, mv);
    timeField.setData(data, headers, mv);
    yearField.setData(data, headers, mv);
    monthField.setData(data, headers, mv);
    dayField.setData(data, headers, mv);
    hourField.setData(data, headers, mv);
    minuteField.setData(data, headers, mv);
    secondField.setData(data, headers, mv);
  }

  private final HashSet<Integer> mvIndices = new HashSet<>();
  private final HashSet<Integer> ivIndices = new HashSet<>();
  private LocalDateTime[] parsed;
  private String[] unparsed;

  private void updateData() {

    mvIndices.clear();
    ivIndices.clear();

    // dateTimeField.

    if (modeCombobox.getSelectedIndex() == 0) {
      String format = dateTimeFormat.getCurrentText();
      Function<String, LocalDateTime> converter = buildDateTimeConverter(format);
      unparsed = dateTimeField.getUnparsedColumn();
      if (unparsed == null) {
        parsed = null;
        fireChangeListeners();
        return;
      }
      parsed = new LocalDateTime[unparsed.length];
      for (int k = 0; k < unparsed.length; k++) {
        if (unparsed[k].equals(mv)) {
          mvIndices.add(k);
          parsed[k] = null;
        } else {
          parsed[k] = converter.apply(unparsed[k]);
          if (parsed[k] == null) {
            ivIndices.add(k);
          }
        }
      }

    } else if (modeCombobox.getSelectedIndex() == 1) {
      String dateFormatStr = dateFormat.getCurrentText();
      String timeFormatStr = timeFormat.getCurrentText();

      String[] dateVectorStr = dateField.getUnparsedColumn();
      String[] timeVectorStr = timeField.getUnparsedColumn();

      if (dateVectorStr == null) {
        unparsed = null;
        parsed = null;
        fireChangeListeners();
        return;
      }

      String format = String.format("%s %s", dateFormatStr, timeVectorStr == null ? "HH:mm:ss" : timeFormatStr);

      Function<String, LocalDateTime> converter = buildDateTimeConverter(format);

      int n = dateVectorStr.length;

      unparsed = new String[n];
      if (timeVectorStr == null) {
        for (int k = 0; k < n; k++) {
          unparsed[k] = dateVectorStr[k] + " 00:00:00";
        }
      } else {
        for (int k = 0; k < n; k++) {
          unparsed[k] = dateVectorStr[k] + " " + timeVectorStr[k];
        }
      }

      parsed = new LocalDateTime[n];
      for (int k = 0; k < n; k++) {
        if (unparsed[k].equals(mv)) {
          mvIndices.add(k);
          parsed[k] = null;
        } else {
          parsed[k] = converter.apply(unparsed[k]);
          if (parsed[k] == null) {
            ivIndices.add(k);
          }
        }
      }

    } else {
      if (data == null || data.size() == 0) {
        unparsed = null;
        parsed = null;
        fireChangeListeners();
        return;
      }
      int n = data.get(0).length;

      String[] y = yearField.getUnparsedColumn();
      String[] M = monthField.getUnparsedColumn();
      String[] d = dayField.getUnparsedColumn();
      String[] H = hourField.getUnparsedColumn();
      String[] m = minuteField.getUnparsedColumn();
      String[] s = secondField.getUnparsedColumn();

      if (y == null || M == null || d == null) {
        unparsed = null;
        parsed = null;
        fireChangeListeners();
        return;
      }

      unparsed = new String[n];
      if (H == null) {
        for (int k = 0; k < n; k++) {
          unparsed[k] = y[k] + "-" + M[k] + "-" + d[k] + " 00:00:00";

        }
      } else if (m == null) {
        for (int k = 0; k < n; k++) {
          unparsed[k] = y[k] + "-" + M[k] + "-" + d[k] + " " + H[k] + ":00:00";
        }
      } else if (s == null) {
        for (int k = 0; k < n; k++) {
          unparsed[k] = y[k] + "-" + M[k] + "-" + d[k] + " " + H[k] + ":" + m[k] + ":00";
        }
      } else {
        for (int k = 0; k < n; k++) {
          unparsed[k] = y[k] + "-" + M[k] + "-" + d[k] + " " + H[k] + ":" + m[k] + ":" + s[k];
        }
      }

      Function<String, LocalDateTime> converter = buildDateTimeConverter("y-M-d H:m:s");

      parsed = new LocalDateTime[n];
      for (int k = 0; k < n; k++) {
        if (unparsed[k].equals(mv)) {
          mvIndices.add(k);
          parsed[k] = null;
        } else {
          parsed[k] = converter.apply(unparsed[k]);
          if (parsed[k] == null) {
            ivIndices.add(k);
          }
        }
      }
    }

    fireChangeListeners();
  }

  public LocalDateTime[] getParsedColumn() {
    return parsed;
  }

  public void setValidityView(boolean isValid) {
    dateTimeField.combobox.setValidityView(isValid);
    dateField.combobox.setValidityView(isValid);
    timeField.combobox.setValidityView(isValid);
    yearField.combobox.setValidityView(isValid);
    monthField.combobox.setValidityView(isValid);
    dayField.combobox.setValidityView(isValid);
    hourField.combobox.setValidityView(isValid);
    minuteField.combobox.setValidityView(isValid);
    secondField.combobox.setValidityView(isValid);
  }

  public Set<Integer> getInvalidIndices() {
    return ivIndices;
  }

  public boolean hasValidSelection() {
    if (modeCombobox.getSelectedIndex() == 0) {
      return dateTimeField.getIndex() >= 0;
    } else if (modeCombobox.getSelectedIndex() == 1) {
      return dateField.getIndex() >= 0;
    } else {
      boolean y = yearField.getIndex() >= 0;
      boolean M = monthField.getIndex() >= 0;
      boolean d = dayField.getIndex() >= 0;
      boolean H = hourField.getIndex() >= 0;
      boolean m = minuteField.getIndex() >= 0;
      boolean s = secondField.getIndex() >= 0;
      return (y & M & d) |
          (y & M & d & H) |
          (y & M & d & H & m) |
          (y & M & d & H & m & s);
    }
  }

  public int[] getIndices() {
    // List<Integer> indices = new ArrayList<>();
    if (modeCombobox.getSelectedIndex() == 0) {
      int[] indices = new int[1];
      indices[0] = dateTimeField.getIndex();
      return indices;
    } else if (modeCombobox.getSelectedIndex() == 1) {
      int[] indices = new int[2];
      indices[0] = dateField.getIndex();
      indices[1] = timeField.getIndex();
      return indices;
    } else {
      int[] indices = new int[6];
      indices[0] = yearField.getIndex();
      indices[1] = monthField.getIndex();
      indices[2] = dayField.getIndex();
      indices[3] = hourField.getIndex();
      indices[4] = minuteField.getIndex();
      indices[5] = secondField.getIndex();
      return indices;
    }
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

  private static Function<String, LocalDateTime> buildDateTimeConverter(String format) {
    return (String v) -> {
      try {
        LocalDateTime dataTime = LocalDateTime.parse(v, DateTimeFormatter.ofPattern(format));
        return dataTime;
      } catch (Exception e) {
        return null;
      }
    };
  }

  private JButton buildHelpButton(
      boolean date,
      boolean time,
      boolean fractionofsecond,
      String... examples) {
    JButton btn = new JButton();
    btn.setIcon(AppSetup.ICONS.HELP_SMALL);

    for (int k = 0; k < examples.length; k++) {
      examples[k] = T.text("example_num", k + 1) + ": " + examples[k];
    }
    String examplesJoined = String.join("<br>", examples);

    btn.addActionListener(l -> {
      JLabel infoLabel = new JLabel();
      String msg = "";
      if (date) {
        msg += buildDateTimeHelpStringPiece("y", T.text("year"), null, true);
        msg += buildDateTimeHelpStringPiece("M", T.text("month"), null, true);
        msg += buildDateTimeHelpStringPiece("d", T.text("day"), null, time || fractionofsecond);
      }
      if (time) {
        msg += buildDateTimeHelpStringPiece("H", T.text("hour"), "0-23", true);
        msg += buildDateTimeHelpStringPiece("m", T.text("minute"), "0-59", true);
        msg += buildDateTimeHelpStringPiece("s", T.text("second"), "0-59", fractionofsecond);
      }
      if (fractionofsecond) {
        msg += buildDateTimeHelpStringPiece("SSS", T.text("fraction_of_seconds"), "0-999", false);
      }
      infoLabel.setText(String.format("<html><code>%s<br>%s</code></html>", msg, examplesJoined));
      SimplePopup popup = new SimplePopup(btn);
      popup.setPadding(10);
      popup.setContent(infoLabel);
      popup.show();
    });

    return btn;
  }

  private static String buildDateTimeHelpStringPiece(
      String code,
      String meanning,
      String range,
      boolean comma) {
    String piece = String.format("%s = %s", code, meanning);
    if (range != null) {
      piece += String.format(" (%s)", range);
    }
    if (comma) {
      piece += ",<br>";
    }
    return piece;
  }

  @Override
  public boolean isMissing(int colIndex, int rowIndex) {
    return mvIndices.contains(rowIndex);
  }

  @Override
  public boolean isInvalid(int colIndex, int rowIndex) {
    return ivIndices.contains(rowIndex);
  }

  @Override
  public String getLabelText(int colIndex, int rowIndex) {
    if (unparsed == null) {
      if (data == null) {
        return "-";
      } else {
        return data.get(colIndex)[rowIndex];
      }
    }
    if (parsed == null) {
      return unparsed[rowIndex];
    }
    int mode = modeCombobox.getSelectedIndex();
    if (mode == 0) {
      return isInvalid(colIndex, rowIndex) ? "??" : formatter.format(parsed[rowIndex]);
    } else if (mode == 1) {

      if (dateField.getIndex() == colIndex) {
        return isInvalid(colIndex, rowIndex) ? unparsed[rowIndex] : formatter.format(parsed[rowIndex]);
      } else {
        return "";
      }
    } else {
      if (yearField.getIndex() == colIndex) {
        return isInvalid(colIndex, rowIndex) ? unparsed[rowIndex] : formatter.format(parsed[rowIndex]);
      } else {
        return "";
      }
    }
  }

  @Override
  public String getSecondaryLabelText(int colIndex, int rowIndex) {
    if (unparsed == null) {
      return "";
    }
    int mode = modeCombobox.getSelectedIndex();
    if (mode == 0) {
      return isInvalid(colIndex, rowIndex) ? String.format(" (%s) ", unparsed[rowIndex]) : "";
    } else if (mode == 1) {
      if (data == null) {
        return "";
      }
      return String.format(" (%s) ", data.get(colIndex)[rowIndex]);
    } else {
      if (data == null) {
        return "";
      }
      return String.format(" (%s) ", data.get(colIndex)[rowIndex]);

    }
  }

}
