package org.baratinage.ui.bam;

import java.awt.Color;
import java.awt.Component;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicBorders;

import org.json.JSONObject;

public class BamItemCombobox extends JComboBox<BamItem> {

    private DefaultComboBoxModel<BamItem> model;

    private final BamItem EMPTY_BAMITEM = new BamItem(BamItemType.EMPTY_ITEM, UUID.randomUUID().toString()) {

        @Override
        public void parentHasChanged(BamItem parent) {
        }

        @Override
        public JSONObject toJSON() {
            return null;
        }

        @Override
        public void fromJSON(JSONObject json) {
        }

        @Override
        public BamItem clone(String uuid) {
            return EMPTY_BAMITEM;
        }

    };

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

                String itemString = "...";
                if (item != null) {
                    itemString = item.getName();
                } else {
                    itemString = String.format("<html><i>%s</i></html>", EMPTY_BAMITEM.getName());
                }

                return super.getListCellRendererComponent(list, itemString, index, isSelected,
                        cellHasFocus);
            }
        });

        model = new DefaultComboBoxModel<>();

        addActionListener((e) -> {
            BamItem selectedItem = getSelectedItem();
            setValidity(selectedItem != null);
        });
        setValidity(false);

        EMPTY_BAMITEM.setName("empty");
        model.addElement(EMPTY_BAMITEM);

        setModel(model);

    }

    public void setEmptyItemText(String text) {
        EMPTY_BAMITEM.setName(text);
    }

    private void setValidity(boolean isValid) {
        Color color = new Color(125, 255, 125, 0);

        if (!isValid) {
            color = new Color(255, 125, 125, 200);
        }

        setBorder(new BasicBorders.FieldBorder(color, color, color, color));

    }

    public void syncWithBamItemList(BamItemList bamItemList) {
        // adding missing item to the list
        for (BamItem item : bamItemList) {
            if (model.getIndexOf(item) == -1) {
                model.addElement(item);
            }
        }
        // removing items that are no longer needed
        for (int k = 0; k < model.getSize(); k++) {
            BamItem item = model.getElementAt(k);
            if (item.equals(EMPTY_BAMITEM)) {
                continue;
            }
            if (bamItemList.indexOf(item) == -1) {
                BamItem selectedItem = getSelectedItem();
                if (selectedItem != null && selectedItem.equals(item)) {
                    model.setSelectedItem(EMPTY_BAMITEM);
                }
                model.removeElement(item);
            }
        }
    }

    @Override
    public BamItem getSelectedItem() {
        BamItem selectedItem = (BamItem) super.getSelectedItem();
        return selectedItem.equals(EMPTY_BAMITEM) ? null : selectedItem;
    }

    public BamItem getBamItemWithId(String id) {
        int n = model.getSize();
        for (int k = 0; k < n; k++) {
            BamItem item = model.getElementAt(k);
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

    // FIXME: should return boolean to check operation success
    public void setSelectedBamItem(String id) {
        BamItem item = getBamItemWithId(id);
        if (item != null) {
            setSelectedItem(item);
        }
    }
}
