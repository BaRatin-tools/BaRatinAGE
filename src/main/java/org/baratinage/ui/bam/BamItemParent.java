package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.json.JSONObject;

public class BamItemParent extends RowColPanel {

    private final BamItemType TYPE;
    private final BamItem CHILD;

    private final JLabel comboboxLabel;

    // private final String errorMessagesKey = Misc.getTimeStampedId();

    private String bamItemBackupId = null;
    private BamConfigRecord bamItemBackup = null;

    private BamItemList allItems = new BamItemList();
    private BamItem currentBamItem = null;

    private boolean isCurrentInSyncWithBackup = false;

    private final SimpleComboBox cb;

    private final ChangeListener onBamItemNameChange;
    private final ChangeListener onBamItemContentChange;

    private final MsgPanel outOfSyncMsgPanel;
    private final MsgPanel missingSelectionMsgPanel;

    public BamItemParent(
            BamItem child,
            BamItemType type) {

        super(AXIS.COL);
        setGap(5);
        setPadding(5);

        TYPE = type;
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

        appendChild(comboboxLabel, 0);
        appendChild(cb, 1);

        onBamItemNameChange = (chEvt) -> {
            syncWithBamItemList();
            T.updateTranslation(this);
        };

        onBamItemContentChange = (chEvt) -> {
            fireChangeListeners();
        };

        outOfSyncMsgPanel = new MsgPanel(MsgPanel.TYPE.ERROR);
        JButton createInSyncCompBtn = new JButton();
        createInSyncCompBtn.setIcon(TYPE.getAddIcon());
        createInSyncCompBtn.addActionListener((e) -> {
            if (bamItemBackup == null) {
                return;
            }
            BamItem bamItem = CHILD.PROJECT.addBamItem(TYPE);
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
        outOfSyncMsgPanel.addButton(createInSyncCompBtn);

        missingSelectionMsgPanel = new MsgPanel(MsgPanel.TYPE.ERROR);

        T.t(this, () -> {
            String typeText = T.text(TYPE.id).toLowerCase();
            String currentItemName = getCurrentItemName();
            outOfSyncMsgPanel.message.setText(
                    T.html("msg_component_content_out_of_sync", typeText, currentItemName));
            createInSyncCompBtn.setText(
                    T.html("btn_create_component_from_backup", typeText));
            missingSelectionMsgPanel.message.setText(T.html("msg_no_component_selected", typeText));
        });

        T.t(this, comboboxLabel, false, TYPE.id);

        updateCombobox();
    }

    private String getCurrentItemName() {
        return currentBamItem == null ? T.text("msg_empty_selection")
                : currentBamItem.bamItemNameField.getText();
    }

    private JSONFilter filter;

    public void setComparisonJSONfilter(JSONFilter filter) {
        this.filter = filter;
    }

    public BamItem getCurrentBamItem() {
        return currentBamItem;
    }

    public void updateBackup() {
        bamItemBackupId = currentBamItem.ID;
        bamItemBackup = currentBamItem.save(true);
    }

    public void updateCombobox() {

        BamItemList bamItemList = CHILD.PROJECT.BAM_ITEMS;
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
        cb.setValidityView(isCurrentInSyncWithBackup);
    }

    public void updateSyncStatus() {

        if (currentBamItem == null) {
            // content is different
            setSyncStatus(false);
            return;
        }

        if (bamItemBackup == null) {
            // no backup, irrelevant
            setSyncStatus(true);
            return;
        }

        BamConfigRecord currentBamItemBackup = currentBamItem.save(false);
        JSONObject backupFiltered = bamItemBackup.jsonObject();
        JSONObject currentFiltered = currentBamItemBackup.jsonObject();
        if (filter != null) {
            backupFiltered = filter.apply(backupFiltered);
            currentFiltered = filter.apply(currentFiltered);
        }
        JSONCompareResult result = JSONCompare.compare(backupFiltered, currentFiltered);
        setSyncStatus(result.matching());
    }

    public boolean getSyncStatus() {
        return isCurrentInSyncWithBackup;
    }

    public MsgPanel getOutOfSyncMessage() {
        T.updateTranslation(this);
        if (currentBamItem == null) {
            return missingSelectionMsgPanel;
        }
        return outOfSyncMsgPanel;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (currentBamItem != null) {
            json.put("bamItemId", currentBamItem.ID);
        }
        if (bamItemBackup != null) {
            json.put("bamItemBackup", BamConfigRecord.toJSON(bamItemBackup));
            json.put("bamItemBackupId", bamItemBackupId);
        }
        return json;
    }

    public void fromJSON(JSONObject json) {
        if (json.has("bamItemId")) {
            String bamItemId = json.getString("bamItemId");
            setCurrentBamItem(bamItemId);
        }
        if (json.has("bamItemBackup")) {
            bamItemBackup = BamConfigRecord.fromJSON(json.getJSONObject("bamItemBackup"));
            bamItemBackupId = json.getString("bamItemBackupId");
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
}
