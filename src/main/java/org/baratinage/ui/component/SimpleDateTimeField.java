package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Font;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;

import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class SimpleDateTimeField extends GridPanel {

    private final SimpleIntegerField yearField;
    private final SimpleIntegerField monthField;
    private final SimpleIntegerField dayField;
    private final SimpleIntegerField hourField;
    private final SimpleIntegerField minutField;
    private final SimpleIntegerField secondField;

    // private final LabeledField yearLabField;
    // private final LabeledField monthLabField;
    // private final LabeledField dayLabField;
    // private final LabeledField hourLabField;
    // private final LabeledField minutLabField;
    // private final LabeledField secondLabField;

    private final JLabel yearLabField;
    private final JLabel monthLabField;
    private final JLabel dayLabField;
    private final JLabel hourLabField;
    private final JLabel minutLabField;
    private final JLabel secondLabField;

    private final ChangeListener changeListener;

    private final String id;

    private boolean showSeconds;

    public SimpleDateTimeField(Component owner) {
        this(owner, true);
    }

    public SimpleDateTimeField(Component owner, boolean showSeconds) {
        setGap(5);
        for (int k = 0; k < 6; k++) {
            setColWeight(k, 1);
        }

        this.showSeconds = showSeconds;

        id = Misc.getTimeStampedId();

        yearField = new SimpleIntegerField(1000, 2100, 1);

        monthField = new SimpleIntegerField(1, 12, 1);

        dayField = new SimpleIntegerField(1, 31, 1);

        hourField = new SimpleIntegerField(0, 23, 1);

        minutField = new SimpleIntegerField(0, 59, 1);

        secondField = new SimpleIntegerField(0, 59, 1);

        changeListener = (e) -> {
            TimedActions.throttle(id, 500, () -> {
                System.out.println("HAS CHANGED");
                int nDays = getNumberOfDaysInMonth();
                dayField.configure(1, nDays, 1);
                if (dayField.getIntValue() > nDays) {
                    dayField.setValue(nDays);
                }
            });
        };

        yearField.addChangeListener(changeListener);
        monthField.addChangeListener(changeListener);
        dayField.addChangeListener(changeListener);
        hourField.addChangeListener(changeListener);
        minutField.addChangeListener(changeListener);
        secondField.addChangeListener(changeListener);

        // yearLabField = new LabeledField(yearField);
        // monthLabField = new LabeledField(monthField);
        // dayLabField = new LabeledField(dayField);
        // hourLabField = new LabeledField(hourField);
        // minutLabField = new LabeledField(minutField);
        // secondLabField = new LabeledField(secondField);

        yearLabField = buildLabel();
        monthLabField = buildLabel();
        dayLabField = buildLabel();
        hourLabField = buildLabel();
        minutLabField = buildLabel();
        secondLabField = buildLabel();

        setRowLayout();

        T.updateHierarchy(owner, this);
        T.t(this, () -> {
            // yearLabField.setLabel(T.text("year"));
            // monthLabField.setLabel(T.text("month"));
            // dayLabField.setLabel(T.text("day"));
            // hourLabField.setLabel(T.text("hour"));
            // minutLabField.setLabel(T.text("minute"));
            // secondLabField.setLabel(T.text("second"));
            yearLabField.setText(T.text("year"));
            monthLabField.setText(T.text("month"));
            dayLabField.setText(T.text("day"));
            hourLabField.setText(T.text("hour"));
            minutLabField.setText(T.text("minute"));
            secondLabField.setText(T.text("second"));
        });
    }

    private static JLabel buildLabel() {
        JLabel label = new JLabel();
        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getSize() * 0.9f));
        return label;
    }

    private void setRowLayout() {
        clear();

        insertChild(yearLabField, 0, 0);
        insertChild(monthLabField, 1, 0);
        insertChild(dayLabField, 2, 0);
        insertChild(hourLabField, 3, 0);
        insertChild(minutLabField, 4, 0);
        if (showSeconds) {
            insertChild(secondLabField, 5, 0);
        }

        insertChild(yearField, 0, 1);
        insertChild(monthField, 1, 1);
        insertChild(dayField, 2, 1);
        insertChild(hourField, 3, 1);
        insertChild(minutField, 4, 1);
        if (showSeconds) {
            insertChild(secondField, 5, 1);
        }

    }

    public void setDateTime(LocalDateTime dateTime) {
        yearField.setValue(dateTime.getYear());
        monthField.setValue(dateTime.getMonthValue());
        dayField.setValue(dateTime.getDayOfMonth());
        hourField.setValue(dateTime.getHour());
        minutField.setValue(dateTime.getMinute());
        secondField.setValue(dateTime.getSecond());
    }

    private int getNumberOfDaysInMonth() {
        int y = yearField.getIntValue();
        int M = monthField.getIntValue();
        int h = hourField.getIntValue();
        int m = minutField.getIntValue();
        int s = secondField.getIntValue();
        LocalDateTime ldt = null;
        try {
            ldt = LocalDateTime.of(y, M, 1, h, m, s);

        } catch (DateTimeException e) {
            ConsoleLogger.error(e);
        }
        if (ldt == null) {
            return 31;
        }
        YearMonth yearMonth = YearMonth.from(ldt);
        int nDays = yearMonth.lengthOfMonth();
        return nDays;
    }

    public LocalDateTime getDateTime() {
        int y = yearField.getIntValue();
        int M = monthField.getIntValue();
        int d = dayField.getIntValue();
        int h = hourField.getIntValue();
        int m = minutField.getIntValue();
        int s = secondField.getIntValue();
        try {
            return LocalDateTime.of(y, M, d, h, m, s);
        } catch (DateTimeException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }
}
