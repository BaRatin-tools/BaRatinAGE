package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Component;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.container.ChangingRowColPanel;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

abstract public class BamItem extends ChangingRowColPanel {

    private String uuid;

    private BamItemList children;
    private BamItemList siblings;
    private String name;
    private String description;

    private JLabel titleLabel;
    private JButton deleteButton;
    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    private boolean isFrozen = false;
    private String jsonFrozenState;

    private RowColPanel backupPanel;
    private JLabel backupInfoLabel;
    private JButton useBackupButton;
    private JButton propagateChangeButton;

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

        backupPanel = new RowColPanel();
        backupPanel.setGap(5);
        backupInfoLabel = new JLabel();
        backupInfoLabel.setForeground(Color.RED);
        useBackupButton = new JButton("Annuler les modifications");
        useBackupButton.addActionListener((e) -> {
            JSONObject json = new JSONObject(jsonFrozenState);
            fromJSON(json);
            backupPanel.clear();
            backupInfoLabel.setText("Cet objet est utilisé par d'autres objets");
            backupPanel.appendChild(backupInfoLabel);
        });
        propagateChangeButton = new JButton("Propager les modifications");
        propagateChangeButton.addActionListener((e) -> {
        });
        backupPanel.appendChild(backupInfoLabel);

        headerPanel.insertChild(titleLabel, 0, 0);
        headerPanel.insertChild(deleteButton, 1, 0);
        headerPanel.insertChild(backupPanel, 0, 1, 2, 1);

        this.uuid = UUID.randomUUID().toString();

        this.siblings = new BamItemList();
        this.children = new BamItemList();
    }

    public void updateSiblings(BamItemList bamItems) {
        // add missing siblings
        for (BamItem item : bamItems) {
            if (!siblings.contains(item)) {
                siblings.add(item);
            }
        }
        // remove no longer existing siblings
        for (BamItem item : siblings) {
            if (!bamItems.contains(item)) {
                siblings.remove(item);
            }
        }
    }

    public BamItemList getSiblings() {
        return this.siblings;
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
        if (isFrozen) {
            backupInfoLabel.setText(
                    "Attention, des objets existants dépendent de l'objet que vous venez de modifier!");
            if (backupPanel.getComponentCount() == 1) {
                backupPanel.appendChild(useBackupButton);
                backupPanel.appendChild(propagateChangeButton);
            }
        }
    }

    private void freeze() {
        isFrozen = true;
        backupInfoLabel.setText("Cet objet est utilisé par d'autres objets");
        jsonFrozenState = toJSON().toString();
    }

    private void unfreeze() {
        isFrozen = false;
        backupInfoLabel.setText("");
        backupPanel.clear();
        backupPanel.appendChild(backupInfoLabel);
    }

    public void addChild(BamItem child) {
        children.add(child);
        if (!isFrozen) {
            freeze();
        }
    }

    public void removeChild(BamItem child) {
        children.remove(child);
        if (children.size() == 0) {
            unfreeze();
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
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

    public abstract void parentHasChanged(BamItem parent);

    public abstract JSONObject toJSON();

    public abstract void fromJSON(JSONObject json);

}
