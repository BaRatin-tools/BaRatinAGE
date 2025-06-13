package org.baratinage.ui.commons;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;

public class Explorer extends SimpleFlowPanel {

    private final JTree explorerTree;
    public final ExplorerItem rootNode;
    private final DefaultTreeModel explorerTreeModel;

    public Explorer() {

        super(true);

        setGap(5);

        Misc.setMinimumSize(this, 200, null);

        explorerTree = new JTree();
        explorerTree.setBorder(BorderFactory.createEmptyBorder());

        explorerTree.setRootVisible(false);
        explorerTree.setShowsRootHandles(true);

        JScrollPane treeViewScrollableArea = new JScrollPane(explorerTree);
        treeViewScrollableArea.setBorder(BorderFactory.createEmptyBorder());

        addChild(treeViewScrollableArea, 1);

        rootNode = new ExplorerItem("root", "Root");
        explorerTreeModel = new DefaultTreeModel(rootNode);
        explorerTree.setModel(explorerTreeModel);

        explorerTree.setCellRenderer(new CustomRenderer());
        explorerTree.setRowHeight(35);

        explorerTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON3) {
                    return;
                }
                int x = e.getX();
                int y = e.getY();
                ExplorerItem clickedItem = getClickedExplorerItem(y);
                if (clickedItem != null) {
                    selectItem(clickedItem);
                    clickedItem.contextMenu.show(explorerTree, x, y);
                }
            }
        });
    }

    private ExplorerItem getClickedExplorerItem(int y) {
        int step = 10;
        TreePath clickedTreePath = null;
        for (int k = 0; k < explorerTree.getWidth(); k = k + step) {
            clickedTreePath = explorerTree.getPathForLocation(k, y);
            if (clickedTreePath != null) {
                break;
            }
        }
        if (clickedTreePath != null) {
            Object o = clickedTreePath.getLastPathComponent();
            if (o != null) {
                if (o instanceof ExplorerItem) {
                    ExplorerItem item = (ExplorerItem) o;
                    return item;
                }
            }
        }
        return null;
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

    public void removeAllItems() {
        rootNode.removeAllChildren();
        explorerTreeModel.nodeStructureChanged(rootNode);
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
