package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.DefaultComboBoxModel;

import org.baratinage.ui.container.RowColPanel;

public class SimpleComboBox extends RowColPanel {

    private DefaultComboBoxModel<String> model;
    private JComboBox<String> comboBox;

    public SimpleComboBox() {
        model = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>();
        comboBox.setModel(model);
        comboBox.addActionListener((e) -> {
            fireChangeListeners();
        });
        appendChild(comboBox);
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
    }

    public void setItems(String[] items) {
        model.removeAllElements();
        model.addElement("-"); // empty element
        for (String item : items) {
            model.addElement(item);
        }
    }

    public void clearItems() {
        model.removeAllElements();
    }

    public void setSelectedItem(int k) {
        if (k < 0) {
            model.setSelectedItem(model.getElementAt(0));
            return;
        }
        k += 1;
        String label = model.getElementAt(k);
        if (label != null) {
            model.setSelectedItem(label);
        }
    }

    public int getSelectedIndex() {
        Object item = model.getSelectedItem();
        if (item == null) {
            return -1;
        }
        int k = model.getIndexOf(item);
        return k - 1;
    }

    public int getItemCount() {
        return model.getSize() - 1;
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
