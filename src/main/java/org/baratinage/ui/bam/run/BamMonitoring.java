package org.baratinage.ui.bam.run;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.run.BamRun.BamRunProgress;

public class BamMonitoring {

  private final static String MONITOR_FILE_SUFFIX = ".monitor";

  private final BamRun bamRun;

  private record BamMonitoringStep(String id, Path file, int total) {

  }

  private final List<BamMonitoringStep> monitoringSteps = new ArrayList<>();

  private boolean isMonitoring = false;

  public BamMonitoring(BamRun bamRun) {

    this.bamRun = bamRun;

    int mcmcSamples = bamRun.getCalibrationConfig().mcmcConfig.numberOfMcmcSamples();
    int cookedMcmcSamples = bamRun.getCalibrationConfig().mcmcCookingConfig.numberOfCookedMcmcSamples(mcmcSamples);

    if (bamRun.getRunOptions().doMcmc) {
      monitoringSteps.add(
          new BamMonitoringStep("mcmc",
              Path.of(
                  bamRun.workspace.toString(),
                  BamFilesHelpers.CONFIG_MCMC + BamMonitoring.MONITOR_FILE_SUFFIX),
              mcmcSamples));
    }
    if (bamRun.getRunOptions().doPrediction) {
      for (PredictionConfig predConfig : bamRun.getPredictionConfigs()) {
        String configFileName = predConfig.predictionConfigFileName;
        monitoringSteps.add(
            new BamMonitoringStep("pred_" + configFileName,
                Path.of(
                    bamRun.workspace.toString(),
                    configFileName + BamMonitoring.MONITOR_FILE_SUFFIX),
                cookedMcmcSamples));
      }
    }
  }

  public void startMonitoring(Consumer<BamRunProgress> onProgress) {

    isMonitoring = true;

    final int CHECK_INTERVAL = 50;
    final int MAX_DURATION = 30 * 1000; // 30s
    final int N_MAX = MAX_DURATION / CHECK_INTERVAL;

    SwingWorker<Void, Void> worker = new SwingWorker<>() {

      @Override
      protected Void doInBackground() throws Exception {
        float stepProgressSize = 1f / (float) monitoringSteps.size();
        for (int k = 0; k < monitoringSteps.size(); k++) {
          BamMonitoringStep step = monitoringSteps.get(k);
          int ITER = 0;
          boolean stepDone = false;
          while (!stepDone && isMonitoring && ITER < N_MAX) {
            ITER++;
            Thread.sleep(CHECK_INTERVAL);

            if (!bamRun.isRunning()) {
              isMonitoring = false;
            }

            if (!Files.exists(step.file())) {
              System.out.println(String.format("missing file '%s'", step.file()));
              continue;
            }

            int[] progress = getProgressFromFile(step.file());

            if (progress == null) {
              System.out.println(String.format("invalid format in file '%s'", step.file()));
              continue;
            }

            if (progress[0] >= progress[1]) {
              stepDone = true;
            }

            float p = ((float) progress[0]) / ((float) progress[1]);
            float o = (((float) k + p)) * stepProgressSize;

            onProgress.accept(new BamRunProgress(
                LocalDateTime.now(),
                k, monitoringSteps.size(),
                p, o));

          }
        }
        return null;
      }

    };

    worker.execute();
  }

  private static int[] getProgressFromFile(Path filePath) {
    try {
      String line = Files.readString(filePath).trim();
      String[] parts = line.split("/");

      if (parts.length != 2) {
        return null;
      }
      return new int[] {
          Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1])
      };
    } catch (Exception e) {
      return null;
    }
  }

}
