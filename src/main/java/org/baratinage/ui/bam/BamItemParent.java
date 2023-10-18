package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.json.JSONObject;

public class BamItemParent extends RowColPanel {

    private final BamItemType TYPE;
    private final BamItem CHILD;

    private final JLabel comboboxLabel;

    private final List<MsgPanel> messages;

    private BamItem backupItemForDatasetSafekeeping = null;

    private String backupItemString = null;
    private String backupItemId = null;
    private BamItemList allItems = new BamItemList();
    private BamItem currentItem = null;

    private final SimpleComboBox cb;

    private final ChangeListener onBamItemNameChange;
    private final ChangeListener onBamItemContentChange;

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
            cb.setValidityView(selectedIndex >= 0);
        });

        messages = new ArrayList<>();

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

        T.t(this, comboboxLabel, false, TYPE.id);
        T.updateHierarchy(this, messages);

        updateCombobox();
    }

    private String getCurrentItemName() {
        return currentItem == null ? T.text("msg_empty_selection")
                : currentItem.bamItemNameField.getText();
    }

    private String getBackupItemName() {
        String name = " &mdash; ";
        if (backupItemId != null) {
            BamItem item = allItems.getBamItemWithId(backupItemId);
            if (item != null) {
                name = item.bamItemNameField.getText();
            }
        }
        return name;

    }

    private Function<JSONObject, JSONObject> filter;

    public void setComparisonJSONfilter(Function<JSONObject, JSONObject> filter) {
        this.filter = filter;
    }

    public BamItem getCurrentBamItem() {
        return currentItem;
    }

    public void updateBackup() {
        backupItemId = currentItem.ID;
        JSONObject json = currentItem.toJSON();
        backupItemString = json.toString();

        backupItemForDatasetSafekeeping = TYPE.buildBamItem();
        backupItemForDatasetSafekeeping.fromJSON(json);
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
            currentItem.removeChangeListener(onBamItemContentChange);
        }
        currentItem = bamItem;
        if (currentItem != null) {
            currentItem.addChangeListener(onBamItemContentChange);
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
        JSONObject currentItemJson = currentItem.toJSON();

        JSONObject backupFiltered = backupItemJson;
        JSONObject currentFiltered = currentItemJson;

        if (filter != null) {
            backupFiltered = filter.apply(backupItemJson);
            currentFiltered = filter.apply(currentItemJson);
        }

        JSONCompareResult result = JSONCompare.compare(backupFiltered, currentFiltered);

        return result.matching();
    }

    public boolean isBamRerunRequired() {
        if (backupItemString == null || backupItemId == null) {
            return false;
        }
        return !isCurrentInSyncWithBackup();
    }

    public List<MsgPanel> getMessages() {
        messages.clear();
        T.clear(messages);

        if (backupItemString != null && backupItemId != null) {
            boolean inSync = isCurrentInSyncWithBackup();

            if (!inSync) {

                MsgPanel syncIssueMsg = new MsgPanel(MsgPanel.TYPE.ERROR);
                JButton revertToSelectCompBtn = new JButton();
                JButton createInSyncCompBtn = new JButton();
                createInSyncCompBtn.setIcon(TYPE.getAddIcon());

                revertToSelectCompBtn.addActionListener((e) -> {
                    if (backupItemId == null) {
                        return;
                    }
                    BamItem item = allItems.getBamItemWithId(backupItemId);
                    if (item == null) {
                        JOptionPane.showConfirmDialog(
                                AppConfig.AC.APP_MAIN_FRAME,
                                T.text("impossible_component_deleted"),
                                T.text("error"),
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

                if (hasSelectionChanged() && allItems.getBamItemWithId(backupItemId) != null) {
                    syncIssueMsg.addButton(revertToSelectCompBtn);
                }
                syncIssueMsg.addButton(createInSyncCompBtn);
                messages.add(syncIssueMsg);

                T.t(messages, () -> {
                    String typeText = T.text(TYPE.id);
                    String backupItemName = getBackupItemName();
                    String currentItemName = getCurrentItemName();
                    syncIssueMsg.message.setText(
                            T.html("msg_component_content_out_of_sync", typeText, currentItemName));
                    revertToSelectCompBtn.setText(
                            T.html("btn_revert_component_selection", backupItemName));
                    createInSyncCompBtn.setText(
                            T.html("btn_create_component_from_backup", typeText));
                });

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

            // will write dataset (if any) and register it to PROJECT for zipping
            backupItemForDatasetSafekeeping.toJSON();
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

            backupItemForDatasetSafekeeping = TYPE.buildBamItem();
            backupItemForDatasetSafekeeping.fromJSON(new JSONObject(backupItemString));
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
