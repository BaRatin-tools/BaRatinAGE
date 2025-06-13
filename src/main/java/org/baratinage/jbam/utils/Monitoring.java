package org.baratinage.jbam.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.PredictionConfig;

public class Monitoring {

    private final static String MONITOR_FILE_SUFFIX = ".monitor";

    private List<MonitoringStep> monitoringSteps;

    private List<Consumer<MonitoringStep>> monitoringConsumers = new ArrayList<>();

    public Monitoring(BaM bam, String workspace) {

        monitoringSteps = new ArrayList<>();

        int mcmcSamples = bam.getCalibrationConfig().mcmcConfig.numberOfMcmcSamples();
        if (bam.getRunOptions().doMcmc) {
            monitoringSteps
                    .add(new MonitoringStep(
                            "MCMC",
                            Path.of(workspace,
                                    BamFilesHelpers.CONFIG_MCMC + Monitoring.MONITOR_FILE_SUFFIX),
                            0,
                            mcmcSamples,
                            0,
                            0));
        }

        if (bam.getRunOptions().doPrediction) {
            int cookedMcmcSamples = bam.getCalibrationConfig().mcmcCookingConfig.numberOfCookedMcmcSamples(mcmcSamples);
            for (PredictionConfig predConfig : bam.getPredictionConfigs()) {
                monitoringSteps.add(
                        new MonitoringStep(
                                "Prediction_" + predConfig.predictionConfigFileName,
                                Path.of(workspace,
                                        predConfig.predictionConfigFileName + Monitoring.MONITOR_FILE_SUFFIX),
                                0,
                                cookedMcmcSamples,
                                0,
                                0));

            }
        }
    }

    public int getNumberOfSteps() {
        return monitoringSteps.size();
    }

    public void addMonitoringConsumer(Consumer<MonitoringStep> mc) {
        monitoringConsumers.add(mc);
    }

    public void removeMonitoringConsumer(Consumer<MonitoringStep> mc) {
        monitoringConsumers.remove(mc);
    }

    private void publishToMonitoringConsumers(MonitoringStep ms) {
        for (Consumer<MonitoringStep> mc : monitoringConsumers) {
            mc.accept(ms);
        }
    }

    public void startMonitoring() throws InterruptedException {

        final int CHECK_INTERVAL = 50;
        final int MAX_DURATION = 24 * 60 * 60 * 1000; // 24h!
        final int N_MAX = MAX_DURATION / CHECK_INTERVAL;
        int k = 0;

        int currentStep = 0;
        for (MonitoringStep ms : this.monitoringSteps) {
            currentStep++;
            k = 0;
            while (k < N_MAX) {
                k++;
                Thread.sleep(CHECK_INTERVAL);
                if (Files.exists(ms.monitorFilePath)) {

                    int[] res = getProgressFromFile(ms.monitorFilePath);
                    if (res == null) {
                        System.out.println("Invalid monitoring file");
                        res = new int[] { 1, 1 };
                        continue;
                    }
                    ms.progress = res[0];
                    ms.total = res[1];

                    if (ms.progress > ms.total) {
                        // This happens for prediction with a single sample
                        ms.progress = ms.total;
                    }

                    ms.currenStep = currentStep;
                    ms.totalSteps = this.monitoringSteps.size();
                    publishToMonitoringConsumers(ms);

                    if (ms.progress >= ms.total) {
                        k = N_MAX;
                    }
                }

            }
        }

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
