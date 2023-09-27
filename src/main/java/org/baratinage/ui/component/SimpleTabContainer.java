package org.baratinage.ui.component;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class SimpleTabContainer extends JTabbedPane {

    private record TabItem(JLabel title, Component component) {
    };

    private List<TabItem> tabItems = new ArrayList<>();

    private final EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);

    public static enum LOC {
        LEFT(JTabbedPane.LEFT),
        TOP(JTabbedPane.TOP),
        RIGHT(JTabbedPane.RIGHT),
        BOTTOM(JTabbedPane.BOTTOM);

        private final int placement;

        private LOC(int loc) {
            placement = loc;
        }
    }

    public SimpleTabContainer() {
        super();
    }

    public SimpleTabContainer(LOC placement) {
        super(placement.placement);
    }

    @Override
    public void addTab(String label, Icon icon, Component component) {
        JLabel jlab = new JLabel();
        jlab.setText(label);
        jlab.setIcon(icon);
        jlab.setBorder(emptyBorder);
        int index = tabItems.size();
        tabItems.add(new TabItem(jlab, component));
        super.addTab(label, component);
        super.setTabComponentAt(index, jlab);
    }

    @Override
    public void remove(int index) {
        if (index >= 0 && index < tabItems.size()) {
            tabItems.remove(index);
            super.remove(index);
        }
    }

    public void setTitleTextAt(int index, String text) {
        if (index >= 0 && index < tabItems.size()) {
            TabItem item = tabItems.get(index);
            if (item != null) {
                item.title.setText(text);
            }
        }
    }

}
