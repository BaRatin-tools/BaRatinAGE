package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicBorders;

import org.baratinage.App;
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
            color = App.INVALID_COLLOR;
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

    public void setValue(double value, boolean firePropertyChange) {
        setValueValidity(textField.isTextValid());
        double oldValue = this.value;
        this.value = value;

        if (firePropertyChange) {
            firePropertyChange("value", oldValue, value);
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
