package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.json.JSONObject;

public class BamItemParent extends SimpleFlowPanel {

    private final BamItemType TYPE;
    private final BamItemType[] TYPES;
    private final BamItem CHILD;

    private final JLabel comboboxLabel;

    private String bamItemBackupId = null;
    private BamConfig bamItemBackup = null;

    private BamItemList allItems = new BamItemList();
    private BamItem currentBamItem = null;

    private boolean isCurrentInSyncWithBackup = false;
    private boolean canBeEmpty = false;

    public final SimpleComboBox cb;

    private final ChangeListener onBamItemNameChange;
    private final ChangeListener onBamItemContentChange;

    public BamItemParent(
            BamItem child,
            BamItemType... types) {

        super(true);
        // setGap(5);
        setPadding(5);

        TYPE = types[0];
        TYPES = types;
        CHILD = child;

        CHILD.PROJECT.BAM_ITEMS.addChangeListener((chEvt) -> {
            updateCombobox();
        });

        cb = new SimpleComboBox();
        cb.addChangeListener((chEvt) -> {
            int selectedIndex = cb.getSelectedIndex();
            setCurrentBamItem(selectedIndex);
        });
        cb.setValidityView(false);

        comboboxLabel = new JLabel();
        comboboxLabel.setIcon(TYPE.getIcon());

        // appendChild(comboboxLabel, 0);
        // appendChild(cb, 1);
        addChild(comboboxLabel, false);
        addChild(cb, false);

        onBamItemNameChange = (chEvt) -> {
            syncWithBamItemList();
            T.updateTranslation(this);
        };

        onBamItemContentChange = (chEvt) -> {
            fireChangeListeners();
        };

        T.t(this, comboboxLabel, false, TYPE.id);

        updateCombobox();
    }

    private String getCurrentItemName() {
        return currentBamItem == null ? T.text("msg_empty_selection")
                : currentBamItem.bamItemNameField.getText();
    }

    public void setComparisonJSONfilter(JSONFilter filter) {
        filters.put(TYPE, filter);
    }

    private HashMap<BamItemType, JSONFilter> filters = new HashMap<>();

    public void setComparisonJSONfilter(BamItemType type, JSONFilter filter) {
        filters.put(type, filter);
    }

    public BamItem getCurrentBamItem() {
        return currentBamItem;
    }

    public void updateBackup() {
        if (currentBamItem != null) {
            bamItemBackupId = currentBamItem.ID;
            bamItemBackup = currentBamItem.save(true);
        } else {
            bamItemBackupId = "";
            bamItemBackup = null;
        }
    }

    public void updateCombobox() {

        BamItemList bamItemList = CHILD.PROJECT.BAM_ITEMS;
        BamItemList filteredBamItemList = bamItemList.filterByType(TYPES);
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
        if (currentBamItem == null) {
            cb.setSelectedItem(-1, true);
        } else {
            int index = allItems.indexOf(currentBamItem);
            cb.setSelectedItem(index, true);
            if (index == -1) {
                cb.fireChangeListeners();
            }
        }
    }

