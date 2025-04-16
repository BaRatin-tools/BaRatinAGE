package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Misc;

public class SimpleSlider extends RowColPanel {

    private double min;
    private double step;
    private int n;

    private final JSlider slider;
    private final JLabel valueLabel;

    public SimpleSlider(double min, double max, double step) {
        this.min = min;
        this.step = step;
        n = (int) Math.floor((max - min) / step) + 1;

        slider = new JSlider(1, n);
        valueLabel = new JLabel();

        appendChild(slider, 1);
        appendChild(valueLabel, 0);

        slider.addChangeListener(l -> {
            if (!slider.getValueIsAdjusting()) {
                updateLabel();
                fireChangeListeners();
            }
        });
    }

    private int toSliderUnit(double value) {
        return (int) Math.floor((value - min) / step) + 1;
    }

    private double toUserUnit(int value) {
        return (double) ((value - 1) * step) + min;
    }

    private void updateLabel() {
        valueLabel.setText(Misc.formatNumber(getValue()));
    }

    public void setValue(double value) {
        slider.setValue(toSliderUnit(value));
        updateLabel();
    }

    public double getValue() {
        return toUserUnit(slider.getValue());
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void fireChangeListeners() {
        for (ChangeListener cl : changeListeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

}
