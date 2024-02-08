package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.swing.JMenuItem;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
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

    private record ProjectBamItem(BamItemType type, String categoryId, Function<String, BamItem> builder) {
    };

    private final HashMap<BamItemType, ProjectBamItem> projectBamItems = new HashMap<>();

    protected void initBamItemType(
            BamItemType itemType,
            String categoryId,
            Function<String, BamItem> bamItemBuilder) {

        ExplorerItem categoryItem = EXPLORER.getItem(categoryId);
        if (categoryItem == null) {
            ExplorerItem newCategoryItem = new ExplorerItem(
                    categoryId,
                    "",
                    AppSetup.ICONS.getCustomAppImageIcon(categoryId + ".svg"));
            T.t(this, () -> {
                newCategoryItem.label = T.text(categoryId);
                EXPLORER.updateItemView(newCategoryItem);
            });
            categoryItem = newCategoryItem;
            EXPLORER.appendItem(categoryItem);
        }

        projectBamItems.put(itemType, new ProjectBamItem(itemType, categoryId, bamItemBuilder));

    }

    protected BamItem addBamItem(BamItem bamItem) {

        ProjectBamItem pBamItem = projectBamItems.get(bamItem.TYPE);
        if (pBamItem == null) {
            ConsoleLogger.error("Cannot find item type '" + bamItem.TYPE + "'!");
            return null;
        }

        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.ID,
                bamItem.bamItemNameField.getText(),
                bamItem.TYPE.getIcon(),
                EXPLORER.getItem(pBamItem.categoryId));

        T.updateHierarchy(this, bamItem);

        BAM_ITEMS.add(bamItem);

        explorerItem.contextMenu.add(
                BamItem.getAddBamItemBtn(
                        new JMenuItem(),
                        this,
                        bamItem.TYPE,
                        true, true));

        explorerItem.contextMenu.add(bamItem.getCloneBamItemBtn(
                new JMenuItem(),
                true, true));
        explorerItem.contextMenu.add(bamItem.getDeleteBamItemBtn(
                new JMenuItem(),
                true, true));

        EXPLORER.appendItem(explorerItem);
        EXPLORER.selectItem(explorerItem);

        return bamItem;

    }

    public BamItem addBamItem(BamItemType itemType, String uuid) {
        ProjectBamItem pBamItem = projectBamItems.get(itemType);
        if (pBamItem == null) {
            ConsoleLogger.error("Cannot find item type '" + itemType + "'!");
            return null;
        }
        BamItem bamItem = pBamItem.builder.apply(uuid);
        bamItem.bamItemNameField.setText(BAM_ITEMS.getDefaultName(itemType));
        return addBamItem(bamItem);
    }

    public BamItem addBamItem(BamItemType type) {
        return addBamItem(type, Misc.getTimeStampedId());
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
