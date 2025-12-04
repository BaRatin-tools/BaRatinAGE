package org.baratinage.ui.component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.SimpleFlowPanel;

import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class SimpleDateTimeField extends SimpleFlowPanel {

  public final DatePicker datePicker;
  public final TimePicker timePicker;

  public SimpleDateTimeField() {
    super();
    setGap(5);

    datePicker = new DatePicker();
    timePicker = new TimePicker();
    timePicker.set24HourView(true);
    datePicker.setEditor(new JFormattedTextField());
    timePicker.setEditor(new JFormattedTextField());

    addChild(datePicker.getEditor());
    addChild(timePicker.getEditor());

    datePicker.addDateSelectionListener(e -> {
      fireChangeListeners();
    });
    timePicker.addTimeSelectionListener(e -> {
      fireChangeListeners();
    });

  }

  public void clearDateTime() {
    datePicker.clearSelectedDate();
    timePicker.clearSelectedTime();
  }

  public void setDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      clearDateTime();
      return;
    }
    datePicker.setSelectedDate(dateTime.toLocalDate());
    timePicker.setSelectedTime(dateTime.toLocalTime());

  }

  public LocalDateTime getDateTime() {
    LocalDate date = datePicker.getSelectedDate();
    LocalTime time = timePicker.getSelectedTime();
    if (date != null && time != null) {
      LocalDateTime dateTime = LocalDateTime.of(date, time);
      return dateTime;
    }
    return null;
  }

  private final List<ChangeListener> changeListeners = new ArrayList<>();

  public void addChangeListener(ChangeListener l) {
    changeListeners.add(l);
  }

  public void removeChangeListener(ChangeListener l) {
    changeListeners.remove(l);
  }

  private void fireChangeListeners() {
    for (ChangeListener l : changeListeners) {
      l.stateChanged(new ChangeEvent(this));
    }
  }
}
