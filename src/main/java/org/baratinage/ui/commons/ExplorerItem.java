package org.baratinage.ui.commons;

import javax.swing.tree.DefaultMutableTreeNode;

import org.baratinage.ui.component.NoScalingIcon;

public class ExplorerItem extends DefaultMutableTreeNode {

    public final String id;
    public String label;
    public NoScalingIcon icon;
    public final ExplorerItem parentItem;

    public ExplorerItem(String id, String label, String iconPath, ExplorerItem parentItem) {

        this.id = id;
        this.label = label;
        if (iconPath == "" | iconPath == null) {
            this.icon = null;
        } else {
            this.icon = new NoScalingIcon(iconPath);
            // this.icon.setPref
        }
        this.parentItem = parentItem;
    }

    // public ExplorerItem(String id, String label) {
    // this(id, label, null, null);
    // }

    // public ExplorerItem(String id, String label, ExplorerItem parentItem) {
    // this(id, label, null, parentItem);
    // }

    public ExplorerItem(String id, String label, String iconPath) {
        this(id, label, iconPath, null);
    }

    // public ExplorerItem(String id, String label, ExplorerItem pare) {
    // this(id, label, null, null);
    // }

    @Override
    public String toString() {
        // return ">>>" + this.label + "<<<";
        return this.label;
    }
}
