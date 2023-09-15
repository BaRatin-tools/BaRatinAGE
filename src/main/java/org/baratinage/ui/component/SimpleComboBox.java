package org.baratinage.ui.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.DefaultComboBoxModel;

import org.baratinage.ui.container.RowColPanel;

public class SimpleComboBox extends RowColPanel {

    private DefaultComboBoxModel<String> model;
    private JComboBox<String> comboBox;
    private boolean changeListenersDisabled = false;

    public SimpleComboBox() {
        model = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>();
        comboBox.setModel(model);
        comboBox.addActionListener((e) -> {
            if (!changeListenersDisabled) {
                fireChangeListeners();
            }
            fireValidators();
        });
        appendChild(comboBox);
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
    }

    public void setItems(String[] items) {
        setItems(items, false);
    }

    public void setItems(String[] items, boolean silent) {
        if (silent) {
            changeListenersDisabled = true;
        }
        model.removeAllElements();
        model.addElement("-"); // empty element
        for (String item : items) {
            model.addElement(item);
        }
        changeListenersDisabled = false;
    }

    public void clearItems() {
        model.removeAllElements();
    }

    public void setSelectedItem(int k) {
        setSelectedItem(k, false);
    }

    public void setSelectedItem(int k, boolean silent) {
        if (silent) {
            changeListenersDisabled = true;
        }
        if (k < 0) {
            model.setSelectedItem(model.getElementAt(0));
            changeListenersDisabled = false;
            return;
        }
        k += 1;
        String label = model.getElementAt(k);
        if (label != null) {
            model.setSelectedItem(label);
        }
        changeListenersDisabled = false;
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

    private final List<Predicate<Integer>> validators = new ArrayList<>();

    public void addValidator(Predicate<Integer> validator) {
        this.validators.add(validator);
    }

    public void removeValidator(Predicate<Integer> validator) {
        this.validators.remove(validator);
    }

    public void fireValidators() {
        setValidity(true);
        for (Predicate<Integer> p : validators) {
            if (!p.test(getSelectedIndex())) {
                setValidity(false);
            }
        }
    }

    private void setValidity(boolean isValid) {
        Color color = new Color(125, 255, 125, 0);
        if (!isValid) {
            color = new Color(255, 125, 125, 200);
        }
        setBorder(new BasicBorders.FieldBorder(color, color, color, color));
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
