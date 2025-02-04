package org.baratinage.ui.component;

import java.awt.Component;
import java.util.UUID;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

public class SimpleListItem<A> {

    public final String uuid;
    public final JLabel label;
    public final A value;

    public SimpleListItem(String label) {
        this(label, null, null);
    }

    public SimpleListItem(String label, Icon icon, A value) {
        uuid = UUID.randomUUID().toString();
        this.label = new JLabel(label);
        this.label.setIcon(icon);
        this.value = value;
    }

    public static class SimpleListItemRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SimpleListItem) {
                SimpleListItem<?> item = (SimpleListItem<?>) value;
                item.label.setOpaque(true);
                if (isSelected) {
                    item.label.setBackground(list.getSelectionBackground());
                    item.label.setForeground(list.getSelectionForeground());
                } else {
                    item.label.setBackground(list.getBackground());
                    item.label.setForeground(list.getForeground());
                }
                return item.label;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
