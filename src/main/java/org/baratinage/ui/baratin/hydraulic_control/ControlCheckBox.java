package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import org.baratinage.ui.container.RowColPanel;

public class ControlCheckBox
        extends RowColPanel {

    private int grayRGBvalue = 225;
    private Color uncheckedColor = new Color(grayRGBvalue, grayRGBvalue, grayRGBvalue);
    private Color checkedColor = new Color(50, 200, 50);

    private JCheckBox checkBox;

    private ItemListener itemListener;

    public ControlCheckBox(String text, ItemListener itemListener) {
        super(AXIS.ROW, ALIGN.CENTER);
        checkBox = new JCheckBox(text);
        checkBox.setSelected(false);
        checkBox.setOpaque(false);
        checkBox.addItemListener(itemListener);
        this.itemListener = itemListener;

        appendChild(checkBox);
        this.setOpaque(true);
        this.setBackground(uncheckedColor);
        this.setPreferredSize(new Dimension(100, 30));
        checkBox.addItemListener(e -> {
            if (checkBox.isSelected()) {
                setBackground(checkedColor);
            } else {
                setBackground(uncheckedColor);
            }
        });
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void destroy() {
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
        }

        checkBox.removeItemListener(itemListener);

    }

}
