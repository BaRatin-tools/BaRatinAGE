package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ExplorerItem;
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

  public static void saveProject(BamProject project, String saveFilePath, Consumer<BamConfig> onSaved,
      Runnable onCancel,
      Runnable onError) {

    BamItemList bamItemList = project.getOrderedBamItemList();

    ProgressFrame bamProjectSavingFrame = new ProgressFrame();

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

    bamProjectSavingFrame.openProgressFrame(
        AppSetup.MAIN_FRAME,
        p,
        savingMessage,
        0,
        bamItemList.size(),
        true);

    bamProjectSavingFrame.updateProgress(savingMessage, 0);
    bamProjectSavingFrame.clearOnCancelActions();
    bamProjectSavingFrame.addOnCancelAction(
        onCancel);

    TasksWorker<Integer> tasksWorker = new TasksWorker<>();

    List<String> files = new ArrayList<>();
    JSONArray bamItemsJson = new JSONArray();

    int n = bamItemList.size();
    for (int k = 0; k < n; k++) {
      tasksWorker.addTask(k, (index) -> {
        BamItem item = bamItemList.get(index);
        BamConfigItem itemConfig = getBamConfigItem(item, true);
        bamItemsJson.put(index, itemConfig.json);
        for (String file : itemConfig.files) {
          files.add(file);
        }
      }, (index) -> {
        BamItem item = bamItemList.get(index);
        String itemName = item.bamItemNameField.getText();
        String progressMsg = T.html(
            "saving_project_component",
            T.text(item.TYPE.id), itemName);
        bamProjectSavingFrame.updateProgress(progressMsg, index);
      }, null);
    }

    tasksWorker.addTask(n, (index) -> {
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
    }, (index) -> {
      String progressMsg = T.text("saving_project_zipping");
      bamProjectSavingFrame.updateProgress(progressMsg, bamItemList.size());

    }, (index) -> {
      bamProjectSavingFrame.done();
    });

    tasksWorker.run();

  }

}
