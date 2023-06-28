package org.baratinage.ui.bam;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.App;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

public class BamItemParent implements ChangeListener {

    public final BamItem.ITEM_TYPE type;

    public final RowColPanel comboboxPanel;
    public final BamItemCombobox combobox;
    public final JLabel comboboxLabel;

    public final BamItem child;

    private String backupItemString = null;
    private String backupItemId = null;
    private BamItem currentItem = null;

    private String[] jsonKeys = new String[] {};
    private boolean excludeJsonKeys = true;

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
                currentItem.removeChangeListener(this);
            }

            if (selected == null) {
                currentItem = null;
                fireChangeListeners();
                return;
            }

            selected.addBamItemChild(child);
            currentItem = selected;
            currentItem.addChangeListener(this);
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

    public List<WarningAndActions> checkSync() {
        System.out.println("================================================================================");
        System.out.println("Checking sync for parent item of type " + type);

        List<WarningAndActions> warnings = new ArrayList<>();

        if (backupItemString == null) {
            if (currentItem == null) {
                System.out.println("> Invalid configuration");
            }
            System.out.println("> No backup");
            return warnings;
        }

        if (currentItem == null) {
            System.out.println("> Invalid configuration");
            System.out.println("> Item selection has changed");

            WarningAndActions warning = new WarningAndActions();
            warning.setWarningMessage("La sélection de '" + type + "' a changé et n'est plus valide.");
            warning.addActionButton(
                    "revert",
                    "Revenir à la sélection précedente",
                    (e) -> {
                        revertToBackup();
                    });

            warnings.add(warning);

            return warnings;
        }

        JSONObject backupItemJson = new JSONObject(backupItemString);

        boolean selectionHasChanged = !backupItemId.equals(currentItem.ID);
        boolean selectionIsOutOfSync = !currentItem.isMatchingWith(backupItemJson, jsonKeys, excludeJsonKeys);

        if (!selectionIsOutOfSync) {
            return warnings;
        }

        WarningAndActions warning = new WarningAndActions();
        warnings.add(warning);
        String warningMessage = "";
        if (selectionHasChanged && selectionIsOutOfSync) {
            System.out.println("> Item selection has changed");
            warningMessage = "La sélection de '" + type
                    + "' a changé et le composant sélectionné n'est pas à jour avec les résultats. ";
            warning.addActionButton(
                    "revert",
                    "Revenir à la sélection précedente",
                    (e) -> {
                        revertToBackup();
                    });
        } else {
            warningMessage = "Le composant '" + type + "' sélectionné n'est pas à jour avec les résultats. ";
        }

        System.out.println("> Current item is out of sync with backup");

        warning.addActionButton(
                "duplicate",
                "Créer un nouveau composant '" + type + "' à jour avec les résultats.",
                (e) -> {
                    if (createBackupBamItemAction == null) {
                        return;
                    }
                    String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
                    String name = backupItemJson.has("name") ? backupItemJson.getString("name") : "...";
                    name += " (copie " + timeStamp + ")";
                    backupItemJson.put("name", name);
                    String uuid = UUID.randomUUID().toString();
                    createBackupBamItemAction.onCreateBackupBamItem(
                            uuid,
                            backupItemJson);
                    combobox.setSelectedBamItem(uuid);
                });

        warning.setWarningMessage(warningMessage);

        return warnings;

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
            // currentItem = combobox.getSelectedItem();
        }
        if (json.has("backupItemString")) {
            backupItemString = json.getString("backupItemString");
            backupItemId = json.getString("backupItemId");
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

    @Override
    public void stateChanged(ChangeEvent e) {
        System.out.println("Current item has changed!");
        fireChangeListeners();
    }

}
