package org.baratinage.ui.commons;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.baratinage.ui.container.RowColPanel;

public class Explorer extends RowColPanel {

    public final JLabel headerLabel = new JLabel();

    private JTree explorerTree;
    private ExplorerItem rootNode;
    private DefaultTreeModel explorerTreeModel;

    public Explorer() {

        super(AXIS.COL, ALIGN.STRETCH, ALIGN.STRETCH);

        this.setGap(5);

        setMinimumSize(new Dimension(200, 100));

        this.appendChild(headerLabel, 0);

        this.explorerTree = new JTree();
        this.explorerTree.setBorder(BorderFactory.createEmptyBorder());

        this.explorerTree.setRootVisible(false);
        this.explorerTree.setShowsRootHandles(true);

        JScrollPane treeViewScrollableArea = new JScrollPane(explorerTree);
        treeViewScrollableArea.setBorder(BorderFactory.createEmptyBorder());

        this.appendChild(treeViewScrollableArea);

        this.rootNode = new ExplorerItem("root", "Root");
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
        this.explorerTreeModel.nodeChanged(item);
    }

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

            if (item.parentItem == null) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            return this;
        }
    }

}
