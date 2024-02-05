package org.baratinage.ui.bam;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItemType.BamItemBuilderFunction;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadWriteZip;
import org.baratinage.utils.fs.WriteFile;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BamProject extends RowColPanel {

    public final String ID;
    public final BamProjectType PROJECT_TYPE;
    public final BamItemList BAM_ITEMS;

    protected final Explorer EXPLORER;

    private final JMenu componentMenu;

    private String projectPath = null;

    protected final SplitContainer content;
    protected final RowColPanel currentPanel;

    private BamConfigRecord lastSavedConfigRecord;
    private boolean unsavedChanges = false;

    public BamProject(BamProjectType projectType) {
        super(AXIS.COL);

        // main instance public object
        ID = Misc.getTimeStampedId();
        PROJECT_TYPE = projectType;
        BAM_ITEMS = new BamItemList();

        // setting explorer
        EXPLORER = new Explorer();
        EXPLORER.headerLabel.setIcon(AppSetup.ICONS.LIST);
        T.t(this, EXPLORER.headerLabel, false, "explorer");

        setupExplorer();

        // resetting toolbar and component menu (where "add bam item" buttons are)
        componentMenu = AppSetup.MAIN_FRAME.mainMenuBar.componentMenu;
        componentMenu.removeAll();
        AppSetup.MAIN_FRAME.mainToolBars.clearBamItemTools();

        // inialialize current panel, place holder for bam item panels
        currentPanel = new RowColPanel(AXIS.COL);
        currentPanel.setGap(5);

        // final layout
        content = new SplitContainer(EXPLORER, currentPanel, true);
        appendChild(content, 1);
        content.setLeftComponent(EXPLORER);
        content.setRightComponent(currentPanel);
        content.setResizeWeight(0);

        // inialialize last save record
        lastSavedConfigRecord = null;
    }

    public boolean checkUnsavedChange() {
        if (lastSavedConfigRecord == null) {
            unsavedChanges = true;
            return true;
        }
        BamConfigRecord currentConfigRecord = save(false);
        JSONFilter filter = new JSONFilter(true, true, "selectedItemId");
        JSONObject curr = filter.apply(BamConfigRecord.toJSON(currentConfigRecord));
        JSONObject saved = filter.apply(BamConfigRecord.toJSON(lastSavedConfigRecord));
        JSONCompareResult compareRes = JSONCompare.compare(curr, saved);
        if (!compareRes.matching() != unsavedChanges) {
            unsavedChanges = !compareRes.matching();
            return true;
        }
        return false;
    }

    public boolean hasUnsavedChange() {
        return unsavedChanges;
    }

    private void createProjectBackupConfigRecord() {
        lastSavedConfigRecord = save(false);
    }

    private void setupExplorer() {

        EXPLORER.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = EXPLORER.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = getBamItem(explorerItem.id);
                if (bamItem != null) {
                    currentPanel.clear();
                    currentPanel.appendChild(bamItem, 1);
                } else {
                    ConsoleLogger.log("selected BamItem is null");
                    currentPanel.clear();
                }
                updateUI();
            }
        });

    }

    public void setCurrentBamItem(BamItem bamItem) {
        ExplorerItem item = EXPLORER.getItem(bamItem.ID);
        if (item != null) {
            EXPLORER.selectItem(item);
        }
    }

    protected void initBamItemType(
            BamItemType itemType,
            BamItemBuilderFunction builder) {

        itemType.setBamItemBuilderFunction(builder);

        ExplorerItem explorerItem = new ExplorerItem(
                itemType.id,
                T.text(itemType.id),
                itemType.getIcon());

        EXPLORER.appendItem(explorerItem);

        ActionListener addBamItemAction = (e) -> {
            addBamItem(itemType);
        };

        JButton addBamItemToolbarButton = new JButton();
        addBamItemToolbarButton.addActionListener(addBamItemAction);
        addBamItemToolbarButton.setIcon(itemType.getAddIcon());
        AppSetup.MAIN_FRAME.mainToolBars.addBamItemTool(addBamItemToolbarButton);

        JMenuItem addBamItemMenuBarItem = new JMenuItem();
        addBamItemMenuBarItem.addActionListener(addBamItemAction);
        addBamItemMenuBarItem.setIcon(itemType.getAddIcon());
        componentMenu.add(addBamItemMenuBarItem);

        JMenuItem addBamItemContextMenuItem = new JMenuItem();
        addBamItemContextMenuItem.addActionListener(addBamItemAction);
        addBamItemContextMenuItem.setIcon(itemType.getAddIcon());
        explorerItem.contextMenu.add(addBamItemContextMenuItem);

        T.t(this, () -> {
            explorerItem.label = T.text(itemType.id);
            EXPLORER.updateItemView(explorerItem);
            String tCreateKey = "create_" + itemType.id;
            addBamItemMenuBarItem.setText(T.text(tCreateKey));
            addBamItemToolbarButton.setToolTipText(T.text(tCreateKey));
            addBamItemContextMenuItem.setText(T.text(tCreateKey));
        });

    }

    protected BamItem addBamItem(BamItem bamItem) {

        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.ID,
                bamItem.bamItemNameField.getText(),
                bamItem.TYPE.getIcon(),
                EXPLORER.getItem(bamItem.TYPE.id));

        T.updateHierarchy(this, bamItem);

        BAM_ITEMS.add(bamItem);

        JMenuItem addBamItemMenu = new JMenuItem();
        T.t(bamItem, addBamItemMenu, false, "create_" + bamItem.TYPE.id);
        addBamItemMenu.setIcon(bamItem.TYPE.getAddIcon());
        addBamItemMenu.addActionListener((e) -> {
            addBamItem(bamItem.TYPE);
        });
        explorerItem.contextMenu.add(addBamItemMenu);

        JMenuItem cloneMenuItem = new JMenuItem();
        T.t(bamItem, cloneMenuItem, false, "duplicate");
        cloneMenuItem.setIcon(AppSetup.ICONS.COPY);
        cloneMenuItem.addActionListener(bamItem.cloneButton.getActionListeners()[0]);
        explorerItem.contextMenu.add(cloneMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem();
        T.t(bamItem, deleteMenuItem, false, "delete");
        deleteMenuItem.setIcon(AppSetup.ICONS.TRASH);
        deleteMenuItem.addActionListener(bamItem.deleteButton.getActionListeners()[0]);
        explorerItem.contextMenu.add(deleteMenuItem);

        EXPLORER.appendItem(explorerItem);
        EXPLORER.selectItem(explorerItem);

        return bamItem;

    }

    public BamItem addBamItem(BamItemType type, String uuid) {
        BamItem bamItem = type.buildBamItem(uuid);
        bamItem.bamItemNameField.setText(BAM_ITEMS.getDefaultName(type));
        return addBamItem(bamItem);
    }

    public BamItem addBamItem(BamItemType type) {
        BamItem bamItem = type.buildBamItem();
        bamItem.bamItemNameField.setText(BAM_ITEMS.getDefaultName(type));
        return addBamItem(bamItem);
    }

    protected void deleteBamItem(BamItem bamItem) {
        ExplorerItem explorerItem = EXPLORER.getItem(bamItem.ID);
        BAM_ITEMS.remove(bamItem);
        EXPLORER.removeItem(explorerItem);
        EXPLORER.selectItem(explorerItem.parentItem);
        T.clear(bamItem);
    }

    public BamItem getBamItem(String id) {
        for (BamItem item : BAM_ITEMS) {
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

    private BamConfigRecord save() {
        return save(true);
    }

    private BamConfigRecord save(boolean writeToFile) {
        JSONObject json = new JSONObject();
        List<String> files = new ArrayList<>();
        JSONArray bamItemsJson = new JSONArray();
        BamItemList bamItemList = getOrderedBamItemList();
        int n = bamItemList.size();
        for (int k = 0; k < n; k++) {
            BamItem item = bamItemList.get(k);
            BamConfigRecord itemConfig = item.save(writeToFile);
            JSONObject bamItemJson = new JSONObject();
            bamItemJson.put("id", item.ID);
            bamItemJson.put("type", item.TYPE.toString());
            bamItemJson.put("name", item.bamItemNameField.getText());
            bamItemJson.put("description", item.bamItemDescriptionField.getText());
            bamItemJson.put("config", itemConfig.jsonObject());
            bamItemsJson.put(k, bamItemJson);
            for (String file : itemConfig.filePaths()) {
                files.add(file);
            }
        }
        json.put("fileVersion", 0);
        json.put("bamProjectType", PROJECT_TYPE.toString());
        json.put("bamItems", bamItemsJson);
        ExplorerItem exItem = EXPLORER.getLastSelectedPathComponent();
        if (exItem != null) {
            json.put("selectedItemId", exItem.id);
        }

        return new BamConfigRecord(json, files.toArray(new String[files.size()]));
    }

    // needs to return the item in the order in wich they must be loaded
    public abstract BamItemList getOrderedBamItemList();

    public void saveProject(String saveFilePath) {

        ConsoleLogger.log("saving project...");
        String mainConfigFilePath = Path.of(AppSetup.PATH_APP_TEMP_DIR,
                "main_config.json").toString();

        BamConfigRecord bamConfig = save().addPaths(mainConfigFilePath);
        JSONObject json = bamConfig.jsonObject();

        String mainJsonString = json.toString(4);
        File mainConfigFile = new File(mainConfigFilePath);
        try {
            WriteFile.writeLines(mainConfigFile, new String[] { mainJsonString });
        } catch (

        IOException saveError) {
            ConsoleLogger.error("Failed to write main config JSON file!\n" + saveError);
            return;
        }

        String[] filePaths = bamConfig.filePaths();

        boolean success = ReadWriteZip.flatZip(saveFilePath, filePaths);
        if (success) {
            ConsoleLogger.log("project saved!");
        } else {
            ConsoleLogger.error("an error occured while saving project!");
        }

        createProjectBackupConfigRecord();
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectName() {
        if (projectPath == null) {
            return "";
        }
        String fileName = Path.of(projectPath).getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileName = fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
