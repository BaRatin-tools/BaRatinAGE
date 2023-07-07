package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.App;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;
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
        Lg.register(new LgElement<JButton>(outOfSyncSelectOriginalButton) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText("ui", "oos_revert_select", true);
                String name = getBackupItemName();
                text = Lg.format(text, name);
                object.setText(text);
            }
        });
        outOfSyncSelectOriginalButton.addActionListener((e) -> {
            revertToBackup();
        });

        outOfSyncCreateNewFromOriginalButton = new JButton();
        Lg.register(new LgElement<JButton>(outOfSyncCreateNewFromOriginalButton) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText("ui", "oos_create_from_backup", true);
                text = Lg.format(text, Lg.getText("ui", type.id));
                object.setText(text);
            }
        });
        outOfSyncCreateNewFromOriginalButton.addActionListener((e) -> {
            if (createBackupBamItemAction == null) {
                return;
            }
            String uuid = UUID.randomUUID().toString();
            createBackupBamItemAction.onCreateBackupBamItem(
                    uuid,
                    new JSONObject(backupItemString));
            combobox.setSelectedBamItem(uuid);
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
        public void onCreateBackupBamItem(String uuid, JSONObject json);
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

        Lg.register(new LgElement<JLabel>(outOfSyncWarning.message) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText("ui", "oos_select_and_content", true);
                text = Lg.format(text, Lg.getText("ui", type.id));
                object.setText(text);
            }
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

        // JButton createBamItemFromBackupButton =

        if (!selectionIsOutOfSync) {
            return null;
        }
        System.out.println("> Current item is out of sync with backup");

        if (selectionHasChanged) {
            System.out.println("> Item selection has changed");
            outOfSyncWarning.addButton(outOfSyncSelectOriginalButton);
        } else {
            System.out.println("> Item selection has not changed");
            Lg.register(new LgElement<JLabel>(outOfSyncWarning.message) {
                @Override
                public void setTranslatedText() {
                    String text = Lg.getText("ui", "oos_content", true);
                    String name = getBackupItemName();
                    text = Lg.format(text, Lg.getText("ui", type.id), name);
                    object.setText(text);
                }
            });
        }
        outOfSyncWarning.addButton(outOfSyncCreateNewFromOriginalButton);

        return outOfSyncWarning;

    }

    private void revertToBackup() {
        BamItem item = combobox.getBamItemWithId(backupItemId);
        if (item == null) {
            JOptionPane.showConfirmDialog(
                    App.MAIN_FRAME,
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
