package org.baratinage.ui.component;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimpleRadioButtons<T> {

    private final Map<String, T> optVals = new HashMap<>();
    private final Map<String, JRadioButton> optBtns = new HashMap<>();
    private ButtonGroup buttonGroup = new ButtonGroup();

    private ActionListener actionListener = (e) -> {
        fireChangeListeners();
    };

    public JRadioButton addOption(String optionId, String labelText, T value) {
        JRadioButton button = new JRadioButton(labelText);
        buttonGroup.add(button);
        optBtns.put(optionId, button);
        optVals.put(optionId, value);
        button.addActionListener(actionListener);
        return button;
    }

    public void removeOption(String optionId) {
        JRadioButton btn = optBtns.get(optionId);
        if (btn == null) {
            return;
        }
        btn.removeActionListener(actionListener);
        buttonGroup.remove(btn);
        optBtns.remove(optionId);
    }

    public void setSelected(String optionId) {
        JRadioButton btn = optBtns.get(optionId);
        if (btn != null) {
            btn.setSelected(true);
        }
    }

    public T getSelectedValue() {
        for (String key : optBtns.keySet()) {
            JRadioButton btn = optBtns.get(key);
            if (btn.isSelected()) {
                return optVals.get(key);
            }
        }
        return null;
    }

    public String getSelectedId() {
        for (String key : optBtns.keySet()) {
            JRadioButton btn = optBtns.get(key);
            if (btn.isSelected()) {
                return key;
            }
        }
        return null;
    }

    public JRadioButton getSelectedButton() {
        for (String key : optBtns.keySet()) {
            JRadioButton btn = optBtns.get(key);
            if (btn.isSelected()) {
                return btn;
            }
        }
        return null;
    }

    public Map<String, JRadioButton> getButtons() {
        // create shallow copy
        Map<String, JRadioButton> buttons = new HashMap<>();
        for (String key : optBtns.keySet()) {
            buttons.put(key, optBtns.get(key));
        }
        return buttons;
    }

    public Map<String, T> getValues() {
        // create shallow copy
        Map<String, T> values = new HashMap<>();
        for (String key : optVals.keySet()) {
            values.put(key, optVals.get(key));
        }
        return values;
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
