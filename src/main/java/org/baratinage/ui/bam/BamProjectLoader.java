package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.ReadWriteZip;
import org.baratinage.utils.perf.Performance;
import org.baratinage.utils.perf.TasksWorker;
import org.json.JSONArray;
import org.json.JSONObject;

public class BamProjectLoader {

  static public void loadProject(
      String projectFilePath,
      Consumer<BamProject> onLoaded,
      Runnable onError,
      Runnable onCancel) {

    File projectFile = new File(projectFilePath);

    // checking that file exists

    if (!projectFile.exists()) {
      ConsoleLogger.error("Project file doesn't exist! (%s)".formatted(projectFilePath));
      onError.run();
      return;
    }

    // clearing temp directory
    AppSetup.clearTempDir();

    // unzipping project file
    try {
      ReadWriteZip.unzip(projectFilePath, AppSetup.PATH_APP_TEMP_DIR);
    } catch (Exception e) {
      ConsoleLogger.error(e);
      onError.run();
      return;
    }

    // reading main config file
    JSONObject json;
    try {
      String jsonContent = ReadFile
          .readTextFile(Path.of(AppSetup.PATH_APP_TEMP_DIR, "main_config.json").toString());
      json = new JSONObject(jsonContent);

    } catch (IOException e) {
      ConsoleLogger.error(e);
      onError.run();
      return;
    }

    // getting BaM items json array
    JSONArray bamItemsJson = json.getJSONArray("bamItems");
    int n = bamItemsJson.length();

    // initializing loading progress frame

    ProgressFrame bamProjectLoadingFrame = new ProgressFrame();
    SimpleFlowPanel p = new SimpleFlowPanel(true);
    p.setGap(5);
    JLabel lMessage = new JLabel();
    String loadingMessage = T.text("loading_project");
    lMessage.setText("<html>" +
        "<b>" + projectFile.getName() + "</b>" + "<br>" +
        "<code>" + projectFile.getAbsolutePath() + "</code>" +
        "</html>");

    p.addChild(lMessage);
    bamProjectLoadingFrame.openProgressFrame(
        AppSetup.MAIN_FRAME,
        p,
        loadingMessage,
        0,
        n,
        true);
    bamProjectLoadingFrame.updateProgress(loadingMessage, 0);
    bamProjectLoadingFrame.clearOnCancelActions();

    BamProjectType projectType = BamProjectType.valueOf(json.getString("bamProjectType"));
    BamProject bamProject;
    if (projectType == BamProjectType.BARATIN) {
      bamProject = new BaratinProject();
    } else {
      return;
    }

    TasksWorker<Integer, BamItem> tasksWorker = new TasksWorker<>();

    bamProjectLoadingFrame.addOnCancelAction(
        () -> {
          tasksWorker.cancel();
        });

    Map<BamItem, Boolean> hasError = new HashMap<>();

    for (int k = 0; k < n; k++) {

      JSONObject bamItemJson = bamItemsJson.getJSONObject(k);

      BamItemType itemType = BamItemType.valueOf(bamItemJson.getString("type"));
      String id = bamItemJson.getString("id");
      BamItem item = bamProject.addBamItem(itemType, id);

      tasksWorker.addTask(
          k,
          (index) -> {

            item.bamItemNameField.setText(bamItemJson.getString("name"));
            item.bamItemDescriptionField.setText(bamItemJson.getString("description"));

            BamConfig config = new BamConfig(bamItemJson.getJSONObject("config"));

            ConsoleLogger.log("Loading item " + item);

            String loadingString = String.format("loading %s", item.TYPE.toString());
            Performance.startTimeMonitoring(loadingString);
            try {
              item.load(config);
            } catch (Exception e) {
              ConsoleLogger.error(e);
              e.printStackTrace();
              hasError.put(item, true);
            }
            hasError.put(item, false);
            return item;

          },
          (index) -> {
            String itemName = item.bamItemNameField.getText();
            String progressMsg = T.html(
                "loading_project_component",
                T.text(item.TYPE.id), itemName);
            bamProjectLoadingFrame.updateProgress(progressMsg, index);
          },
          null);
    }

    tasksWorker.setOnDoneAction(() -> {

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

      bamProjectLoadingFrame.done();

      boolean anyError = hasError
          .values()
          .stream()
          .anyMatch(Boolean::booleanValue);

      if (anyError) {
        onError.run();
      }

      if (!tasksWorker.wasCanceled()) {
        onLoaded.accept(bamProject);
      } else {
        onCancel.run();
      }
    });

    tasksWorker.run();

  }
}
