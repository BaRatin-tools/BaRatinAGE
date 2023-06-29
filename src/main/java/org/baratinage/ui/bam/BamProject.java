package org.baratinage.ui.bam;

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
import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileFilter;

import org.baratinage.App;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.ReadFile;
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.utils.WriteFile;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BamProject extends RowColPanel {

    protected BamItemList items;
    protected List<ExplorerItem> explorerItems;

    // protected RowColPanel actionBar;
    protected JSplitPane content;

    protected Explorer explorer;
    protected RowColPanel currentPanel;

    public BamProject() {
        super(AXIS.COL);

        this.items = new BamItemList();
        this.explorerItems = new ArrayList<>();

        this.content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.content.setBorder(BorderFactory.createEmptyBorder());
        this.appendChild(this.content, 1);

        this.explorer = new Explorer("Explorateur");
        this.setupExplorer();

        this.currentPanel = new RowColPanel(AXIS.COL);
        this.currentPanel.setGap(5);

        this.content.setLeftComponent(this.explorer);
        this.content.setRightComponent(this.currentPanel);
        this.content.setResizeWeight(0);

    }

    private void setupExplorer() {

        this.explorer.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = explorer.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = findBamItem(explorerItem.id);
                if (bamItem != null) {
                    this.currentPanel.clear();
                    this.currentPanel.appendChild(bamItem, 1);

                } else {
                    System.out.println("selected BamItem is null");
                    this.currentPanel.clear();
                }
                this.updateUI();
            }
        });

    }

    public void setCurrentBamItem(BamItem bamItem) {
        ExplorerItem item = findExplorerBamItem(bamItem.ID);
        if (item != null) {
            explorer.selectItem(item);
        }
    }

    public BamItemList getBamItems() {
        return this.items;
    }

    public void addItem(BamItem bamItem, ExplorerItem explorerItem) {

        Lg.registerButton(bamItem.cloneButton, "ui", "duplicate");
        Lg.registerButton(bamItem.deleteButton, "ui", "delete");

        items.add(bamItem);
        explorerItems.add(explorerItem);
        // bamItem.updateSiblings(items);

        bamItem.addPropertyChangeListener((p) -> {
            if (p.getPropertyName().equals("bamItemName")) {
                String newName = (String) p.getNewValue();
                if (newName.equals("")) {
                    newName = "<html><div style='color: red; font-style: italic'>Sansnom</div></html>";
                }
                explorerItem.label = newName;
                explorer.updateItemView(explorerItem);
            }
        });

        bamItem.addDeleteAction(e -> {
            deleteItem(bamItem, explorerItem);
        });

        this.explorer.appendItem(explorerItem);
        this.explorer.selectItem(explorerItem);

    }

    public void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        items.remove(bamItem);
        explorerItems.remove(explorerItem);
        this.explorer.removeItem(explorerItem);
        this.explorer.selectItem(explorerItem.parentItem);
    }

    public BamItem findBamItem(String id) {
        for (BamItem item : this.items) {
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

    private ExplorerItem findExplorerBamItem(String id) {
        for (ExplorerItem item : this.explorerItems) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void saveProject() {
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray jsonItems = new JSONArray();
        for (BamItem item : items) {
            jsonItems.put(item.toFullJSON());
        }
        json.put("version", 0);
        json.put("items", jsonItems);
        json.put("model", ""); // must be overriden!
        return json;
    }

    public abstract void fromJSON(JSONObject json);

    // public void fromJSON(JSONObject json) {

    // }

    public void saveProject(String saveFilePath) {

        String mainConfigFilePath = Path.of(App.TEMP_DIR, "main_config.json").toString();
        File mainConfigFile = new File(mainConfigFilePath);
        try {
            WriteFile.writeLines(mainConfigFile, new String[] { toJSON().toString(4) });
        } catch (IOException saveError) {
            System.err.println("Failed to save file");
            saveError.printStackTrace();
        }

        try {

            File zipFile = new File(saveFilePath);
            FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);

            ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

            System.out.println("File '" + mainConfigFile + "'.");
            ZipEntry zipEntry = new ZipEntry(mainConfigFile.getName());

            zipOutStream.putNextEntry(zipEntry);

            Files.copy(mainConfigFile.toPath(), zipOutStream);

            List<String> usedNames = new ArrayList<>();
            for (BamItem item : items) {
                String[] dataFileNames = item.getTempDataFileNames();

                for (String dfp : dataFileNames) {
                    File f = new File(Path.of(App.TEMP_DIR, dfp).toString());
                    System.out.println("Including file '" + f + "'...");
                    String name = f.getName();
                    if (usedNames.stream().anyMatch(s -> s.equals(name))) {
                        System.out.println("Duplicated zip entry '" + name + "' ignored!");
                        continue;
                    }
                    usedNames.add(name);
                    ZipEntry ze = new ZipEntry(name);
                    zipOutStream.putNextEntry(ze);
                    Files.copy(f.toPath(), zipOutStream);
                }
            }

            zipOutStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    static public BamProject loadProject(String projectFilePath) {

        // Clear Temp Directory!
        for (File file : new File(App.TEMP_DIR).listFiles()) {
            if (!file.isDirectory())
                file.delete();
        }

        ReadWriteZip.unzip(projectFilePath, App.TEMP_DIR);

        try {
            BufferedReader bufReader = ReadFile
                    .createBufferedReader(Path.of(App.TEMP_DIR, "main_config.json").toString(), true);
            String jsonString = "";
            String jsonLine;
            while ((jsonLine = bufReader.readLine()) != null) {
                jsonString += jsonLine;
            }
            JSONObject json = new JSONObject(jsonString);
            System.out.println(json);

            // FIXME: is this where version conversion should occur?
            int version = json.getInt("version");
            System.out.println("FILE VERSION = " + version);
            String model = json.getString("model");
            System.out.println("MODEL = " + model);
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

}
