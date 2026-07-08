package org.baratinage.ui.commons;

import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.baratinage.AppSetup;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;

public class Explorer extends SimpleFlowPanel {

    public final JPopupMenu contextMenu;
    private final JTree explorerTree;
    public final ExplorerItem rootNode;
    private final DefaultTreeModel explorerTreeModel;

    public Explorer() {

        super(true);

        setGap(5);

        Misc.setMinimumSize(this, 200, null);

        contextMenu = new JPopupMenu();

        explorerTree = new JTree();
        explorerTree.setBorder(BorderFactory.createEmptyBorder());

        explorerTree.setRootVisible(false);
        explorerTree.setShowsRootHandles(true);
        explorerTree.setDragEnabled(true);
        explorerTree.setDropMode(DropMode.INSERT);
        explorerTree.setTransferHandler(new ExplorerItemTransferHandler(this));

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
                List<ExplorerItem> selectedItems = getSelectedExplorerItems();
                if (selectedItems.size() == 0 || selectedItems.size() == 1) {
                    selectItem(clickedItem);
                } else {
                    boolean clickedSelected = false;
                    for (ExplorerItem item : selectedItems) {
                        if (item.equals(clickedItem)) {
                            clickedSelected = true;
                            break;
                        }
                    }
                    if (!clickedSelected) {
                        selectItem(clickedItem);
                    }
                }
                JPopupMenu menu = AppSetup.MAIN_FRAME.currentProject.createExplorerContextMenu();
                menu.show(explorerTree, x, y);
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
        insertItem(item, root.getChildCount());
    }

    public void insertItem(ExplorerItem item, int index) {
        ExplorerItem root = this.rootNode;
        if (item.parentItem != null) {
            root = item.parentItem;
        }
        explorerTreeModel.insertNodeInto(item, root, index);
        explorerTreeModel.nodeStructureChanged(root);
    }

    private int getExplorerItemIndex(ExplorerItem item) {
        ExplorerItem parent = rootNode;
        if (item.parentItem != null) {
            parent = item.parentItem;
        }
        return parent.getIndex(item);
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

    public List<ExplorerItem> getSelectedExplorerItems() {
        TreePath[] selectedPaths = explorerTree.getSelectionPaths();
        if (selectedPaths == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(selectedPaths)
                .map(TreePath::getLastPathComponent)
                .filter(node -> node instanceof ExplorerItem)
                .map(node -> (ExplorerItem) node)
                .collect(Collectors.toList());
    }

    public ExplorerItem getSelectedExplorerItem() {
        TreePath selectedPath = explorerTree.getSelectionPath();
        if (selectedPath == null) {
            return null;
        }
        Object obj = selectedPath.getLastPathComponent();
        if (obj instanceof ExplorerItem) {
            return (ExplorerItem) obj;
        }
        return null;
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

    public void addFocusListener(FocusListener l) {
        explorerTree.addFocusListener(l);
    }

    private class ExplorerItemTransferHandler extends TransferHandler {

        private record DropLocationInfo(ExplorerItem parentItem, int index) {
        };

        // safe approach making sure only local objects are handle which
        // means no serialization need to be implemented
        private static final DataFlavor NODE_FLAVOR;
        static {
            DataFlavor f;
            try {
                f = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=" + ExplorerItem.class.getName());
            } catch (ClassNotFoundException e) {
                f = null; // should never happen
            }
            NODE_FLAVOR = f;
        }

        private final Explorer explorer;
        private ExplorerItem draggedExplorerItem;

        public ExplorerItemTransferHandler(Explorer explorer) {
            this.explorer = explorer;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {

            draggedExplorerItem = explorer.getSelectedExplorerItem();
            if (draggedExplorerItem == null) {
                return null;
            }

            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { NODE_FLAVOR };
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(NODE_FLAVOR);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) {
                    return draggedExplorerItem;
                }
            };
        }

        private DropLocationInfo getDropLocation(TransferSupport support) {
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath parentPath = dl.getPath();
            ExplorerItem parentItem = null;
            if (parentPath != null) {
                parentItem = (ExplorerItem) parentPath.getLastPathComponent();
            }
            return new DropLocationInfo(parentItem, dl.getChildIndex());
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()
                    || draggedExplorerItem == null
                    || !support.isDataFlavorSupported(NODE_FLAVOR)) {
                return false;
            }
            DropLocationInfo dl = getDropLocation(support);
            return (draggedExplorerItem.parentItem.equals(dl.parentItem));
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support))
                return false;

            DropLocationInfo dl = getDropLocation(support);
            int oldIndex = explorer.getExplorerItemIndex(draggedExplorerItem);
            explorer.insertItem(draggedExplorerItem, oldIndex < dl.index ? dl.index - 1 : dl.index);
            explorer.selectItem(draggedExplorerItem);
            draggedExplorerItem = null;

            return true;
        }

    }

}
