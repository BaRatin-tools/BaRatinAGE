package org.baratinage.ui.bam;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItemType.BamItemBuilderFunction;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.utils.WriteFile;
import org.baratinage.utils.perf.Performance;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BamProject extends RowColPanel {

    protected enum BamProjectType {
        BARATIN
    }

    public final BamProjectType PROJECT_TYPE;
    public final BamItemList BAM_ITEMS;
    public final List<ExplorerItem> EXPLORER_ITEMS;

    private String projectPath = null;

    protected JSplitPane content;
    protected JToolBar toolBar;
    protected Explorer explorer;
    protected RowColPanel currentPanel;

    protected JMenu projectMenu;

    public BamProject(BamProjectType projectType) {
        super(AXIS.COL);

        PROJECT_TYPE = projectType;
        BAM_ITEMS = new BamItemList();
        EXPLORER_ITEMS = new ArrayList<>();

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        appendChild(toolBar, 0);
        appendChild(new JSeparator(), 0);

        content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());
        appendChild(content, 1);

        explorer = new Explorer();
        // T.t(explorer.headerLabel, false, "explorer");
        T.t(this, explorer.headerLabel, false, "explorer");

        setupExplorer();

        currentPanel = new RowColPanel(AXIS.COL);
        currentPanel.setGap(5);

        content.setLeftComponent(explorer);
        content.setRightComponent(currentPanel);
        content.setResizeWeight(0);

    }

    private void setupExplorer() {

        this.explorer.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = explorer.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = getBamItem(explorerItem.id);
                if (bamItem != null) {
                    this.currentPanel.clear();
                    this.currentPanel.appendChild(bamItem, 1);

                } else {
                    System.out.println("BamProject: selected BamItem is null");
                    this.currentPanel.clear();
                }
                this.updateUI();
            }
        });

    }

    public void setCurrentBamItem(BamItem bamItem) {
        ExplorerItem item = explorer.getItem(bamItem.ID);
        if (item != null) {
            explorer.selectItem(item);
        }
    }

    protected void initBamItemType(
            BamItemType itemType,
            BamItemBuilderFunction builder) {

        itemType.setBuilderFunction(builder);

        ActionListener onAdd = (e) -> {
            addBamItem(itemType);
        };

        String tKey = itemType.id;
        String tCreateKey = "create_" + itemType.id;

        JMenuItem menuButton = new JMenuItem();
        menuButton.setIcon(itemType.getAddIcon());
        menuButton.addActionListener(onAdd);
        projectMenu.add(menuButton);

        JButton toolbarButton = new JButton(itemType.getAddIcon());
        toolbarButton.addActionListener(onAdd);
        toolBar.add(toolbarButton);

        ExplorerItem explorerItem = new ExplorerItem(
                itemType.id,
                T.text(tKey),
                itemType.getIcon());
        this.explorer.appendItem(explorerItem);

        T.t(this, menuButton, false, tCreateKey);
        T.t(this, () -> toolbarButton.setToolTipText(T.text(tCreateKey)));
        T.t(this, () -> {
            explorerItem.label = T.text(tKey);
            explorer.updateItemView(explorerItem);
        });

    }

    protected BamItem addBamItem(BamItem bamItem) {

        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.ID,
                bamItem.bamItemNameField.getText(),
                bamItem.TYPE.getIcon(),
                explorer.getItem(bamItem.TYPE.id));

        T.t(bamItem, bamItem.bamItemTypeLabel, false, bamItem.TYPE.id);

        bamItem.bamItemTypeLabel.setIcon(bamItem.TYPE.getIcon());
        bamItem.cloneButton.addActionListener((e) -> {
            BamItem clonedBamItem = bamItem.TYPE.buildBamItem();
            clonedBamItem.load(bamItem.save(false));
            clonedBamItem.bamItemNameField.setText(bamItem.bamItemNameField.getText());
            clonedBamItem.bamItemDescriptionField.setText(bamItem.bamItemDescriptionField.getText());
            clonedBamItem.setCopyName();
            addBamItem(clonedBamItem);
        });

        bamItem.cloneButton.setIcon(SvgIcon.buildFeatherAppImageIcon("copy.svg"));
        bamItem.deleteButton.setIcon(SvgIcon.buildFeatherAppImageIcon("trash.svg"));

        T.t(bamItem, bamItem.cloneButton, false, "duplicate");
        T.t(bamItem, bamItem.deleteButton, false, "delete");
        T.updateHierarchy(this, bamItem);

        BAM_ITEMS.add(bamItem);
        EXPLORER_ITEMS.add(explorerItem);

        bamItem.bamItemNameField.addChangeListener((e) -> {
            String newName = bamItem.bamItemNameField.getText();
            explorerItem.label = newName;
            explorer.updateItemView(explorerItem);
        });
        bamItem.bamItemNameField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (bamItem.bamItemNameField.getText().equals("")) {
                    bamItem.bamItemNameField.setText(T.text("untitled"));
                }
            }

        });

        bamItem.deleteButton.addActionListener((e) -> {
            int response = JOptionPane.showConfirmDialog(this,
                    T.html("delete_component_question", bamItem.bamItemNameField.getText()),
                    T.text("warning"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                deleteBamItem(bamItem, explorerItem);
            }
        });

        this.explorer.appendItem(explorerItem);
        this.explorer.selectItem(explorerItem);

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

    protected void deleteBamItem(BamItem bamItem, ExplorerItem explorerItem) {
        BAM_ITEMS.remove(bamItem);
        EXPLORER_ITEMS.remove(explorerItem);
        this.explorer.removeItem(explorerItem);
        this.explorer.selectItem(explorerItem.parentItem);
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
        JSONObject json = new JSONObject();
        List<String> files = new ArrayList<>();
        JSONArray bamItemsJson = new JSONArray();
        BamItemList bamItemList = getOrderedBamItemList();
        int n = bamItemList.size();
        for (int k = 0; k < n; k++) {
            BamItem item = bamItemList.get(k);
            BamConfigRecord itemConfig = item.save(true);
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
        ExplorerItem exItem = explorer.getLastSelectedPathComponent();
        if (exItem != null) {
            json.put("selectedItemId", exItem.id);
        }

        return new BamConfigRecord(json, files.toArray(new String[files.size()]));
    }

    // needs to return the item in the order in wich they must be loaded
    public abstract BamItemList getOrderedBamItemList();

    public void saveProject(String saveFilePath) {

        System.out.println("BamProject:  saving project...");
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
            System.err.println("BamProject Error: Failed to write main config JSON file!");
            saveError.printStackTrace();
            return;
        }

        String[] filePaths = bamConfig.filePaths();

        boolean success = ReadWriteZip.flatZip(saveFilePath, filePaths);
        if (success) {
            System.out.println("BamProject: project saved!");
        } else {
            System.err.println("BamProject Error: an error occured while saving project!");
        }

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
        JLabel lMain = new JLabel();
        JLabel lSecondary = new JLabel();
        String loadingMessage = T.text("loading_project");

        lSecondary.setText("<html>" +
                "<b>" + sourceFile.getName() + "</b>" + "&nbsp;&nbsp;" +
                "<code>" + sourceFile.getAbsolutePath() + "</code>" +
                "</html>");

        lMain.setText(loadingMessage);
        lMain.setIcon(new SvgIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "icon.svg").toString(), 32, 32));

        p.appendChild(lMain);
        p.appendChild(lSecondary);

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
            ExplorerItem exItem = bamProject.explorer.getLastSelectedPathComponent();
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

            Performance.endTimeMonitoring(bamProjectLoadingFrame);
        };

        return;
    }

    static private void loadNextBamItem() {
        if (bamProjectLoadingCanceled) {
            System.out.println("BamProject: loading was canceled.");
            return;
        }
        if (bamProjectLoadingProgress == -1) {
            System.out.println("BamProject: no BamItem to load.");
            return;
        }
        if (bamProjectLoadingProgress >= bamProjectBamItemsToLoad.size()) {
            System.out.println("BamProject: all BamItem loaded.");
            doAfterBamItemsLoaded.run();
            return;
        }

        BamConfigRecord config = bamProjectBamItemsToLoadConfig.get(bamProjectLoadingProgress);
        BamItem item = bamProjectBamItemsToLoad.get(bamProjectLoadingProgress);

        System.out.println("BamProject: Loading item " + item);

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

    static public void loadProject(String projectFilePath, Consumer<BamProject> onLoaded) {

        AppConfig.AC.clearTempDirectory();

        File projectFile = new File(projectFilePath);
        if (!projectFile.exists()) {
            System.err.println("BamProject Error: Project file doesn't exist! (" +
                    projectFilePath + ")");
            return;
        }

        ReadWriteZip.unzip(projectFilePath, AppConfig.AC.APP_TEMP_DIR);

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
            e.printStackTrace();
        }
        return;
    }

}
