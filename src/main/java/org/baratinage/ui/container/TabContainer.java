package org.baratinage.ui.container;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class TabContainer extends JTabbedPane {

    private final EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);

    public static enum SIDE {
        LEFT(JTabbedPane.LEFT),
        TOP(JTabbedPane.TOP),
        RIGHT(JTabbedPane.RIGHT),
        BOTTOM(JTabbedPane.BOTTOM);

        private final int placement;

        private SIDE(int loc) {
            placement = loc;
        }
    }

    public TabContainer() {
        super();
    }

    public TabContainer(SIDE placement) {
        super(placement.placement);
    }

    public void addTab(TitledPanel titledPanel) {
        int index = getTabCount();
        super.addTab("", titledPanel.getContent());
        super.setTabComponentAt(index, titledPanel.getTitle());
    }

    @Override
    public void addTab(String label, Icon icon, Component component) {
        int index = getTabCount();
        JLabel jlab = new JLabel();
        jlab.setText(label);
        jlab.setIcon(icon);
        jlab.setBorder(emptyBorder);
        super.addTab(label, component);
        super.setTabComponentAt(index, jlab);
    }

    @Override
    public void setTitleAt(int index, String label) {
        Component comp = super.getTabComponentAt(index);
        JLabel jlab = new JLabel();
        if (comp instanceof JLabel) {
            jlab = (JLabel) comp;
        }
        jlab.setText(label);
        super.setTabComponentAt(index, jlab);
    }

}
