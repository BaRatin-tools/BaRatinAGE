package org.baratinage.ui.commons;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public class ExplorerItem extends DefaultMutableTreeNode {

    public final String id;
    public String label;
    public Icon icon;
    public final ExplorerItem parentItem;

    public final JPopupMenu contextMenu;

    public ExplorerItem(String id, String label, Icon icon, ExplorerItem parentItem) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.parentItem = parentItem;
        contextMenu = new JPopupMenu();
    }

    public ExplorerItem(String id, String label) {
        this(id, label, null, null);
    }

    public ExplorerItem(String id, String label, Icon icon) {
        this(id, label, icon, null);
    }

    public ExplorerItem[] getChildrenExplorerItems() {
        int n = getChildCount();
        ExplorerItem[] children = new ExplorerItem[n];
        for (int k = 0; k < n; k++) {
            children[k] = (ExplorerItem) getChildAt(k);
        }
        return children;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
