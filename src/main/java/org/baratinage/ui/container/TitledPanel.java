package org.baratinage.ui.container;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.baratinage.ui.component.Title;

public class TitledPanel {

    private JComponent content;
    private SimpleFlowPanel panel;
    private Title title;
    private String titleText;
    private Icon titleIcon;

    public TitledPanel(JComponent content) {
        this.content = content;
        title = new Title();
        panel = new SimpleFlowPanel(true);
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

    public SimpleFlowPanel getTitledPanel() {
        panel.addChild(title, false);
        panel.addChild(content, true);
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
