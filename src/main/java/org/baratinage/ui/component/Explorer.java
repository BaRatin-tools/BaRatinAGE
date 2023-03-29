package org.baratinage.ui.component;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.baratinage.ui.container.RowColPanel;

public class Explorer extends RowColPanel {

    private JTree explorerTree;
    private ExplorerItem rootNode;
    private DefaultTreeModel explorerTreeModel;

    public Explorer(String label) {

        super(AXIS.COL, ALIGN.STRETCH, ALIGN.STRETCH);

        this.setGap(5);
        this.setPadding(5);

        JLabel header = new JLabel(label);
        this.appendChild(header, 0);

        // this.explorerTreeData = new DefaultMutableTreeNode();

        this.explorerTree = new JTree();

        this.explorerTree.setRootVisible(false);
        this.explorerTree.setShowsRootHandles(true);

        JScrollPane treeViewScrollableArea = new JScrollPane(explorerTree);
        // treeViewScrollableArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // this.appendChild(treeViewScrollableArea, 1);
        this.appendChild(treeViewScrollableArea);

        this.rootNode = new ExplorerItem("root", "Root", null);
        this.explorerTreeModel = new DefaultTreeModel(rootNode);
        this.explorerTree.setModel(this.explorerTreeModel);

        this.explorerTree.setCellRenderer(new CustomRenderer());
        this.explorerTree.setRowHeight(35);

    }

    public void appendItem(ExplorerItem item) {
        ExplorerItem root = this.rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        this.explorerTreeModel.insertNodeInto(item, root, root.getChildCount());
        this.explorerTreeModel.nodeStructureChanged(root);
    }

    public void updateItemView(ExplorerItem item) {
        ExplorerItem root = this.rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        this.explorerTreeModel.nodeStructureChanged(root);
        // expandItem(item);
    }

    // public void updateItem(ExplorerItem item) {
    // ExplorerItem root = this.rootNode;
    // if (item.parentItem != null) {
    // root = item.parentItem;
    // }
    // // this.explorerTreeModel.upda
    // this.explorerTreeModel.nodeStructureChanged(root);
    // }

    // FIXME: given the following methods, I think I sould find a way to make
    // this class herit from JTree!
    public void addTreeSelectionListener(TreeSelectionListener listener) {
        this.explorerTree.addTreeSelectionListener(listener);
    }

    public ExplorerItem getLastSelectedPathComponent() {
        return (ExplorerItem) this.explorerTree.getLastSelectedPathComponent();
    }

    public void expandItem(ExplorerItem item) {
        this.explorerTree.expandPath(new TreePath(item.getPath()));
    }

    public void selectItem(ExplorerItem item) {
        // FIXME: needed?
        if (item == null) {
            this.explorerTree.setSelectionPath(null);
            return;
        }
        this.explorerTree.setSelectionPath(new TreePath(item.getPath()));
    }

    public void removeItem(ExplorerItem item) {
        // this.explorerTreeModel.insertNodeInto(item, root, root.getChildCount());
        // this.explorerTreeModel.nodeStructureChanged(root);
        ExplorerItem root = this.rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        this.explorerTreeModel.removeNodeFromParent(item);
        this.explorerTreeModel.nodeStructureChanged(root);
    }

    private class CustomRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            ExplorerItem item = (ExplorerItem) value;

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            setText(item.label);
            setIcon(item.icon);

            // this.setPreferredSize(new Dimension(5000, 40));

            // this.setOpaque(true);
            // Color rC = this.getBackgroundSelectionColor();
            // if (selected && hasFocus) {
            // Color c = new Color(rC.getRed(), rC.getGreen(), rC.getBlue());
            // this.setBackground(c);
            // } else if (selected && !hasFocus) {
            // Color c = new Color(rC.getRed() + 50, rC.getGreen() + 50, 255);
            // this.setBackground(c);
            // } else {
            // this.setOpaque(false);
            // }

            return this;
        }
    }

}
