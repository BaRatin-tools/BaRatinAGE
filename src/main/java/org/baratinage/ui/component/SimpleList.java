package org.baratinage.ui.component;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
// import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
        list = new JList<SimpleListItem<A>>();

        list.addMouseListener(new MouseAdapter() {
            private void handleSelection(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) {
                    list.clearSelection();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleSelection(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleSelection(e);
            }
        });

        model = new DefaultListModel<>();

        list.setDragEnabled(true);
        // list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setDropMode(DropMode.INSERT);

        SimpleListTransferHandler<A> transferHandler = new SimpleListTransferHandler<A>(
                this);
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

    public int getItemCount() {
        return model.getSize();
    }

    public A getObject(int index) {
        return model.getElementAt(index).value;
    }

    public JLabel getLabel(A value) {
        SimpleListItem<A> listItem = getListItem(value);
        if (listItem == null) {
            return null;
        }
        return listItem.label;
    }

    public SimpleListItem<A> getListItem(int index) {
        return model.getElementAt(index);
    }

    public SimpleListItem<A> getListItem(A value) {
        for (int k = 0; k < model.getSize(); k++) {
            if (model.getElementAt(k).value.equals(value)) {
                return model.getElementAt(k);
            }
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

    public void modifyItemLabel(int index, String label) {
        if (index < 0 || index >= model.getSize()) {
            System.err.println("Trying to modify an item at an invalid index '" + index + "'!");
            return;
        }
        SimpleListItem<A> item = model.getElementAt(index);
        if (item == null) {
            System.err.println("No item found at index '" + index + "'!");
            return;
        }
        item.label.setText(label);
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

        repaint();
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

    public void reorderItems(List<A> values) {
        List<SimpleListItem<A>> allItemsInCurrentOrder = new ArrayList<>();
        HashMap<A, SimpleListItem<A>> allItems = new HashMap<>();
        HashMap<A, SimpleListItem<A>> itemsInCorrectOrder = new HashMap<>();
        for (int k = 0; k < model.getSize(); k++) {
            SimpleListItem<A> item = model.getElementAt(k);
            allItemsInCurrentOrder.add(item);
            allItems.put(item.value, item);
            if (!values.contains(item.value)) {
                itemsInCorrectOrder.put(item.value, item);
            }
        }
        List<SimpleListItem<A>> itemsToReorder = new ArrayList<>();
        for (A value : values) {
            if (!itemsInCorrectOrder.containsKey(value) && allItems.containsKey(value)) {
                itemsToReorder.add(allItems.get(value));
            }
        }
        model.clear();
        int counter = 0;
        for (SimpleListItem<A> item : allItemsInCurrentOrder) {
            if (itemsInCorrectOrder.containsKey(item.value)) {
                model.addElement(item);
            } else {
                model.addElement(itemsToReorder.get(counter));
                counter++;
            }
        }
    }

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

        private static record TransferedSimpleListItem<A>(int index, int offset, List<SimpleListItem<A>> items) {
        };

        private TransferedSimpleListItem<A> getItemListInOriginalOrder(
                TransferHandler.TransferSupport support, int index)
                throws UnsupportedFlavorException, IOException {
            List<SimpleListItem<A>> transferedItems = new ArrayList<>();

            Object data = support.getTransferable().getTransferData(SimpleListTransferable.flavor);
            if (!(data instanceof List)) {
                ConsoleLogger.error("Transfered data should be a list!");
                return new TransferedSimpleListItem<>(index, 0, transferedItems);
            }
            List<?> dataList = (List<?>) data;
            if (dataList.size() <= 0) {
                return new TransferedSimpleListItem<>(index, 0, transferedItems);
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
            return new TransferedSimpleListItem<>(index, offset, transferedItems);
        }

        TransferedSimpleListItem<A> lastTransferedData;

        public boolean importData(TransferHandler.TransferSupport support) {
            try {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int index = dl.getIndex();
                TransferedSimpleListItem<A> transferedData = getItemListInOriginalOrder(support, index);
                lastTransferedData = transferedData;
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

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            JList<?> list = (JList<?>) source;
            if (list == null || lastTransferedData == null) {
                return;
            }
            int[] selected = new int[lastTransferedData.items.size()];
            for (int k = 0; k < lastTransferedData.items.size(); k++) {
                selected[k] = lastTransferedData.index - lastTransferedData.offset + k;
            }

            list.setSelectedIndices(selected);
        }
    }

}
