package org.baratinage.ui.bam;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItemType.BamItemBuilderFunction;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.utils.WriteFile;
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
            if (newName.equals("")) {
                newName = "<html><div style='color: red; font-style: italic'>" + T.text("untitled") + "</div></html>";
            }
            explorerItem.label = newName;
            explorer.updateItemView(explorerItem);
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

    public BamConfigRecord save() {
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

    public static BamProject load(JSONObject json) {
        BamProjectType projectType = BamProjectType.valueOf(json.getString("bamProjectType"));
        BamProject bamProject;
        if (projectType == BamProjectType.BARATIN) {
            bamProject = new BaratinProject();
        } else {
            return null;
        }
        JSONArray bamItemsJson = json.getJSONArray("bamItems");
        int n = bamItemsJson.length();
        for (int k = 0; k < n; k++) {
            JSONObject bamItemJson = bamItemsJson.getJSONObject(k);
            BamItemType itemType = BamItemType.valueOf(bamItemJson.getString("type"));
            String id = bamItemJson.getString("id");
            BamItem item = bamProject.addBamItem(itemType, id);
            item.bamItemNameField.setText(bamItemJson.getString("name"));
            item.bamItemDescriptionField.setText(bamItemJson.getString("description"));
            item.load(new BamConfigRecord(bamItemJson.getJSONObject("config")));
        }
        ExplorerItem exItem = bamProject.explorer.getLastSelectedPathComponent();
        if (exItem != null) {
            json.put("selectedItem", exItem.id);
        }
        if (json.has("selectedItemId")) {
            String id = json.getString("selectedItemId");
            bamProject.setCurrentBamItem(bamProject.getBamItem(id));
        }
        return bamProject;
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

    static public BamProject loadProject(String projectFilePath) {

        AppConfig.AC.clearTempDirectory();

        File projectFile = new File(projectFilePath);
        if (!projectFile.exists()) {
            System.err.println("BamProject Error: Project file doesn't exist! (" +
                    projectFilePath + ")");
            return null;
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

            return load(json);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

}
