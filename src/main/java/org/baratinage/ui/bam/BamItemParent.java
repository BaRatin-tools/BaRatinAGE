package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.MainFrame;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.json.JSONObject;

public class BamItemParent implements ChangeListener {

    public final BamItemType type;

    public final RowColPanel comboboxPanel;
    public final BamItemCombobox combobox;
    public final JLabel comboboxLabel;

    public final BamItem child;

    public final WarningAndActions outOfSyncWarning;
    public final JButton outOfSyncSelectOriginalButton;
    public final JButton outOfSyncCreateNewFromOriginalButton;

    private String backupItemString = null;
    private String backupItemId = null;
    private BamItem currentItem = null;

    private String[] jsonKeys = new String[] {};
    private boolean excludeJsonKeys = true;

    public BamItemParent(
            BamItem child,
            BamItemType type) {

        this.type = type;
        this.child = child;
        this.combobox = new BamItemCombobox();

        combobox.addActionListener(e -> {
            BamItem selected = combobox.getSelectedItem();
            if (currentItem != null) {
                currentItem.removeChangeListener(this);
            }

            if (selected == null) {
                currentItem = null;
                fireChangeListeners();
                return;
            }

            currentItem = selected;
            currentItem.addChangeListener(this);
            fireChangeListeners();
        });

        comboboxLabel = new JLabel();

        comboboxPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        comboboxPanel.setPadding(5);
        comboboxPanel.appendChild(comboboxLabel);
        comboboxPanel.appendChild(combobox);

        outOfSyncWarning = new WarningAndActions();

        outOfSyncSelectOriginalButton = new JButton();
        Lg.register(outOfSyncSelectOriginalButton, () -> {
            String text = Lg.html("oos_revert_select", getBackupItemName());
            outOfSyncSelectOriginalButton.setText(text);
        });
        outOfSyncSelectOriginalButton.addActionListener((e) -> {
            revertToBackup();
        });

        outOfSyncCreateNewFromOriginalButton = new JButton();
        Lg.register(outOfSyncCreateNewFromOriginalButton, () -> {
            String typeText = Lg.text(type.id);
            String text = Lg.html("oos_create_from_backup", typeText);
            outOfSyncCreateNewFromOriginalButton.setText(text);
        });

        outOfSyncCreateNewFromOriginalButton.addActionListener((e) -> {
            if (createBackupBamItemAction == null) {
                return;
            }
            BamItem item = createBackupBamItemAction.createBackupBamItem(new JSONObject(backupItemString));
            combobox.setSelectedBamItem(item.ID);
        });
    }

    private String getBackupItemName() {
        String name = "?";
        if (backupItemId != null) {
            BamItem item = combobox.getBamItemWithId(backupItemId);
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
        BamItemList filteredBamItemList = bamItemList.filterByType(type);
        combobox.syncWithBamItemList(filteredBamItemList);
    }

    public void setSyncJsonKeys(String[] keys, boolean exclude) {
        jsonKeys = keys;
        excludeJsonKeys = exclude;
    }

    @FunctionalInterface
    public interface ICreateBackupBamItem {
        public BamItem createBackupBamItem(JSONObject json);
    }

    private ICreateBackupBamItem createBackupBamItemAction = null;

    public void setCreateBackupBamItemAction(ICreateBackupBamItem l) {
        createBackupBamItemAction = l;
    }

    public WarningAndActions getOutOfSyncWarning() {

        if (backupItemString == null) {
            if (currentItem == null) {
                System.out.println("> Invalid configuration");
            }
            System.out.println("> No backup");
            return null;
        }

        boolean backupItemStillExists = combobox.getBamItemWithId(backupItemId) != null;

        outOfSyncSelectOriginalButton.setEnabled(backupItemStillExists);

        Lg.register(outOfSyncWarning.message, () -> {
            String typeText = Lg.text(type.id);
            String text = Lg.html("oos_select_and_content", typeText);
            outOfSyncWarning.message.setText(text);
        });

        outOfSyncWarning.clearButtons();

        if (currentItem == null) {
            System.out.println("> Invalid configuration");
            System.out.println("> Item selection has changed");
            outOfSyncWarning.addButton(outOfSyncSelectOriginalButton);
            return outOfSyncWarning;
        }

        JSONObject backupItemJson = new JSONObject(backupItemString);

        boolean selectionHasChanged = !backupItemId.equals(currentItem.ID);
        boolean selectionIsOutOfSync = !currentItem.isMatchingWith(backupItemJson, jsonKeys, excludeJsonKeys);

        if (!selectionIsOutOfSync) {
            return null;
        }
        System.out.println("> Current item is out of sync with backup");

        if (selectionHasChanged) {
            System.out.println("> Item selection has changed");
            outOfSyncWarning.addButton(outOfSyncSelectOriginalButton);
        } else {
            System.out.println("> Item selection has not changed");
            Lg.register(outOfSyncWarning.message, () -> {
                String typeText = Lg.text(type.id);
                String text = Lg.html("oos_content", typeText, getBackupItemName());
                outOfSyncWarning.message.setText(text);
            });
        }
        outOfSyncWarning.addButton(outOfSyncCreateNewFromOriginalButton);

        return outOfSyncWarning;

    }

    private void revertToBackup() {
        BamItem item = combobox.getBamItemWithId(backupItemId);
        if (item == null) {
            JOptionPane.showConfirmDialog(
                    MainFrame.APP_CONFIG.APP_MAIN_FRAME,
                    "Opération impossible, le composant a sans doute été supprimé.",
                    "Erreur!",
                    JOptionPane.CLOSED_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        combobox.setSelectedBamItem(backupItemId);
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
            combobox.setSelectedBamItem(bamItemId);
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
