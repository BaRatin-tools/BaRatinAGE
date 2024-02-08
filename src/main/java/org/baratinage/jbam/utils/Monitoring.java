package org.baratinage.jbam.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.PredictionConfig;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;

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

        final int CHECK_INTERVAL = 10;
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

                    try {
                        // FIXME: should use a more low level function for this task...
                        List<double[]> res = ReadFile.readMatrix(
                                ms.monitorFilePath.toString(),
                                "/",
                                0, Integer.MAX_VALUE,
                                "",
                                false, true);
                        if (res.size() == 2 && res.get(0).length == 1) {
                            ms.progress = (int) res.get(0)[0];
                            ms.total = (int) res.get(1)[0];
                            if (ms.progress > ms.total) {
                                // FIXME: this happens for prediction with a single sample
                                ms.progress = ms.total;
                            }
                        }
                        ms.currenStep = currentStep;
                        ms.totalSteps = this.monitoringSteps.size();
                        publishToMonitoringConsumers(ms);
                        if (ms.progress >= ms.total) {
                            k = N_MAX;
                        }
                    } catch (IOException e) {
                        ConsoleLogger.error(e);
                    }
                }

            }
        }

    }
}
