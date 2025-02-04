package org.baratinage.ui.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.baratinage.ui.component.SimpleListItem.SimpleListItemRenderer;

public class SimpleList<A> extends JScrollPane {

    private final JList<SimpleListItem<A>> list;
    private final DefaultListModel<SimpleListItem<A>> model;

    private boolean hasJustReorderedElements = false;

    public SimpleList() {
        list = new JList<SimpleListItem<A>>();
        model = new DefaultListModel<>();

        list.setDragEnabled(true);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new SimpleListTransferHandler(this));

        list.setCellRenderer(new SimpleListItemRenderer());

        list.setModel(model);

        setOpaque(true);

        model.addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                System.out.println("'intervalAdded'");

                if (hasJustReorderedElements) {
                    fireOrderChangeListener();
                    hasJustReorderedElements = false;
                }
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                System.out.println("'intervalRemoved'");
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                System.out.println("'contentsChanged'");
            }

        });

        list.addListSelectionListener(l -> {
            if (!l.getValueIsAdjusting()) {
                fireSelectionChangeListener();
            }
        });

        setViewportView(list);

    }

    // public void add

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

    private static class SimpleListTransferable implements Transferable {

        public static DataFlavor flavor = new DataFlavor(
                SimpleListItem.class,
                "SimpleListItem");
        private static final DataFlavor[] flavors = {
                flavor
        };

        private SimpleListItem<?> item;

        public SimpleListTransferable(SimpleListItem<?> item) {
            this.item = item;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors.clone();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavor.equals(flavors[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(flavors[0])) {
                return item;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

    }

    private class SimpleListTransferHandler extends TransferHandler {

        private final SimpleList<A> simpleList;

        public SimpleListTransferHandler(SimpleList<A> list) {
            this.simpleList = list;
        }

        protected Transferable createTransferable(JComponent c) {
            return new SimpleListTransferable(simpleList.list.getSelectedValue());
        }

        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(SimpleListTransferable.flavor);
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            try {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                DefaultListModel<SimpleListItem<A>> model = simpleList.model;
                int index = dl.getIndex();
                Object data = support.getTransferable()
                        .getTransferData(SimpleListTransferable.flavor);

                if (data instanceof SimpleListItem<?>) {
                    @SuppressWarnings("unchecked")
                    SimpleListItem<A> castedData = (SimpleListItem<A>) data;
                    int indexOfElementToRemove = -1;
                    for (int k = 0; k < model.getSize(); k++) {
                        if (model.getElementAt(k).uuid.equals(castedData.uuid)) {
                            indexOfElementToRemove = k;
                            break;
                        }
                    }
                    if (indexOfElementToRemove >= 0) {
                        simpleList.hasJustReorderedElements = true; // to fire appropriate listeners
                        model.removeElementAt(indexOfElementToRemove);
                    }
                    model.add(indexOfElementToRemove < index ? index - 1 : index, castedData);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
