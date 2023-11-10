package org.baratinage.ui.commons;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public class ExplorerItem extends DefaultMutableTreeNode {

    public final String id;
    public String label;
    public ImageIcon icon;
    public final ExplorerItem parentItem;

    public final JPopupMenu contextMenu;

    public ExplorerItem(String id, String label, ImageIcon icon, ExplorerItem parentItem) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.parentItem = parentItem;
        contextMenu = new JPopupMenu();
    }

    public ExplorerItem(String id, String label) {
        this(id, label, null, null);
    }

    public ExplorerItem(String id, String label, ImageIcon icon) {
        this(id, label, icon, null);
    }

    @Override
    public String toString() {
        return this.label;
    }
}
