package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicBorders;

import org.baratinage.ui.container.ChangingRowColPanel;

public class NumberField extends ChangingRowColPanel {

    private TextField textField;
    private boolean integerOnly;
    private boolean isValueValid;
    private double value;

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
            color = new Color(255, 125, 125, 200);
        }

        setBorder(new BasicBorders.FieldBorder(color, color, color, color));

        this.isValueValid = isValid;
    }

    public boolean isValueValid() {
        return this.isValueValid;
    }

    public void setValue(String value) {
        double doubleValue = Double.NaN;
        try {
            doubleValue = Double.parseDouble(value);

        } catch (NumberFormatException e) {
            setValueValidity(false);
        }

        setValue(doubleValue);

    }

    public void setValue(double value) {
        setValue(value, false);
    }

    public void setValue(double value, boolean doNotNotifyFollowers) {
        setValueValidity(textField.isTextValid());
        this.value = value;

        if (!doNotNotifyFollowers) {
            notifyFollowers();

        }
    }

    public double getValue() {
        return value;
    }

    public void updateTextField() {
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