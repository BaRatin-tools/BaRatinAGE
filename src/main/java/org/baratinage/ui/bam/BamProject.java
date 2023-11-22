package org.baratinage.ui.bam;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItemType.BamItemBuilderFunction;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.ReadWriteZip;
import org.baratinage.utils.fs.WriteFile;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.Performance;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BamProject extends RowColPanel {

    public final String ID;
    public final BamProjectType PROJECT_TYPE;
    public final BamItemList BAM_ITEMS;

    protected final Explorer EXPLORER;

    private final JMenu componentMenu;
    private final JToolBar projectToolbar;

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
        EXPLORER.headerLabel.setIcon(AppConfig.AC.ICONS.LIST_ICON);
        T.t(this, EXPLORER.headerLabel, false, "explorer");

        setupExplorer();

        // resetting toolbar and component menu (where "add bam item" buttons are)
        componentMenu = AppConfig.AC.APP_MAIN_FRAME.mainMenuBar.componentMenu;
        componentMenu.removeAll();
        projectToolbar = AppConfig.AC.APP_MAIN_FRAME.projectToolbar;
        projectToolbar.removeAll();

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
        JSONObject curr = JSONFilter.filter(
                BamConfigRecord.toJSON(currentConfigRecord),
                true, true,
                "selectedItemId");
        JSONObject saved = JSONFilter.filter(
                BamConfigRecord.toJSON(lastSavedConfigRecord),
                true, true,
                "selectedItemId");
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
        projectToolbar.add(addBamItemToolbarButton);

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
        explorerItem.contextMenu.add(addBamItemMenu);

        JMenuItem cloneMenuItem = new JMenuItem();
        T.t(bamItem, cloneMenuItem, false, "duplicate");
        cloneMenuItem.setIcon(AppConfig.AC.ICONS.COPY_ICON);
        explorerItem.contextMenu.add(cloneMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem();
        T.t(bamItem, deleteMenuItem, false, "delete");
        deleteMenuItem.setIcon(AppConfig.AC.ICONS.TRASH_ICON);
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
        String mainConfigFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR,
                "main_config.json").toString();

        BamConfigRecord bamConfig = save().addPaths(mainConfigFilePath);
        JSONObject json = bamConfig.jsonObject();

        String mainJsonString = json.toString(4);
        File mainConfigFile = new File(mainConfigFilePath);
        try {
            WriteFile.writeLines(mainConfigFile, new String[] { mainJsonString });
        } catch (

        IOException saveError) {
            ConsoleLogger.error("Failed to write main config JSON file!");
            ConsoleLogger.stackTrace(saveError);
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

    static private boolean bamProjectLoadingCanceled;
    static private Runnable doAfterBamItemsLoaded = () -> {
    };
    static final private ProgressFrame bamProjectLoadingFrame = new ProgressFrame();
    static final private List<BamItem> bamProjectBamItemsToLoad = new ArrayList<>();
    static final private List<BamConfigRecord> bamProjectBamItemsToLoadConfig = new ArrayList<>();
    static private int bamProjectLoadingProgress = -1;

    private static void load(JSONObject json, File sourceFile, Consumer<BamProject> onLoaded) {

        Performance.startTimeMonitoring(bamProjectLoadingFrame);

        // get bam items configs
        JSONArray bamItemsJson = json.getJSONArray("bamItems");
        int n = bamItemsJson.length();

        // Initalize loading monitoring frame
        RowColPanel p = new RowColPanel(RowColPanel.AXIS.COL);
        p.setGap(5);
        JLabel lMessage = new JLabel();
        String loadingMessage = T.text("loading_project");

        lMessage.setText("<html>" +
                "<b>" + sourceFile.getName() + "</b>" + "<br>" +
                "<code>" + sourceFile.getAbsolutePath() + "</code>" +
                "</html>");

        p.appendChild(lMessage);

        bamProjectLoadingFrame.openProgressFrame(
                AppConfig.AC.APP_MAIN_FRAME,
                p,
                loadingMessage,
                0,
                n,
                true);

        bamProjectLoadingFrame.updateProgress(loadingMessage, 0);
        bamProjectLoadingFrame.clearOnCancelActions();
        bamProjectLoadingFrame.addOnCancelAction(
                () -> {
                    bamProjectLoadingCanceled = true;
                });
        bamProjectLoadingProgress = 0;
        bamProjectLoadingCanceled = false;

        // get project type and create appropriate project
        BamProjectType projectType = BamProjectType.valueOf(json.getString("bamProjectType"));
        BamProject bamProject;
        if (projectType == BamProjectType.BARATIN) {
            bamProject = new BaratinProject();
        } else {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // create BamItems and prepare their configuration for actual loading
            bamProjectBamItemsToLoad.clear();
            bamProjectBamItemsToLoadConfig.clear();

            for (int k = 0; k < n; k++) {

                JSONObject bamItemJson = bamItemsJson.getJSONObject(k);

                BamItemType itemType = BamItemType.valueOf(bamItemJson.getString("type"));
                String id = bamItemJson.getString("id");
                BamItem item = bamProject.addBamItem(itemType, id);

                item.bamItemNameField.setText(bamItemJson.getString("name"));
                item.bamItemDescriptionField.setText(bamItemJson.getString("description"));

                bamProjectBamItemsToLoad.add(item);
                bamProjectBamItemsToLoadConfig.add(new BamConfigRecord(bamItemJson.getJSONObject("config")));
            }
        });

        // set loading of BamItems as next task to perform on EDT (eventDispatchThread)
        SwingUtilities.invokeLater(BamProject::loadNextBamItem); // invokeLater loop

        // sets the last step
        doAfterBamItemsLoaded = () -> {
            ExplorerItem exItem = bamProject.EXPLORER.getLastSelectedPathComponent();
            if (exItem != null) {
                json.put("selectedItem", exItem.id);
            }
            BamItem toSelectItem = null;
            if (json.has("selectedItemId")) {
                String id = json.getString("selectedItemId");
                if (id != null) {
                    toSelectItem = bamProject.getBamItem(id);

                }
            }
            if (toSelectItem == null && bamProject.BAM_ITEMS.size() > 0) {
                toSelectItem = bamProject.BAM_ITEMS.get(0);
            }
            if (toSelectItem != null) {
                bamProject.setCurrentBamItem(toSelectItem);
            }

            bamProjectLoadingProgress = -1;
            bamProjectLoadingFrame.done();

            if (!bamProjectLoadingCanceled) {
                onLoaded.accept(bamProject);
            }

            bamProject.createProjectBackupConfigRecord();
            Performance.endTimeMonitoring(bamProjectLoadingFrame);
        };

        return;
    }

    static private void loadNextBamItem() {
        if (bamProjectLoadingCanceled) {
            ConsoleLogger.log("loading was canceled.");
            return;
        }
        if (bamProjectLoadingProgress == -1) {
            ConsoleLogger.log("no BamItem to load.");
            return;
        }
        if (bamProjectLoadingProgress >= bamProjectBamItemsToLoad.size()) {
            ConsoleLogger.log("all BamItem loaded.");
            doAfterBamItemsLoaded.run();
            return;
        }

        BamConfigRecord config = bamProjectBamItemsToLoadConfig.get(bamProjectLoadingProgress);
        BamItem item = bamProjectBamItemsToLoad.get(bamProjectLoadingProgress);

        ConsoleLogger.log("Loading item " + item);

        String itemName = item.bamItemNameField.getText();
        String progressMsg = T.html(
                "loading_project_component",
                T.text(item.TYPE.id), itemName);
        bamProjectLoadingFrame.updateProgress(progressMsg, bamProjectLoadingProgress);

        Performance.startTimeMonitoring(item);
        item.load(config);
        Performance.endTimeMonitoring(item);
        bamProjectLoadingFrame.updateProgress(progressMsg, bamProjectLoadingProgress + 1);

        bamProjectLoadingProgress++;
        SwingUtilities.invokeLater(BamProject::loadNextBamItem);
    }

    static public void loadProject(String projectFilePath, Consumer<BamProject> onLoaded, Runnable onError) {

        AppConfig.AC.clearTempDirectory();

        File projectFile = new File(projectFilePath);
        if (!projectFile.exists()) {
            ConsoleLogger.error("Project file doesn't exist! (" +
                    projectFilePath + ")");
            onError.run();
            return;
        }
        try {
            ReadWriteZip.unzip(projectFilePath, AppConfig.AC.APP_TEMP_DIR);
        } catch (Exception e) {
            ConsoleLogger.error(e);
            onError.run();
            return;
        }

        try {
            BufferedReader bufReader = ReadFile
                    .createBufferedReader(Path.of(AppConfig.AC.APP_TEMP_DIR,
                            "main_config.json").toString(),
                            true);
            String jsonString = "";
            String jsonLine;
            while ((jsonLine = bufReader.readLine()) != null) {
                jsonString += jsonLine;
            }
            JSONObject json = new JSONObject(jsonString);

            load(json, projectFile, onLoaded);
        } catch (IOException e) {
            ConsoleLogger.error(e);
            onError.run();
            return;
        }
        return;
    }

}
