package org.baratinage.ui.container;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.baratinage.ui.component.Title;

public class TitledPanel {

    private JComponent content;
    private RowColPanel panel;
    private Title title;
    private String titleText;
    private Icon titleIcon;

    public TitledPanel(JComponent content) {
        this.content = content;
        title = new Title();
        panel = new RowColPanel(RowColPanel.AXIS.COL);
    }

    public void setText(String text) {
        titleText = text;
        title.setText(text);
    }

    public void setIcon(Icon icon) {
        titleIcon = icon;
        title.setIcon(icon);
    }

    public Title getTitle() {
        return title;
    }

    public RowColPanel getTitledPanel() {
        panel.appendChild(title, 0);
        panel.appendChild(content, 1);
        return panel;
    }

    public JComponent getContent() {
        return content;
    }

    public String getText() {
        return titleText;
    }

    public Icon getIcon() {
        return titleIcon;
    }
}
