package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

abstract public class BamItem extends RowColPanel {

    private String uuid;

    private BamItemList children;
    private String name;
    private String description;

    private JLabel titleLabel;
    private JButton deleteButton;
    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    // private boolean isFrozen = false;
    // private String jsonFrozenState;

    // private RowColPanel backupPanel;
    // private JLabel backupInfoLabel;
    // private JButton useBackupButton;
    // private JButton propagateChangeButton;
    // public boolean locked;

    public final int type;

    public BamItem(int type) {
        super(AXIS.COL);
        this.type = type;

        headerPanel = new GridPanel();
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        headerPanel.setColWeight(0, 1);

        contentPanel = new RowColPanel();

        super.appendChild(headerPanel, 0, 0, 0, 0, 0);
        super.appendChild(new JSeparator(), 0, 0, 0, 0, 0);
        super.appendChild(contentPanel, 1, 0, 0, 0, 0);

        titleLabel = new JLabel(getName());
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD));
        deleteButton = new JButton("Supprimer");

        headerPanel.insertChild(titleLabel, 0, 0);
        headerPanel.insertChild(deleteButton, 1, 0);

        this.uuid = UUID.randomUUID().toString();

        this.children = new BamItemList();
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
        for (BamItem child : this.children) {
            child.parentHasChanged(this);
        }
    }

    public void addBamItemChild(BamItem childBamItem) {
        children.add(childBamItem);
    }

    public void removeBamItemChild(BamItem childBamItem) {
        children.remove(childBamItem);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        String oldName = getName();
        firePropertyChange("bamItemName", oldName, name);
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BamItem | " + this.type + " : " + this.name + " (" + this.uuid + ")";
    }

    public JSONObject toFullJSON() {
        JSONObject json = new JSONObject();
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("description", description);
        json.put("content", toJSON());
        return json;
    }

    public void fromFullJSON(JSONObject json) {
        uuid = (String) json.get("uuid");
        name = (String) json.get("name");
        description = (String) json.get("description");
        fromJSON((JSONObject) json.get("content"));
    }

    public abstract void parentHasChanged(BamItem parent);

    public abstract JSONObject toJSON();

    public abstract void fromJSON(JSONObject json);

}
