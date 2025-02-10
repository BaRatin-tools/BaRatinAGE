package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.RowColPanel;

public class SimpleCheckbox extends RowColPanel {
    private final JCheckBox checkbox;

    public SimpleCheckbox() {
        super();

        checkbox = new JCheckBox();

        checkbox.addItemListener(e -> {
            fireChangeListeners();
        });

        appendChild(checkbox, 1);
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

    public boolean isSelected() {
        return checkbox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkbox.setSelected(selected);
    }

    public void setText(String text) {
        checkbox.setText(text);
    }
}
