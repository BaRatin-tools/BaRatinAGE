package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicBorders;

import org.baratinage.ui.MainFrame;
import org.baratinage.ui.container.RowColPanel;

public class NumberField extends RowColPanel {

    public static final double NaN = -9999.9999;
    private TextField textField;
    private boolean integerOnly;
    private boolean isValueValid;
    private double value = NaN;

    public NumberField(boolean integerOnly) {

        textField = new TextField();
        textField.addChangeListener(txt -> {
            setValue(txt);
        });

        this.integerOnly = integerOnly;

        addValidator((nbr) -> true);
        if (integerOnly) {
            addValidator((nbr) -> {
                return ((nbr % 1)) == 0;
            });
        }

        textField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (isValueValid()) {
                    updateTextField();
                }
            }

        });

        appendChild(textField, 1);

        setValueValidity(false);
    }

    public NumberField() {
        this(false);
    }

    private void setValueValidity(boolean isValid) {
        Color color = new Color(125, 255, 125, 0);

        if (!isValid) {
            color = MainFrame.APP_CONFIG.INVALID_COLOR;
        }

        setBorder(new BasicBorders.FieldBorder(color, color, color, color));

        this.isValueValid = isValid;
    }

    public void setPlaceholder(String placeholderText) {
        textField.setPlaceholder(placeholderText);
    }

    public boolean isValueValid() {
        return this.isValueValid;
    }

    public void setValue(String value) {
        double doubleValue = NaN;
        try {
            doubleValue = Double.parseDouble(value);

        } catch (NumberFormatException e) {
            setValueValidity(false);
        }

        setValue(doubleValue);

    }

    public void setValue(double value) {
        setValue(value, true);
    }

    public void setValue(double value, boolean fireChangeListeners) {
        setValueValidity(textField.isTextValid());
        this.value = value;

        if (fireChangeListeners) {
            fireChangeListeners();
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

    public double getValue() {
        return value;
    }

    public void updateTextField() {
        if (value == NaN) {
            textField.setTextWithoutFiringChangeListeners("");
            return;
        }
        if (integerOnly) {
            textField.setTextWithoutFiringChangeListeners(Integer.toString((int) value));
        } else {

            textField.setTextWithoutFiringChangeListeners(Double.toString(value));
        }
        setValueValidity(textField.isTextValid());
    }

    @FunctionalInterface
    public interface NumberValidator {
        boolean isNumberValid(double nbr);
    }

    public void addValidator(NumberValidator validator) {
        textField.addTextValidator((txt) -> {
            try {
                double nbr = Double.parseDouble(txt);
                return validator.isNumberValid(nbr);
            } catch (Exception e) {
                return false;
            }

        });
    }

}
