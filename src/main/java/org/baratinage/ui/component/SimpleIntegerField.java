package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.baratinage.ui.AppConfig;

public class SimpleIntegerField extends JSpinner {

    private static Color REGULAR_BG = new JTextField().getBackground();
    private static Color INVALID_BG = AppConfig.AC.INVALID_COLOR_BG;

    private SpinnerNumberModel model;

    public SimpleIntegerField() {
        this(0, Integer.MAX_VALUE, 1);
    }

    public SimpleIntegerField(int min, int max, int step) {
        super();

        int H = 32;

        Dimension prefDim = this.getPreferredSize();
        prefDim.width = 100;
        prefDim.height = H;
        Dimension minDim = getMinimumSize();
        minDim.width = 50;
        minDim.height = H;
        Dimension maxDim = getMaximumSize();
        maxDim.height = H;

        setPreferredSize(prefDim);
        setMinimumSize(minDim);
        setMinimumSize(maxDim);

        model = new SpinnerNumberModel(min, min, max, step);
        setModel(model);
    }

    public void configure(int min, int max, int step) {
        model.setMinimum(min);
        model.setMaximum(max);
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
