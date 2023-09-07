package org.baratinage.ui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.RowColPanel;

public class RadioButtons extends RowColPanel implements ActionListener {

    private Map<String, JRadioButton> options = new HashMap<>();
    private ButtonGroup buttonGroup = new ButtonGroup();

    public void addOption(String value, JRadioButton button) {
        buttonGroup.add(button);
        options.put(value, button);
        button.addActionListener(this);
        appendChild(button);
    }

    public void removeOption(String value) {
        JRadioButton btn = options.get(value);
        buttonGroup.remove(btn);
        options.remove(value);
        btn.removeActionListener(this);
        remove(btn);
    }

    public void setSelectedValue(String value) {
        JRadioButton btn = options.get(value);
        if (btn != null) {
            btn.setSelected(true);
        }

    }

    public String getSelectedValue() {
        for (String key : options.keySet()) {
            JRadioButton btn = options.get(key);
            if (btn.isSelected()) {
                return key;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        fireChangeListeners();
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

}
