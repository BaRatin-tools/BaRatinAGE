package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadWriteZip;
import org.baratinage.utils.fs.WriteFile;
import org.baratinage.utils.perf.Performance;
import org.baratinage.utils.perf.TasksWorker;
import org.json.JSONArray;
import org.json.JSONObject;

public class BamProjectSaver {

  private static record BamConfigItem(JSONObject json, List<String> files) {
  }

  private static BamConfigItem getBamConfigItem(BamItem item, boolean writeToFile) {
    BamConfig itemConfig = item.save(writeToFile);
    JSONObject bamItemJson = new JSONObject();
    bamItemJson.put("id", item.ID);
    bamItemJson.put("type", item.TYPE.toString());
    bamItemJson.put("name", item.bamItemNameField.getText());
    bamItemJson.put("description", item.bamItemDescriptionField.getText());
    bamItemJson.put("config", itemConfig.JSON);
    return new BamConfigItem(bamItemJson, itemConfig.FILE_PATHS);
  }

  private static BamConfig getBamConfig(BamProject project, JSONArray itemsJson, List<String> files) {
    JSONObject json = new JSONObject();
    json.put("fileVersion", 0);
    json.put("bamProjectType", project.PROJECT_TYPE.toString());
    json.put("bamItems", itemsJson);
    ExplorerItem exItem = project.EXPLORER.getLastSelectedPathComponent();
    if (exItem != null) {
      json.put("selectedItemId", exItem.id);
    }
    BamConfig projectConfig = new BamConfig(json, files);
    return projectConfig;
  }

  public static BamConfig getBamConfig(BamProject project) {
    List<String> files = new ArrayList<>();
    JSONArray bamItemsJson = new JSONArray();
    BamItemList bamItemList = project.getOrderedBamItemList();
    int n = bamItemList.size();
    for (int k = 0; k < n; k++) {
      BamConfigItem itemConfig = getBamConfigItem(bamItemList.get(k), false);
      bamItemsJson.put(k, itemConfig.json);
      for (String file : itemConfig.files) {
        files.add(file);
      }
    }
    return getBamConfig(project, bamItemsJson, files);
  }

  private static void saveProject(
      ProgressFrame progressFrame,
      BamProject project,
      String saveFilePath,
      Consumer<BamConfig> onSaved,
      Runnable onCancel,
      Runnable onError) {

    BamItemList bamItemList = project.getOrderedBamItemList();

    // Initalize loading monitoring frame
    SimpleFlowPanel p = new SimpleFlowPanel(true);
    p.setGap(5);
    JLabel lMessage = new JLabel();
    String savingMessage = T.text("saving_project");

    File saveFile = new File(saveFilePath);

    lMessage.setText("<html>" +
        "<b>" + saveFile.getName() + "</b>" + "<br>" +
        "<code>" + saveFile.getAbsolutePath() + "</code>" +
        "</html>");

    p.addChild(lMessage);

    progressFrame.openProgressFrame(
        AppSetup.MAIN_FRAME,
        p,
        savingMessage,
        0,
        bamItemList.size(),
        true);

    progressFrame.updateProgress(savingMessage, 0);
    progressFrame.clearOnCancelActions();

    TasksWorker<BamItem, Void> tasksWorker = new TasksWorker<>();

    progressFrame.addOnCancelAction(
        () -> {
          tasksWorker.cancel();
        });

    Map<BamItem, Boolean> hasError = new HashMap<>();

    List<String> files = new ArrayList<>();
    JSONArray bamItemsJson = new JSONArray();

    int n = bamItemList.size();
    for (int k = 0; k < n; k++) {
      BamItem bamItem = bamItemList.get(k);
      Integer index = k;
      tasksWorker.addTask(bamItem,
          (item) -> {
            try {
              BamConfigItem itemConfig = getBamConfigItem(item, true);
              bamItemsJson.put(index, itemConfig.json);
              for (String file : itemConfig.files) {
                files.add(file);
              }
              hasError.put(item, false);
            } catch (Exception e) {
              hasError.put(item, true);
            }
            return null;
          }, (item) -> {
            String itemName = item.bamItemNameField.getText();
            String progressMsg = T.html(
                "saving_project_component",
                T.text(item.TYPE.id), itemName);
            progressFrame.updateProgress(progressMsg, index);
          },
          null);
    }

    tasksWorker.setOnDoneAction(() -> {

      // ------------------------------------------------------------
      // saving main config file
      Performance.startTimeMonitoring("bam main config file");
      BamConfig bamConfig = getBamConfig(project, bamItemsJson, files);
      ConsoleLogger.log("saving project...");
      String mainConfigFilePath = Path.of(AppSetup.PATH_APP_TEMP_DIR,
          "main_config.json").toString();
      bamConfig.FILE_PATHS.add(mainConfigFilePath);
      JSONObject json = bamConfig.JSON;
      String mainJsonString = json.toString(4);
      File mainConfigFile = new File(mainConfigFilePath);
      try {
        WriteFile.writeLines(mainConfigFile, new String[] { mainJsonString });
      } catch (IOException saveError) {
        ConsoleLogger.error("Failed to write main config JSON file!\n" + saveError);
      }
      Performance.endTimeMonitoring("bam main config file");
      // ------------------------------------------------------------
      // zipping all files
      boolean success = ReadWriteZip.flatZip(saveFilePath, bamConfig.FILE_PATHS);
      if (success) {
        ConsoleLogger.log("project saved!");
      } else {
        ConsoleLogger.error("an error occured while saving project!");
      }

      progressFrame.done();

      boolean anyError = hasError
          .values()
          .stream()
          .anyMatch(Boolean::booleanValue);

      if (anyError) {
        onError.run();
      }

      if (!tasksWorker.wasCanceled()) {
        onSaved.accept(bamConfig);
      } else {
        onCancel.run();
      }

    });

    tasksWorker.run();

  }

  public static void saveProject(ProgressFrame progressFrame, BamProject project, boolean saveAs, Runnable onSuccess,
      Runnable onFailure) {

    Runnable _onFailure = onFailure != null ? onFailure : () -> {
    };
    Runnable _onSuccess = onSuccess != null ? onSuccess : () -> {
    };

    String projectFilePath = project.getProjectPath();
    if (saveAs || projectFilePath == null) {
      File f = CommonDialog.saveFileDialog(
          "",
          null,
          new CommonDialog.CustomFileFilter(T.text("baratinage_file"),
              "bam", "BAM"));
      if (f == null) {
        ConsoleLogger.error("saving project failed! Selected file is null.");

        return;
      }
      projectFilePath = f.getAbsolutePath();
    }

    String filePath = projectFilePath;
    BamProjectSaver.saveProject(
        progressFrame,
        project,
        projectFilePath,
        (bamConfig) -> {
          project.setLastSavedConfig();
          project.setProjectPath(filePath);
          _onSuccess.run();
        }, () -> {
          CommonDialog.errorDialog(T.text("error_saving_project"));
          _onFailure.run();
        }, () -> {
          ConsoleLogger.warn("Project saving canceled");
          _onFailure.run();
        });

  }

}