    public void selectDefaultBamItem() {
        if (allItems.size() == 1) {
            setCurrentBamItem(allItems.get(0));
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
        if (currentBamItem != null) {
            currentBamItem.removeChangeListener(onBamItemContentChange);
        }
        currentBamItem = bamItem;
        if (currentBamItem != null) {
            currentBamItem.addChangeListener(onBamItemContentChange);
        }
        int index = allItems.indexOf(currentBamItem);
        cb.setSelectedItem(index, true);
        fireChangeListeners();
    }

    private void setSyncStatus(boolean isCurrentInSyncWithBackup) {
        this.isCurrentInSyncWithBackup = isCurrentInSyncWithBackup;
    }

    public void setCanBeEmpty(boolean canBeEmpty) {
        this.canBeEmpty = canBeEmpty;
    }

    public boolean isConfigValid() {
        if (currentBamItem == null && bamItemBackup != null) {
            return false;
        }
        if (bamItemBackup == null && currentBamItem != null) {
            return bamItemBackupId == null; // if empty string, it means a run was done with no selection
        }
        if (bamItemBackup != null && currentBamItem != null) {
            return getSyncStatus();
        }
        if (bamItemBackup == null && currentBamItem == null) {
            return canBeEmpty;
        }
        return false;
    }

    public void updateValidityView() {
        cb.setValidityView(isConfigValid());
    }

    public void updateSyncStatus() {

        if (currentBamItem == null || bamItemBackup == null) {
            // content is different
            setSyncStatus(currentBamItem == null && bamItemBackup == null);
            return;
        }

        BamConfig currentBamItemBackup = currentBamItem.save(false);
        JSONObject backupFiltered = JSONFilter.filter(bamItemBackup.JSON, true, true, "_version");
        JSONObject currentFiltered = JSONFilter.filter(currentBamItemBackup.JSON, true, true, "_version");
        if (bamItemBackup.TYPE != null) {
            if (currentBamItemBackup.TYPE == null ||
                    bamItemBackup.TYPE != currentBamItemBackup.TYPE) {
                // BamItemType not matching
                setSyncStatus(false);
                return;
            }
            if (filters.containsKey(bamItemBackup.TYPE)) {
                backupFiltered = filters.get(bamItemBackup.TYPE).apply(backupFiltered);
                currentFiltered = filters.get(bamItemBackup.TYPE).apply(currentFiltered);
            }
        } else {
            if (filters.containsKey(TYPE)) {
                backupFiltered = filters.get(TYPE).apply(backupFiltered);
                currentFiltered = filters.get(TYPE).apply(currentFiltered);
            }
        }

        JSONCompareResult result = JSONCompare.compare(backupFiltered, currentFiltered);
        setSyncStatus(result.matching());
    }

    public boolean getSyncStatus() {
        return isCurrentInSyncWithBackup;
    }

    public MsgPanel getMessagePanel() {
        if (!isCurrentInSyncWithBackup) {
            if (bamItemBackup == null) {
                // there has been a run with an empty selection when bamItemBackupId != null
                return bamItemBackupId != null ? buildUnselectComponentMsgPanel() : null;
            }
            return buildCreateNewComponentFromBackupMsgPanel();
        } else {
            if (currentBamItem == null && !canBeEmpty) {
                return buildMissingRequiredSelectionMsgPanel();
            }
        }
        return null;

    }

    private MsgPanel buildCreateNewComponentFromBackupMsgPanel() {
        MsgPanel panel = new MsgPanel(MsgPanel.TYPE.ERROR);

        JButton createInSyncCompBtn = new JButton();
        createInSyncCompBtn.setIcon(TYPE.getAddIcon());
        createInSyncCompBtn.addActionListener((e) -> {
            if (bamItemBackup == null) {
                return;
            }
            BamItemType type = bamItemBackup.TYPE;
            BamItem bamItem = CHILD.PROJECT.addBamItem(type == null ? TYPE : type);
            try {
                bamItem.load(bamItemBackup);
            } catch (Exception loadError) {
                ConsoleLogger.error(loadError);
                CHILD.PROJECT.deleteBamItem(bamItem);
                CommonDialog.errorDialog("Error while loading backup component.");
                return;
            }
            CHILD.PROJECT.setCurrentBamItem(CHILD);
            setCurrentBamItem(bamItem);
        });

        panel.addButton(createInSyncCompBtn);

        String typeText = T.text(TYPE.id).toLowerCase();
        String currentItemName = getCurrentItemName();

        createInSyncCompBtn.setText(
                T.html("btn_create_component_from_backup", typeText));
        panel.message.setText(
                T.html(
                        "msg_component_content_out_of_sync",
                        typeText,
                        currentItemName));

        return panel;
    }

    private MsgPanel buildMissingRequiredSelectionMsgPanel() {
        MsgPanel panel = new MsgPanel(MsgPanel.TYPE.ERROR);
        String typeText = T.text(TYPE.id).toLowerCase();
        panel.message.setText(T.html("msg_no_component_selected", typeText));
        return panel;
    }

    private MsgPanel buildUnselectComponentMsgPanel() {
        MsgPanel panel = new MsgPanel(MsgPanel.TYPE.ERROR);
        String typeText = T.text(TYPE.id).toLowerCase();
        String currentItemName = getCurrentItemName();
        panel.message.setText(T.html("msg_component_content_out_of_sync", typeText, currentItemName));

        JButton unselectBtn = new JButton();
        unselectBtn.addActionListener((e) -> {
            cb.setSelectedItem(-1, false);
            fireChangeListeners();
        });
        unselectBtn.setText(T.html("btn_go_back_to_empty_selection", typeText));
        panel.addButton(unselectBtn);

        return panel;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (currentBamItem != null) {
            json.put("bamItemId", currentBamItem.ID);
        }
        if (bamItemBackup != null) {
            json.put("bamItemBackup", bamItemBackup.JSON);
            json.put("bamItemBackupId", bamItemBackupId);
        }
        return json;
    }

    private boolean doNotFireChangeListeners = false;

    public void fromJSON(JSONObject json, boolean doNotFireChangeListeners) {
        if (json.has("bamItemId")) {
            String bamItemId = json.getString("bamItemId");
            this.doNotFireChangeListeners = doNotFireChangeListeners;
            setCurrentBamItem(bamItemId);
            this.doNotFireChangeListeners = false;
        }
        if (json.has("bamItemBackup")) {
            JSONObject backupJson = json.getJSONObject("bamItemBackup");
            bamItemBackup = new BamConfig(backupJson);
            if (bamItemBackup.VERSION < 0 && backupJson.has("jsonObject")) {
                bamItemBackup = new BamConfig(backupJson.getJSONObject("jsonObject"));
            }
            bamItemBackupId = json.getString("bamItemBackupId");
        }
    }

    public void fromJSON(JSONObject json) {
        fromJSON(json, false);
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        if (!doNotFireChangeListeners) {
            for (ChangeListener l : changeListeners) {
                l.stateChanged(new ChangeEvent(this));
            }
        }
    }
}
