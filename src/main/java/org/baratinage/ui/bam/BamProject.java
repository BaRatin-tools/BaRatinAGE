package org.baratinage.ui.bam;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItemType.BamItemBuilderFunction;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.utils.WriteFile;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BamProject extends RowColPanel {

    public final BamItemList BAM_ITEMS;
    public final List<ExplorerItem> EXPLORER_ITEMS;

    private String projectPath = null;

    protected JSplitPane content;
    protected JToolBar toolBar;
    protected Explorer explorer;
    protected RowColPanel currentPanel;

    protected JMenu projectMenu;

    public BamProject() {
        super(AXIS.COL);

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
        Lg.register(explorer.headerLabel, "explorer");

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

        String lgKey = itemType.id;
        String lgCreateKey = "create_" + itemType.id;

        JMenuItem menuButton = new JMenuItem();
        menuButton.setIcon(itemType.getAddIcon());
        menuButton.addActionListener(onAdd);
        projectMenu.add(menuButton);

        JButton toolbarButton = new JButton(itemType.getAddIcon());
        toolbarButton.addActionListener(onAdd);
        toolBar.add(toolbarButton);

        ExplorerItem explorerItem = new ExplorerItem(
                itemType.id,
                Lg.text(lgKey),
                itemType.getIcon());
        this.explorer.appendItem(explorerItem);

        Lg.register(itemType, () -> {
            String createText = Lg.text(lgCreateKey);
            menuButton.setText(createText);
            toolbarButton.setToolTipText(createText);
            explorerItem.label = Lg.text(lgKey);
            explorer.updateItemView(explorerItem);
        });
    }

    protected BamItem addBamItem(BamItem bamItem) {

        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.ID,
                bamItem.bamItemNameField.getText(),
                bamItem.TYPE.getIcon(),
                explorer.getItem(bamItem.TYPE.id));

        Lg.register(bamItem.bamItemTypeLabel, bamItem.TYPE.id);

        bamItem.bamItemTypeLabel.setIcon(bamItem.TYPE.getIcon());
        bamItem.cloneButton.addActionListener((e) -> {
            BamItem clonedItem = bamItem.clone();
            clonedItem.setCopyName();
            addBamItem(clonedItem);
        });

        bamItem.cloneButton.setIcon(SvgIcon.buildFeatherAppImageIcon("copy.svg"));
        bamItem.deleteButton.setIcon(SvgIcon.buildFeatherAppImageIcon("trash.svg"));

        Lg.register(bamItem.cloneButton, "duplicate");
        Lg.register(bamItem.deleteButton, "delete");

        BAM_ITEMS.add(bamItem);
        EXPLORER_ITEMS.add(explorerItem);

        bamItem.bamItemNameField.addChangeListener((e) -> {
            String newName = bamItem.bamItemNameField.getText();
            if (newName.equals("")) {
                newName = "<html><div style='color: red; font-style: italic'>" + Lg.text("untitled") + "</div></html>";
            }
            explorerItem.label = newName;
            explorer.updateItemView(explorerItem);
        });

        bamItem.deleteButton.addActionListener((e) -> {
            int response = JOptionPane.showConfirmDialog(this,
                    Lg.html("delete_component_question", bamItem.bamItemNameField.getText()),
                    Lg.text("warning"),
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
    }

    public BamItem getBamItem(String id) {
        for (BamItem item : BAM_ITEMS) {
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void saveProjectAs() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getName().endsWith(".bam")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Fichier BaRatinAGE (.bam)";
            }

        });
        fileChooser.setDialogTitle("Sauvegarder le projet");
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fullFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            fullFilePath = fullFilePath.endsWith(".bam") ? fullFilePath : fullFilePath + ".bam";
            saveProject(fullFilePath);
        }
    }

    public void saveProject() {
        String projectPath = getProjectPath();
        if (projectPath == null) {
            saveProjectAs();
        } else {
            saveProject(projectPath);
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray jsonItems = new JSONArray();
        for (BamItem item : BAM_ITEMS) {
            jsonItems.put(item.toFullJSON());
        }
        json.put("version", 0);
        json.put("items", jsonItems);
        json.put("model", ""); // must be overriden!
        return json;
    }

    public abstract void fromJSON(JSONObject json);

    public void saveProject(String saveFilePath) {

        String mainConfigFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR, "main_config.json").toString();
        File mainConfigFile = new File(mainConfigFilePath);
        try {
            WriteFile.writeLines(mainConfigFile, new String[] { toJSON().toString(4) });
        } catch (IOException saveError) {
            System.err.println("BamProject Error: Failed to save file");
            saveError.printStackTrace();
        }

        try {

            File zipFile = new File(saveFilePath);
            FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);

            ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

            System.out.println("BamProject: Including file '" + mainConfigFile + "'...");
            ZipEntry zipEntry = new ZipEntry(mainConfigFile.getName());

            zipOutStream.putNextEntry(zipEntry);

            Files.copy(mainConfigFile.toPath(), zipOutStream);

            cleanupRegisteredFile();
            for (File file : registeredFiles) {
                System.out.println("BamProject: Including file '" + file.toString() + "'...");
                String name = file.getName();
                ZipEntry ze = new ZipEntry(name);
                zipOutStream.putNextEntry(ze);
                Files.copy(file.toPath(), zipOutStream);
            }

            zipOutStream.close();

            setProjectPath(saveFilePath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    static public BamProject loadProject(String projectFilePath) {

        // Clear Temp Directory!
        for (File file : new File(AppConfig.AC.APP_TEMP_DIR).listFiles()) {
            if (!file.isDirectory())
                file.delete();
        }

        File projectFile = new File(projectFilePath);
        if (!projectFile.exists()) {
            System.err.println("BamProject Error: Project file doesn't exist! (" + projectFilePath + ")");
            return null;
        }

        ReadWriteZip.unzip(projectFilePath, AppConfig.AC.APP_TEMP_DIR);

        try {
            BufferedReader bufReader = ReadFile
                    .createBufferedReader(Path.of(AppConfig.AC.APP_TEMP_DIR, "main_config.json").toString(),
                            true);
            String jsonString = "";
            String jsonLine;
            while ((jsonLine = bufReader.readLine()) != null) {
                jsonString += jsonLine;
            }
            JSONObject json = new JSONObject(jsonString);

            // FIXME: is this where version conversion should occur?
            int version = json.getInt("version");
            System.out.println("BamProject: file version = " + version);
            String model = json.getString("model");
            System.out.println("BamProject: model id = " + model);
            if (model.equals("baratin")) {
                BamProject project = new BaratinProject();
                project.fromJSON(json);
                return project;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
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

    private List<File> registeredFiles = new ArrayList<>();

    public void registerFile(String filePath) {
        File f = new File(filePath);
        if (registeredFileContains(f)) {
            return;
        }
        if (!f.exists()) {
            System.out.println("BamProject: Cannot register file '" + filePath + "' because it doesn't exist.");
            return;
        }
        registeredFiles.add(f);
    }

    public boolean registeredFileContains(File file) {
        for (File f : registeredFiles) {
            if (f.compareTo(file) == 0) {
                return true;
            }
        }
        return false;
    }

    private void cleanupRegisteredFile() {
        List<File> toRemove = new ArrayList<>();
        List<String> usedNames = new ArrayList<>();
        for (File file : registeredFiles) {
            if (!file.exists()) {
                System.out.println("BamProject: File '" + file.toString() + "' doesn't exist.");
                toRemove.add(file);
            }
            String name = file.getName();
            if (usedNames.stream().anyMatch(s -> s.equals(name))) {
                // necessary when creating flat zip file! No duplicated name allowed.
                System.out
                        .println("BamProject: File '" + file.toString()
                                + "' has a name already used by another registered file.");
                toRemove.add(file);
                continue;
            }
            usedNames.add(name);
        }
        for (File file : toRemove) {
            System.out.println("BamProject: Unregistering file '" + file.toString() + "'...");
            registeredFiles.remove(file);
        }
    }

}
