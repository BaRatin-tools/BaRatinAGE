package org.baratinage.ui.component;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class SimpleTabContainer extends JTabbedPane {

    private record TabItem(JLabel title, Component component) {
    };

    private List<TabItem> tabItems = new ArrayList<>();

    @Override
    public void addTab(String label, Icon icon, Component component) {
        JLabel jlab = new JLabel();
        jlab.setText(label);
        jlab.setIcon(icon);
        int index = tabItems.size();
        tabItems.add(new TabItem(jlab, component));
        super.addTab(label, component);
        super.setTabComponentAt(index, jlab);
    }

    public void setTitleTextAt(int index, String text) {
        if (index > 0 && index < tabItems.size()) {
            TabItem item = tabItems.get(index);
            if (item != null) {
                item.title.setText(text);
            }
        }
    }

}
