package org.baratinage.ui.bam;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

public class BamItemCombobox extends JComboBox<BamItem> {

    private DefaultComboBoxModel<BamItem> model;

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

        model = new DefaultComboBoxModel<>();
        setModel(model);
    }

    @Override
    public void addItem(BamItem item) {
        if (model.getIndexOf(item) == -1) {
            super.addItem(item);
        }
        // else {
        // System.out.println("Item " + item + " ignored");
        // }

    }

    public void syncWithBamItemList(BamItemList bamItemList) {
        for (BamItem item : bamItemList) {
            if (model.getIndexOf(item) == -1) {
                super.addItem(item);
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
}
