package org.baratinage.ui.commons;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.baratinage.ui.container.RowColPanel;

public class Explorer extends RowColPanel {

    public final JLabel headerLabel = new JLabel();

    private final JTree explorerTree;
    private final ExplorerItem rootNode;
    private final DefaultTreeModel explorerTreeModel;

    public Explorer() {

        super(AXIS.COL, ALIGN.STRETCH, ALIGN.STRETCH);

        setGap(5);

        setMinimumSize(new Dimension(200, 100));

        appendChild(headerLabel, 0);

        headerLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        explorerTree = new JTree();
        explorerTree.setBorder(BorderFactory.createEmptyBorder());

        explorerTree.setRootVisible(false);
        explorerTree.setShowsRootHandles(true);

        JScrollPane treeViewScrollableArea = new JScrollPane(explorerTree);
        treeViewScrollableArea.setBorder(BorderFactory.createEmptyBorder());

        appendChild(treeViewScrollableArea);

        rootNode = new ExplorerItem("root", "Root");
        explorerTreeModel = new DefaultTreeModel(rootNode);
        explorerTree.setModel(explorerTreeModel);

        explorerTree.setCellRenderer(new CustomRenderer());
        explorerTree.setRowHeight(35);

        explorerTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int selRow = explorerTree.getRowForLocation(x, y);
                if (selRow != -1) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        // enable right click
                        explorerTree.setSelectionRow(selRow);
                        TreePath selPath = explorerTree.getPathForLocation(x, y);
                        Object o = selPath.getLastPathComponent();
                        if (o instanceof ExplorerItem) {
                            ExplorerItem item = (ExplorerItem) o;
                            item.contextMenu.show(explorerTree, x, y);
                        }
                    }
                }
            }
        });
    }

    public void appendItem(ExplorerItem item) {
        ExplorerItem root = this.rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        explorerTreeModel.insertNodeInto(item, root, root.getChildCount());
        explorerTreeModel.nodeStructureChanged(root);
    }

    public void updateItemView(ExplorerItem item) {
        explorerTreeModel.nodeChanged(item);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        explorerTree.addTreeSelectionListener(listener);
    }

    public ExplorerItem getLastSelectedPathComponent() {
        return (ExplorerItem) explorerTree.getLastSelectedPathComponent();
    }

    public void expandItem(ExplorerItem item) {
        this.explorerTree.expandPath(new TreePath(item.getPath()));
    }

    public void selectItem(ExplorerItem item) {
        // FIXME: needed?
        if (item == null) {
            explorerTree.setSelectionPath(null);
            return;
        }
        explorerTree.setSelectionPath(new TreePath(item.getPath()));
    }

    public void removeItem(ExplorerItem item) {
        ExplorerItem root = rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        explorerTreeModel.removeNodeFromParent(item);
        explorerTreeModel.nodeStructureChanged(root);
    }

    private ExplorerItem searchItem(String id, ExplorerItem parentItem) {
        int n = parentItem.getChildCount();
        for (int k = 0; k < n; k++) {
            ExplorerItem child = (ExplorerItem) parentItem.getChildAt(k);
            if (child.id.equals(id)) {
                return child;
            } else {
                ExplorerItem grandChild = searchItem(id, child);
                if (grandChild != null) {
                    return grandChild;
                }

            }
        }
        return null;
    }

    public ExplorerItem getItem(String id) {
        return searchItem(id, rootNode);
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
