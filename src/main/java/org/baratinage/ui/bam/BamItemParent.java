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
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.json.JSONObject;

public class BamItemParent extends RowColPanel {

    private final BamItemType TYPE;
    private final BamItem CHILD;

    private final JLabel comboboxLabel;

    private final List<MsgPanel> messages;
    private final String errorMessagesKey = Misc.getTimeStampedId();

    private String bamItemBackupId = null;
    private BamConfigRecord bamItemBackup = null;

    private BamItemList allItems = new BamItemList();
    private BamItem currentBamItem = null;

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
        });
        cb.setValidityView(false);

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

        updateCombobox();
    }

    private String getCurrentItemName() {
        return currentBamItem == null ? T.text("msg_empty_selection")
                : currentBamItem.bamItemNameField.getText();
    }

    private String getBackupItemName() {
        String name = " &mdash; ";
        if (bamItemBackupId != null) {
            BamItem item = allItems.getBamItemWithId(bamItemBackupId);
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

    private boolean hasSelectionChanged() {
        if (bamItemBackup == null) {
            // no backup, irrelevant
            return false;
        }
        if (currentBamItem == null) {
            // selection must have changed
            return true;
        }
        return !bamItemBackupId.equals(currentBamItem.ID);
    }

    private boolean isCurrentInSyncWithBackup() {
        if (bamItemBackup == null) {
            // no backup, irrelevant
            return false;
        }
        if (currentBamItem == null) {
            // content is different
            return false;
        }

        BamConfigRecord currentBamItemBackup = currentBamItem.save(false);

        JSONObject backupFiltered = bamItemBackup.jsonObject();
        JSONObject currentFiltered = currentBamItemBackup.jsonObject();

        if (filter != null) {
            backupFiltered = filter.apply(backupFiltered);
            currentFiltered = filter.apply(currentFiltered);
        }

        JSONCompareResult result = JSONCompare.compare(backupFiltered, currentFiltered);

        return result.matching();
    }

    public List<MsgPanel> getMessages() {
        messages.clear();
        T.clear(errorMessagesKey);

        if (bamItemBackup != null && bamItemBackupId != null) {
            boolean inSync = isCurrentInSyncWithBackup();

            if (!inSync) {

                MsgPanel syncIssueMsg = new MsgPanel(MsgPanel.TYPE.ERROR);
                JButton revertToSelectCompBtn = new JButton();
                JButton createInSyncCompBtn = new JButton();
                createInSyncCompBtn.setIcon(TYPE.getAddIcon());

                revertToSelectCompBtn.addActionListener((e) -> {
                    if (bamItemBackupId == null) {
                        return;
                    }
                    BamItem item = allItems.getBamItemWithId(bamItemBackupId);
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
                    if (bamItemBackup == null) {
                        return;
                    }
                    BamItem bamItem = CHILD.PROJECT.addBamItem(TYPE);
                    bamItem.load(bamItemBackup);
                    CHILD.PROJECT.setCurrentBamItem(CHILD);
                    setCurrentBamItem(bamItem);
                });

                if (hasSelectionChanged() && allItems.getBamItemWithId(bamItemBackupId) != null) {
                    syncIssueMsg.addButton(revertToSelectCompBtn);
                }
                syncIssueMsg.addButton(createInSyncCompBtn);
                messages.add(syncIssueMsg);

                T.t(errorMessagesKey, () -> {
                    String typeText = T.text(TYPE.id).toLowerCase();
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
        cb.setValidityView(messages.size() == 0 && cb.getSelectedIndex() != -1);
        return messages;
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
