package org.baratinage.ui.component;

import java.awt.Color;

import javax.swing.plaf.basic.BasicBorders;

import org.baratinage.ui.container.RowColPanel;

public class NumberField extends RowColPanel {

    private TextField textField;
    private boolean isValid;
    private double value;

    public NumberField() {

        textField = new TextField();
        textField.addChangeListener(txt -> {
            System.out.println(txt);
            setValue(txt);
        });

        appendChild(textField, 1);

        setValidity(false);

    }

    private void setValidity(boolean isValid) {

        Color color = new Color(125, 255, 125, 0);

        if (!isValid) {
            color = new Color(255, 125, 125, 200);
        }

        setBorder(new BasicBorders.FieldBorder(color, color, color, color));

        this.isValid = isValid;
    }

    public boolean getValidity() {
        return this.isValid;
    }

    public void setValue(String value) {
        double doubleValue = Double.NaN;
        try {
            doubleValue = Double.parseDouble(value);
            setValidity(true);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            setValidity(false);
        }
        this.value = doubleValue;
    }

    public void setValue(double value) {
        this.value = value;
        setValidity(true);
    }

    public double getValue() {
        return this.value;
    }

}
