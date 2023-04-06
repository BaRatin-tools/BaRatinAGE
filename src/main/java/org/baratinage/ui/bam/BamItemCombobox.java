package org.baratinage.ui.bam;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.MutableComboBoxModel;

public class BamItemCombobox extends JComboBox<BamItem> {

    private BamItemComboBoxModel model;

    public BamItemCombobox() {
        super();

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                BamItem item = (BamItem) value;

                if (item != null) {
                    return super.getListCellRendererComponent(list, item.getName(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus);
                }

            }
        });

        model = new BamItemComboBoxModel();

        setModel(model);
    }

    public void syncWithBamItemList(BamItemList bamItemList) {
        for (BamItem item : bamItemList) {
            if (model.getIndexOf(item) == -1) {
                model.addElement(item);
            }
        }
        for (int k = 0; k < model.getSize(); k++) {
            BamItem item = model.getElementAt(k);
            if (bamItemList.indexOf(item) == -1) {
                BamItem selectedItem = (BamItem) model.getSelectedItem();
                if (selectedItem.equals(item)) {
                    model.setSelectedItem(null);
                }
                model.removeElement(item);
            }
        }

    }

    private class BamItemComboBoxModel extends AbstractListModel<BamItem>
            implements MutableComboBoxModel<BamItem> {

        private List<BamItem> items = new ArrayList<>();
        private BamItem selectedItem = null;

        @Override
        public void setSelectedItem(Object anItem) {
            if (anItem instanceof BamItem) {
                BamItem candidateItem = (BamItem) anItem;
                int index = items.indexOf(candidateItem);
                if (index != -1) {
                    selectedItem = items.get(index);
                    fireContentsChanged(this, -1, -1);
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public BamItem getElementAt(int index) {
            return items.get(index);
        }

        @Override
        public void addElement(BamItem item) {
            items.add(item);
        }

        @Override
        public void removeElement(Object obj) {
            if (obj instanceof BamItem) {
                BamItem item = (BamItem) obj;
                items.remove(item);
            }
        }

        @Override
        public void insertElementAt(BamItem item, int index) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'insertElementAt'");
        }

        @Override
        public void removeElementAt(int index) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeElementAt'");
        }

        public int getIndexOf(BamItem item) {
            return items.indexOf(item);
        }

    }

}
