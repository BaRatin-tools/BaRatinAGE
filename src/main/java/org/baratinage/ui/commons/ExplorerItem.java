package org.baratinage.ui.commons;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.baratinage.ui.component.NoScalingIcon;

public class ExplorerItem extends DefaultMutableTreeNode {

    public final String id;
    public String label;
    public ImageIcon icon;
    public final ExplorerItem parentItem;

    public ExplorerItem(String id, String label, ImageIcon icon, ExplorerItem parentItem) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.parentItem = parentItem;
    }

    public ExplorerItem(String id, String label) {
        this.id = id;
        this.label = label;
        this.icon = null;
        this.parentItem = null;
    }

    public ExplorerItem(String id, String label, ImageIcon icon) {
        this(id, label, icon, null);
    }

    public ExplorerItem(String id, String label, String iconPath, ExplorerItem parentItem) {
        this(id, label, new NoScalingIcon(iconPath), parentItem);
    }

    public ExplorerItem(String id, String label, String iconPath) {
        this(id, label, iconPath, null);
    }

    @Override
    public String toString() {
        return this.label;
    }
}
