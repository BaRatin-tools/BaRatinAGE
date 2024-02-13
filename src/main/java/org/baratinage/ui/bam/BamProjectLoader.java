package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.ReadWriteZip;
import org.baratinage.utils.perf.Performance;
import org.json.JSONArray;
import org.json.JSONObject;

public class BamProjectLoader {

    static private boolean bamProjectLoadingCanceled;
    static private Runnable doAfterBamItemsLoaded = () -> {
    };
    static final private ProgressFrame bamProjectLoadingFrame = new ProgressFrame();
    static final private List<BamItem> bamProjectBamItemsToLoad = new ArrayList<>();
    static final private List<BamConfig> bamProjectBamItemsToLoadConfig = new ArrayList<>();
    static private int bamProjectLoadingProgress = -1;

    private static void load(JSONObject json, File sourceFile, Consumer<BamProject> onLoaded) {

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
                AppSetup.MAIN_FRAME,
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
                bamProjectBamItemsToLoadConfig.add(new BamConfig(bamItemJson.getJSONObject("config")));
            }
        });

        // set loading of BamItems as next task to perform on EDT (eventDispatchThread)
        SwingUtilities.invokeLater(BamProjectLoader::loadNextBamItem); // invokeLater loop

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

            Performance.endTimeMonitoring("loading bam items");
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

        BamConfig config = bamProjectBamItemsToLoadConfig.get(bamProjectLoadingProgress);
        BamItem item = bamProjectBamItemsToLoad.get(bamProjectLoadingProgress);

        ConsoleLogger.log("Loading item " + item);

        String itemName = item.bamItemNameField.getText();
        String progressMsg = T.html(
                "loading_project_component",
                T.text(item.TYPE.id), itemName);
        bamProjectLoadingFrame.updateProgress(progressMsg, bamProjectLoadingProgress);

        Performance.startTimeMonitoring(item.TYPE.toString());
        item.load(config);
        Performance.endTimeMonitoring(item.TYPE.toString());
        bamProjectLoadingFrame.updateProgress(progressMsg, bamProjectLoadingProgress + 1);

        bamProjectLoadingProgress++;
        SwingUtilities.invokeLater(BamProjectLoader::loadNextBamItem);
    }

    private static List<Runnable> delayedActions = new ArrayList<>();
    private static boolean isInLoad = false;

    private static void runDelayedActions() {

        for (Runnable action : delayedActions) {
            SwingUtilities.invokeLater(action);
        }
        delayedActions.clear();
    }

    public static void addDelayedAction(Runnable action) {
        if (isInLoad) {
            delayedActions.add(action);
        } else {
            action.run();
        }
    }

    static public void loadProject(String projectFilePath, Consumer<BamProject> onLoaded, Runnable onError) {

        isInLoad = true;
        ConsoleLogger.addShowFilter("Performance");
        ConsoleLogger.addShowFilter("BamProjectLoader");
        ConsoleLogger.addShowFilter("DensityPlotGrid");

        Performance.startTimeMonitoring("clearing temp directory");

        AppSetup.clearTempDir();

        Performance.endTimeMonitoring("clearing temp directory");

        Performance.startTimeMonitoring("unzipping project file");

        File projectFile = new File(projectFilePath);

        if (!projectFile.exists()) {
            ConsoleLogger.error("Project file doesn't exist! (" +
                    projectFilePath + ")");
            onError.run();
            return;
        }
        try {
            ReadWriteZip.unzip(projectFilePath, AppSetup.PATH_APP_TEMP_DIR);
        } catch (Exception e) {
            ConsoleLogger.error(e);
            onError.run();
            return;
        }

        Performance.endTimeMonitoring("unzipping project file");

        Performance.startTimeMonitoring("reading main JSON config file");

        try {

            String jsonContent = ReadFile
                    .readTextFile(Path.of(AppSetup.PATH_APP_TEMP_DIR, "main_config.json").toString());
            JSONObject json = new JSONObject(jsonContent);

            Performance.endTimeMonitoring("reading main JSON config file");

            Performance.startTimeMonitoring("loading bam items");

            load(json, projectFile, (bamProject) -> {
                ConsoleLogger.clearShowFilters();
                isInLoad = false;
                runDelayedActions();
                onLoaded.accept(bamProject);

            });
        } catch (IOException e) {
            ConsoleLogger.error(e);
            onError.run();
            return;
        }
        return;
    }

}
