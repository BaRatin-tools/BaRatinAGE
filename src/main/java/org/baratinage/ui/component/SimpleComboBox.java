package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;

import org.baratinage.AppSetup;
import org.baratinage.ui.container.RowColPanel;

public class SimpleComboBox extends RowColPanel {

    private static JLabel buildLabel(String text, Icon icon) {
        JLabel label = new JLabel(text);
        label.setBorder(new EmptyBorder(2, 5, 2, 5));
        if (icon != null) {
            label.setIcon(icon);
        }
        return label;
    }

    private final Color INVALID_BG_COLOR;
    private final Color REGULAR_BG_COLOR;

    private final CustomListCellRenderer renderer;
    private final DefaultComboBoxModel<JLabel> model;
    private final JComboBox<JLabel> comboBox;
    private boolean changeListenersDisabled = false;

    private final JLabel defaultEmptyLabel = buildLabel("<html>&mdash;</html>", null);
    private JLabel emptyLabel = defaultEmptyLabel;

    public SimpleComboBox() {

        model = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>();
        comboBox.setModel(model);

        renderer = new CustomListCellRenderer();
        comboBox.setRenderer(renderer);
        comboBox.addActionListener((e) -> {
            if (!changeListenersDisabled) {
                fireChangeListeners();
            }
            // fireValidators();
        });

        INVALID_BG_COLOR = AppSetup.COLORS.INVALID_BG;
        REGULAR_BG_COLOR = comboBox.getBackground();
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
        int n = items.length;
        JLabel[] labels = new JLabel[n];
        for (int k = 0; k < n; k++) {
            labels[k] = buildLabel(items[k], null);
        }
        setItems(labels, silent);
    }

    public void setItems(String[] items, Icon icon) {
        int n = items.length;
        Icon[] icons = new Icon[n];
        for (int k = 0; k < n; k++) {
            icons[k] = icon;
        }
        setItems(items, icons);
    }

    public void setItems(String[] items, Icon[] icons) {
        int n = items.length;
        int m = icons.length;
        if (m != n) {
            throw new IllegalArgumentException("items and icons must have the same length!");
        }
        JLabel[] labels = new JLabel[n];
        for (int k = 0; k < n; k++) {
            labels[k] = buildLabel(items[k], icons[k]);
        }
        setItems(labels, false);
    }

    public void setItems(JLabel[] labels, boolean silent) {
        if (silent) {
            changeListenersDisabled = true;
        }
        model.removeAllElements();
        if (emptyLabel != null) {
            model.addElement(emptyLabel); // empty element
        }
        for (JLabel label : labels) {
            model.addElement(label);
        }
        changeListenersDisabled = false;
    }

    public void setEmptyItem(JLabel label) {
        emptyLabel = label;
        if (model.getSize() > 0) {
            changeListenersDisabled = true;
            model.removeElementAt(0);
            model.insertElementAt(emptyLabel, 0);
            changeListenersDisabled = false;
        }
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
        if (emptyLabel != null) {
            k += 1;
        }
        JLabel label = model.getElementAt(k);
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
        return emptyLabel != null ? k - 1 : k;
    }

    public int getItemCount() {
        int size = model.getSize();
        return emptyLabel != null ? size - 1 : size;
    }

    public void setValidityView(boolean isValid) {
        // renderer.isValid = isValid;
        comboBox.setBackground(isValid ? REGULAR_BG_COLOR : INVALID_BG_COLOR);
        repaint();
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

    private static class CustomListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);
            JLabel originalLabel = (JLabel) value;
            if (originalLabel != null) {
                label.setText(originalLabel.getText());
                label.setIcon(originalLabel.getIcon());
                label.setBorder(originalLabel.getBorder());
            }
            return label;
        }

    }
}
