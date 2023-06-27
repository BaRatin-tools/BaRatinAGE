package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

public class BamItemParent {

    public final BamItem.ITEM_TYPE type;

    public final RowColPanel comboboxPanel;
    public final BamItemCombobox combobox;
    public final JLabel comboboxLabel;

    public final BamItem child;

    private String backupString;

    private BamItem currentItem = null;

    public BamItemParent(
            BamItem child,
            BamItem.ITEM_TYPE type) {

        this.type = type;
        this.child = child;
        this.combobox = new BamItemCombobox();

        combobox.addActionListener(e -> {
            BamItem selected = combobox.getSelectedItem();
            if (currentItem != null) {
                currentItem.removeBamItemChild(child);
            }

            if (selected == null) {
                currentItem = null;
                fireChangeListeners();
                return;
            }

            selected.addBamItemChild(child);
            currentItem = selected;
            fireChangeListeners();
        });

        comboboxLabel = new JLabel();

        comboboxPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        comboboxPanel.setPadding(5);
        comboboxPanel.appendChild(comboboxLabel);
        comboboxPanel.appendChild(combobox);

    }

    public BamItem getCurrentBamItem() {
        return currentItem;
    }

    public void updateBackup() {
        backupString = currentItem.toJSON().toString();
    }

    public String getBackupString() {
        return backupString;
    }

    public void updateCombobox(BamItemList bamItemList) {
        BamItemList filteredBamItemList = bamItemList.filterByType(type);
        combobox.syncWithBamItemList(filteredBamItemList);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (currentItem != null) {
            json.put("bamItemId", currentItem.ID);
        }
        if (backupString != null) {
            json.put("backupString", backupString);
        }
        return json;
    }

    public void fromJSON(JSONObject json) {
        if (json.has("bamItemId")) {
            String bamItemId = json.getString("bamItemId");
            combobox.setSelectedBamItem(bamItemId);
            // currentItem = combobox.getSelectedItem();
        }
        if (json.has("backupString")) {
            backupString = json.getString("backupString");
        }
        // fireChangeListeners();
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
