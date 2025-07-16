package org.baratinage.ui.bam.run;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionResult.PredictionOutputResult;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadWriteZip;

/**
 * handles running BaM
 */
public class BamRun extends BaM {

  public static record BamRunProgress(
      LocalDateTime timestamp,
      int currentStep,
      int totalSteps,
      float stepProgress,
      float totalProgress) {
  }

  public final String id;
  public final Path workspace;
  private boolean isRunning = false;

  public BamRun(String id, Path workspace, BaM bam) {
    super(
        bam.getCalibrationConfig(),
        bam.getPredictionConfigs(),
        bam.getRunOptions(),
        bam.getCalibrationResults(),
        bam.getPredictionResults());
    this.id = id;
    this.workspace = workspace;
  }

  public void executeSync(
      Consumer<BamRunProgress> onProgress,
      Consumer<String> onConsoleLog,
      Consumer<Exception> onError) {
    isRunning = true;
    try {
      BamMonitoring monitoring = new BamMonitoring(this);
      monitoring.startMonitoring((p) -> {
        onProgress.accept(p);
      });
      run(workspace.toString(), onConsoleLog, false);
      updateResults();
    } catch (Exception e) {
      ConsoleLogger.error(e);
      onError.accept(e);
    }
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  // public void executeAsync() // FIXME: to implement

  public boolean hasResults() {
    return getCalibrationResults() != null
        && runOptions.doPrediction ? getPredictionResults() != null : true;
  }

  // ****************************************************************
  // methods that handle saving/loading runs
  private void updateResults() {

    if (runOptions.doMcmc) {
      calibrationResult = new CalibrationResult(
          workspace.toString(),
          calibrationConfig,
          runOptions);
    }

    if (runOptions.doPrediction) {
      int n = predictionConfigs.length;
      predictionResults = new PredictionResult[n];
      for (int k = 0; k < n; k++) {
        predictionResults[k] = new PredictionResult(workspace.toString(), predictionConfigs[k]);
      }
    }

  }

  public String zipRun(boolean writeToFile) {
    if (!workspace.toFile().exists()) {
      toFiles(workspace.toString());
    }
    String zipName = id + ".zip";
    Path zipPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, zipName);
    if (writeToFile) {
      // each run being unique, if the zip file exist, there is no need
      // to recreate it; no modification could have occured
      List<String> filesToZip = new ArrayList<>();
      for (File f : workspace.toFile().listFiles()) {
        filesToZip.add(f.getName());
      }
      List<String> filesToIgnore = new ArrayList<>();
      if (predictionResults != null) {
        for (PredictionResult p : predictionResults) {
          for (int k = 0; k < p.outputResults.size(); k++) {
            PredictionOutputResult r = p.outputResults.get(k);
            if (r.spag() != null && r.spag().size() > 1) {
              String s = p.predictionConfig.outputs[k].spagFileName;
              filesToIgnore.add(s);
              if (filesToZip.contains(s)) {
                filesToZip.remove(s);
              }
            }
          }
        }
      }
      String baseDir = workspace.toString();
      String[] filesToZipFullPath = filesToZip
          .stream()
          .map(f -> Path.of(baseDir, f).toString())
          .toArray(String[]::new);
      ReadWriteZip.flatZip(zipPath.toString(), filesToZipFullPath);
    }
    return zipPath.toString();
  }

  public BamRun createCopy(String id) {
    return new BamRun(id, this.workspace, this);
  }

  // ****************************************************************
  // static constructors

  public static BamRun buildFromWorkspace(String id, Path workspacePath) {
    File mainConfigFile = Path.of(workspacePath.toString(), BamFilesHelpers.CONFIG_BAM).toFile();
    BaM bam = BaM.buildFromWorkspace(mainConfigFile.getAbsolutePath(), workspacePath.toString());
    return new BamRun(id, workspacePath, bam);
  }

  public static BamRun buildFromTempZipArchive(String id) {
    String zipName = id + ".zip";
    Path zipPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, zipName);
    Path workspacePath = Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, id);
    ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());
    return buildFromWorkspace(id, workspacePath);
  }

}
