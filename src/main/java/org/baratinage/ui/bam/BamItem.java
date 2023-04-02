package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.container.ChangingRowColPanel;
import org.baratinage.ui.container.RowColPanel;

abstract public class BamItem extends ChangingRowColPanel {

    private String uuid;

    protected BamItemList children;
    protected BamItemList siblings;
    protected String name;
    protected String description;

    private JLabel titleLabel;
    private JButton deleteButton;
    private RowColPanel headerPanel;
    private RowColPanel contentPanel;

    public final int type;

    public BamItem(int type) {
        super(AXIS.COL);
        this.type = type;

        headerPanel = new RowColPanel(AXIS.ROW);
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        contentPanel = new RowColPanel();

        super.appendChild(headerPanel, 0, 0, 0, 0, 0);
        super.appendChild(new JSeparator(), 0, 0, 0, 0, 0);
        super.appendChild(contentPanel, 1, 0, 0, 0, 0);

        titleLabel = new JLabel(getName());
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD));
        deleteButton = new JButton("Supprimer");

        headerPanel.appendChild(titleLabel, 1);
        headerPanel.appendChild(deleteButton, 0);

        this.uuid = UUID.randomUUID().toString();

        this.siblings = new BamItemList();
        this.children = new BamItemList();
    }

    public void setSiblings(BamItemList siblings) {
        this.siblings = siblings;
    }

    public void addDeleteAction(ActionListener action) {
        this.deleteButton.addActionListener(action);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
    }

    @Override
    public void appendChild(Component component, double weight,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {
        throw new UnsupportedOperationException("Use setContent method! AppendChild is disabled for BamItem");
    }

    public String getUUID() {
        return this.uuid;
    }

    public void hasChanged() {
        notifyFollowers();
        for (BamItem child : this.children) {
            child.parentHasChanged(this);
        }
    }

    public void addChild(BamItem child) {

        children.add(child);
    }

    public void removeChild(BamItem child) {
        children.remove(child);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BamItem | " + this.type + " : " + this.name + " (" + this.uuid + ")";
    }

    public abstract void parentHasChanged(BamItem parent);

    public abstract String toJsonString();

    public abstract void fromJsonString(String jsonString);
}
