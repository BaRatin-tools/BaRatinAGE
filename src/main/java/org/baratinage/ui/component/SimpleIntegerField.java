package org.baratinage.ui.component;

import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.baratinage.AppSetup;

public class SimpleIntegerField extends JSpinner {

    private SpinnerNumberModel model;

    private static Color REGULAR_BG = new JTextField().getBackground();
    private static Color INVALID_BG = AppSetup.COLORS.INVALID_BG;

    public SimpleIntegerField() {
        this(0, Integer.MAX_VALUE, 1);
    }

    public SimpleIntegerField(int min, int max, int step) {
        super();
        setEditor(new JSpinner.NumberEditor(this, "#"));
        model = new SpinnerNumberModel(min, min, max, step);
        setModel(model);
    }

    public void configure(int min, int max, int step) {
        model.setMinimum(min);
        model.setMaximum(max);
        model.setStepSize(step);
    }

    public void setValue(int value) {
        model.setValue(value);
    }

    public int getIntValue() {
        return (Integer) super.getValue();
    }

    public void setValidityView(boolean valid) {
        setBackground(valid ? REGULAR_BG : INVALID_BG);
    }
}
