package org.baratinage.ui.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.SimpleListItem.SimpleListItemRenderer;
import org.baratinage.utils.ConsoleLogger;

public class SimpleList<A> extends JScrollPane {

    private final JList<SimpleListItem<A>> list;
    private final DefaultListModel<SimpleListItem<A>> model;

    public SimpleList() {
        list = new JList<SimpleListItem<A>>() {
            @Override
            public int locationToIndex(java.awt.Point location) {
                int index = super.locationToIndex(location);
                if (index != -1 && getCellBounds(index, index).contains(location)) {
                    return index;
                }
                clearSelection();
                return -1; // Prevent selecting the last item when clicking on empty space
            }
        };

        model = new DefaultListModel<>();

        list.setDragEnabled(true);
        // list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setDropMode(DropMode.INSERT);
        SimpleListTransferHandler<A> transferHandler = new SimpleListTransferHandler<A>(this);
        transferHandler.addTransferDoneListener(l -> {
            fireOrderChangeListener();
        });
        list.setTransferHandler(transferHandler);

        list.setCellRenderer(new SimpleListItemRenderer());

        list.setModel(model);

        setOpaque(true);

        list.addListSelectionListener(l -> {
            if (!l.getValueIsAdjusting()) {
                fireSelectionChangeListener();
            }
        });

        setViewportView(list);

    }

    public boolean containsObject(A object) {
        for (int k = 0; k < model.getSize(); k++) {
            if (model.getElementAt(k).value.equals(object)) {
                return true;
            }
        }
        return false;
    }

    public List<A> getAllObjects() {
        List<A> values = new ArrayList<A>();
        for (int k = 0; k < model.getSize(); k++) {
            values.add(model.getElementAt(k).value);
        }
        return values;
    }

    public List<A> getSelectedObjects() {
        return list.getSelectedValuesList().stream().map(i -> i.value).toList();
    }

    public A getSelectedObject() {
        SimpleListItem<A> item = list.getSelectedValue();
        if (item != null) {
            return item.value;
        }
        return null;
    }

    public void clearList() {
        model.clear();
    }

    public void addItem(String label, Icon icon, A value) {
        model.addElement(new SimpleListItem<A>(label, icon, value));
    }

    public boolean removeItem(A value) {
        int index = getFirstIndex(value);
        if (index >= 0) {
            model.removeElementAt(index);
            return true;
        }
        return false;
    }

    public int getFirstIndex(A value) {
        for (int k = 0; k < model.getSize(); k++) {
            if (model.getElementAt(k).value.equals(value)) {
                return k;
            }
        }
        return -1;
    }

    public void modifyItemLabel(int index, String label, Icon icon) {

        if (index < 0 || index >= model.getSize()) {
            System.err.println("Trying to modify an item at an invalid index '" + index + "'!");
            return;
        }
        SimpleListItem<A> item = model.getElementAt(index);
        if (item == null) {
            System.err.println("No item found at index '" + index + "'!");
            return;
        }

        if (label != null) {
            item.label.setText(label);
        }

        if (icon != null) {
            item.label.setIcon(icon);
        }
    }

    public void modifyItemValue(int index, A value) {

        if (index < 0 || index >= model.getSize()) {
            System.err.println("Trying to modify an item at an invalid index '" + index + "'!");
            return;
        }
        SimpleListItem<A> item = model.getElementAt(index);
        if (item == null) {
            System.err.println("No item found at index '" + index + "'!");
            return;
        }

        item.value = value;
    }

    public List<A> getValues() {
        List<A> items = new ArrayList<>();
        for (int k = 0; k < model.getSize(); k++) {
            items.add(model.getElementAt(k).value);
        }
        return items;
    }

    // public SimpleListItem getItem(int index) {
    // try {
    // SimpleListItem item = model.elementAt(index);
    // return item;
    // } catch (Exception e) {
    // ConsoleLogger.error(e);
    // }
    // return null;
    // }

    private final List<ChangeListener> orderChangeListeners = new ArrayList<>();

    public void addOrderChangeListeners(ChangeListener l) {
        orderChangeListeners.add(l);
    }

    public void removeOrderChangeListener(ChangeListener l) {
        orderChangeListeners.remove(l);
    }

    private void fireOrderChangeListener() {
        for (ChangeListener l : orderChangeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    private final List<ChangeListener> selectionChangeListeners = new ArrayList<>();

    public void addSelectionChangeListeners(ChangeListener l) {
        selectionChangeListeners.add(l);
    }

    public void removeSelectionChangeListener(ChangeListener l) {
        selectionChangeListeners.remove(l);
    }

    private void fireSelectionChangeListener() {
        for (ChangeListener l : selectionChangeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    private static class SimpleListTransferable<A> implements Transferable {

        public static DataFlavor flavor = new DataFlavor(
                SimpleListItem.class,
                "SimpleListItem");
        private static final DataFlavor[] flavors = {
                flavor
        };

        public List<SimpleListItem<A>> data;

        public SimpleListTransferable(List<SimpleListItem<A>> data) {
            this.data = data;

        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors.clone();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return data;
        }

    }

    private static class SimpleListTransferHandler<A> extends TransferHandler {

        private final SimpleList<A> simpleList;

        public SimpleListTransferHandler(SimpleList<A> list) {
            this.simpleList = list;
        }

        private final List<ChangeListener> transferDoneListener = new ArrayList<>();

        public void addTransferDoneListener(ChangeListener l) {
            transferDoneListener.add(l);
        }

        private void fireTransferDoneListener() {
            for (ChangeListener l : transferDoneListener) {
                l.stateChanged(new ChangeEvent(this));
            }
        }

        protected Transferable createTransferable(JComponent c) {

            return new SimpleListTransferable<A>(simpleList.list.getSelectedValuesList());
        }

        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return true;
        }

        private static record TransferedSimpleListItem<A>(int offset, List<SimpleListItem<A>> items) {
        };

        private TransferedSimpleListItem<A> getItemListInOriginalOrder(
                TransferHandler.TransferSupport support, int index)
                throws UnsupportedFlavorException, IOException {
            List<SimpleListItem<A>> transferedItems = new ArrayList<>();

            Object data = support.getTransferable().getTransferData(SimpleListTransferable.flavor);
            if (!(data instanceof List)) {
                ConsoleLogger.error("Transfered data should be a list!");
                return new TransferedSimpleListItem<>(0, transferedItems);
            }
            List<?> dataList = (List<?>) data;
            if (dataList.size() <= 0) {
                return new TransferedSimpleListItem<>(0, transferedItems);
            }
            for (int k = 0; k < index; k++) {
                SimpleListItem<A> item = simpleList.model.getElementAt(k);
                if (dataList.contains(item)) {
                    transferedItems.add(item);
                }
            }
            int offset = transferedItems.size();
            for (int k = index; k < simpleList.model.getSize(); k++) {
                SimpleListItem<A> item = simpleList.model.getElementAt(k);
                if (dataList.contains(item)) {
                    transferedItems.add(item);
                }
            }
            Collections.reverse(transferedItems);
            return new TransferedSimpleListItem<>(offset, transferedItems);
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            try {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int index = dl.getIndex();
                TransferedSimpleListItem<A> transferedData = getItemListInOriginalOrder(support, index);
                for (SimpleListItem<A> transferedItem : transferedData.items) {
                    simpleList.model.removeElement(transferedItem);
                }
                for (SimpleListItem<A> transferedItem : transferedData.items) {
                    simpleList.model.add(index - transferedData.offset, transferedItem);
                }
                fireTransferDoneListener();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
