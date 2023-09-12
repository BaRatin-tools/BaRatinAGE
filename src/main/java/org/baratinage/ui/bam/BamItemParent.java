package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

import org.json.JSONObject;

public class BamItemParent implements ChangeListener {

    public final BamItemType TYPE;
    public final BamItem CHILD;

    public final RowColPanel comboboxPanel;
    public final JLabel comboboxLabel;

    public final WarningAndActions outOfSyncWarningContentOnly;
    public final WarningAndActions outOfSyncWarningSelectionAndContent;
    public final JButton outOfSyncSelectOriginalButton;
    public final JButton outOfSyncCreateNewFromOriginalButton;

    private String backupItemString = null;
    private String backupItemId = null;
    private BamItemList allItems = new BamItemList();
    private BamItem currentItem = null;

    private final String[] JSON_KEYS_TO_IGNORE;

    private final SimpleComboBox cb;

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

        outOfSyncWarningContentOnly = new WarningAndActions();
        outOfSyncWarningSelectionAndContent = new WarningAndActions();
        outOfSyncSelectOriginalButton = new JButton();
        outOfSyncSelectOriginalButton.addActionListener((e) -> {
            revertToBackup();
        });
        outOfSyncCreateNewFromOriginalButton = new JButton();
        outOfSyncCreateNewFromOriginalButton.addActionListener((e) -> {

            BamItem bamItem = CHILD.PROJECT.addBamItem(TYPE);
            bamItem.fromJSON(new JSONObject(backupItemString));
            CHILD.PROJECT.setCurrentBamItem(CHILD);

            setCurrentBamItem(bamItem.ID);
        });

        Lg.register(this, () -> {
            String typeText = Lg.text(TYPE.id);
            comboboxLabel.setText(typeText);
            outOfSyncWarningSelectionAndContent.message.setText(
                    Lg.html("oos_select_and_content", typeText));
            outOfSyncWarningContentOnly.message.setText(
                    Lg.html("oos_content", typeText, getBackupItemName()));
            outOfSyncSelectOriginalButton.setText(
                    Lg.html("oos_revert_select", getBackupItemName()));
            outOfSyncCreateNewFromOriginalButton.setText(
                    Lg.html("oos_create_from_backup", typeText));
        });
    }

    private String getBackupItemName() {
        String name = "?";
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

    public WarningAndActions getOutOfSyncWarning() {

        if (backupItemString == null) {
            if (currentItem == null) {
                System.out.println("> Invalid configuration");
            }
            System.out.println("> No backup");
            return null;
        }

        boolean backupItemStillExists = allItems.getBamItemWithId(backupItemId) != null;

        outOfSyncSelectOriginalButton.setEnabled(backupItemStillExists);

        outOfSyncWarningContentOnly.clearButtons();
        outOfSyncWarningSelectionAndContent.clearButtons();

        if (currentItem == null) {
            outOfSyncWarningSelectionAndContent.addButton(outOfSyncSelectOriginalButton);
            return outOfSyncWarningSelectionAndContent;
        }

        JSONObject backupItemJson = new JSONObject(backupItemString);

        boolean selectionHasChanged = !backupItemId.equals(currentItem.ID);
        boolean selectionIsOutOfSync = !currentItem.isMatchingWith(backupItemJson, JSON_KEYS_TO_IGNORE, true);

        if (!selectionIsOutOfSync) {
            return null;
        }

        if (selectionHasChanged) {
            outOfSyncWarningSelectionAndContent.addButton(outOfSyncSelectOriginalButton);
            outOfSyncWarningSelectionAndContent.addButton(outOfSyncCreateNewFromOriginalButton);
            return outOfSyncWarningSelectionAndContent;
        } else {
            outOfSyncWarningContentOnly.addButton(outOfSyncCreateNewFromOriginalButton);
            return outOfSyncWarningContentOnly;

        }
    }

    private void revertToBackup() {
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
