package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

import org.json.JSONObject;

public class BamItemParent implements ChangeListener {

    public final BamItemType TYPE;
    public final BamItem CHILD;

    public final RowColPanel comboboxPanel;
    public final JLabel comboboxLabel;

    public final MsgPanel syncIssueMsg = new MsgPanel(MsgPanel.TYPE.ERROR);
    public final JButton revertToSelectCompBtn = new JButton();
    public final JButton createInSyncCompBtn = new JButton();

    private String backupItemString = null;
    private String backupItemId = null;
    private BamItemList allItems = new BamItemList();
    private BamItem currentItem = null;

    private final String[] JSON_KEYS_TO_IGNORE;

    private final SimpleComboBox cb;

    private final ChangeListener onBamItemNameChange;

    public BamItemParent(
            BamItem child,
            BamItemType type,
            String... jsonKeysToIgnore) {

        TYPE = type;
        CHILD = child;
        JSON_KEYS_TO_IGNORE = jsonKeysToIgnore;

        cb = new SimpleComboBox();
        cb.addValidator((k) -> {
            return k >= 0;
        });
        cb.addChangeListener((chEvt) -> {
            int selectedIndex = cb.getSelectedIndex();
            setCurrentBamItem(selectedIndex);
        });

        comboboxLabel = new JLabel();

        comboboxPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        comboboxPanel.setPadding(5);
        comboboxPanel.appendChild(comboboxLabel);
        comboboxPanel.appendChild(cb);

        revertToSelectCompBtn.addActionListener((e) -> {
            if (backupItemId == null) {
                return;
            }
            BamItem item = allItems.getBamItemWithId(backupItemId);
            if (item == null) {
                JOptionPane.showConfirmDialog(
                        AppConfig.AC.APP_MAIN_FRAME,
                        Lg.text("impossible_component_deleted"),
                        Lg.text("error"),
                        JOptionPane.CLOSED_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            setCurrentBamItem(item);
        });
        createInSyncCompBtn.addActionListener((e) -> {
            if (backupItemString == null) {
                return;
            }
            BamItem bamItem = CHILD.PROJECT.addBamItem(TYPE);
            bamItem.fromJSON(new JSONObject(backupItemString));
            CHILD.PROJECT.setCurrentBamItem(CHILD);
            setCurrentBamItem(bamItem);
        });

        Lg.register(this, () -> {
            String typeText = Lg.text(TYPE.id);
            String backupItemName = getBackupItemName();
            String currentItemName = currentItem == null ? Lg.text("msg_empty_selection")
                    : currentItem.bamItemNameField.getText();
            comboboxLabel.setText(typeText);
            syncIssueMsg.message.setText(
                    Lg.html("msg_component_content_out_of_sync", typeText, currentItemName));
            revertToSelectCompBtn.setText(
                    Lg.html("btn_revert_component_selection", backupItemName));
            createInSyncCompBtn.setText(
                    Lg.html("btn_create_component_from_backup", typeText));
        });

        onBamItemNameChange = (chEvt) -> {
            updateCombobox(CHILD.PROJECT.BAM_ITEMS);
            Lg.updateRegisteredObject(this);
        };
    }

    private String getBackupItemName() {
        String name = " - ";
        if (backupItemId != null) {
            BamItem item = allItems.getBamItemWithId(backupItemId);
            if (item != null) {
                name = item.bamItemNameField.getText();
            }
        }
        return name;

    }

    public BamItem getCurrentBamItem() {
        return currentItem;
    }

    public void updateBackup() {
        backupItemId = currentItem.ID;
        backupItemString = currentItem.toJSON().toString();
    }

    public void updateCombobox(BamItemList bamItemList) {
        BamItemList filteredBamItemList = bamItemList.filterByType(TYPE);
        allItems = filteredBamItemList;
        for (BamItem bamItem : allItems) {
            bamItem.bamItemNameField.removeChangeListener(onBamItemNameChange);
            bamItem.bamItemNameField.addChangeListener(onBamItemNameChange);
        }
        syncWithBamItemList();
    }

    private void syncWithBamItemList() {
        String[] itemsName = BamItemList.getBamItemNames(allItems);
        cb.setItems(itemsName, true);
        if (currentItem == null) {
            cb.setSelectedItem(-1, true);
        } else {
            int index = allItems.indexOf(currentItem);
            cb.setSelectedItem(index, true);
        }
    }

    private void setCurrentBamItem(String bamItemId) {
        BamItem selectedItem = allItems.getBamItemWithId(bamItemId);
        setCurrentBamItem(selectedItem);
    }

    private void setCurrentBamItem(int bamItemIndex) {
        BamItem selectedItem = allItems.get(bamItemIndex);
        setCurrentBamItem(selectedItem);
    }

    private void setCurrentBamItem(BamItem bamItem) {
        if (currentItem != null) {
            currentItem.removeChangeListener(this);
        }
        currentItem = bamItem;
        if (currentItem != null) {
            currentItem.addChangeListener(this);
        }
        int index = allItems.indexOf(currentItem);
        cb.setSelectedItem(index, true);
        fireChangeListeners();
    }

    private boolean hasSelectionChanged() {
        if (backupItemString == null) {
            // no backup, irrelevant
            return false;
        }
        if (currentItem == null) {
            // selection must have changed
            return true;
        }
        return !backupItemId.equals(currentItem.ID);
    }

    private boolean isCurrentInSyncWithBackup() {
        if (backupItemString == null) {
            // no backup, irrelevant
            return false;
        }
        if (currentItem == null) {
            // content is different
            return false;
        }
        JSONObject backupItemJson = new JSONObject(backupItemString);
        return currentItem.isMatchingWith(backupItemJson, JSON_KEYS_TO_IGNORE, true);
    }

    public boolean isBamRerunRequired() {
        if (backupItemString == null || backupItemId == null) {
            return false;
        }
        return !isCurrentInSyncWithBackup();
    }

    public List<MsgPanel> getMessages() {
        List<MsgPanel> messages = new ArrayList<>();
        if (backupItemString != null && backupItemId != null) {
            boolean inSync = isCurrentInSyncWithBackup();

            if (!inSync) {
                syncIssueMsg.clearButtons();
                if (hasSelectionChanged() && allItems.getBamItemWithId(backupItemId) != null) {
                    syncIssueMsg.addButton(revertToSelectCompBtn);
                }
                syncIssueMsg.addButton(createInSyncCompBtn);
                messages.add(syncIssueMsg);
            }

        }
        return messages;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (currentItem != null) {
            json.put("bamItemId", currentItem.ID);
        }
        if (backupItemString != null) {
            json.put("backupItemString", backupItemString);
            json.put("backupItemId", backupItemId);
        }
        return json;
    }

    public void fromJSON(JSONObject json) {
        if (json.has("bamItemId")) {
            String bamItemId = json.getString("bamItemId");
            setCurrentBamItem(bamItemId);
        }
        if (json.has("backupItemString")) {
            backupItemString = json.getString("backupItemString");
            backupItemId = json.getString("backupItemId");
        }
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

    @Override
    public void stateChanged(ChangeEvent e) {
        System.out.println("Current item has changed!");
        fireChangeListeners();
    }

}
