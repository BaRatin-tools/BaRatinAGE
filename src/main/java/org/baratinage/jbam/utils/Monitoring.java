package org.baratinage.jbam.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.PredictionConfig;

public class Monitoring {

    private final static String MONITOR_FILE_SUFFIX = ".monitor";

    private List<MonitoringStep> monitoringSteps;
    private MonitoringFollower monitoringFollower;

    @FunctionalInterface
    public interface MonitoringFollower {
        public void onUpdate(MonitoringStep monitoringStep);
    }

    public Monitoring(BaM bam, String workspace, MonitoringFollower monitoringFollower) {

        monitoringSteps = new ArrayList<>();
        this.monitoringFollower = monitoringFollower;

        int mcmcSamples = bam.getCalibrationConfig().getMcmcConfig().numberOfMcmcSamples();
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
            int cookedMcmcSamples = bam.getCalibrationConfig().getMcmcCookingConfig()
                    .numberOfCookedMcmcSamples(mcmcSamples);
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

        try {
            startMonitoring();
        } catch (InterruptedException e) {
            System.err.println("Monitoring Error: BaM monitoring interrupted!");
        }

    }

    private void startMonitoring() throws InterruptedException {

        int CHECK_INTERVAL = 10;
        int MAX_DURATION = 24 * 60 * 60 * 1000; // 24h!
        int N_MAX = MAX_DURATION / CHECK_INTERVAL;
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
                        List<double[]> res = Read.readMatrix(ms.monitorFilePath.toString(), "/", 0, 0);
                        if (res.size() == 2 && res.get(0).length == 1) {
                            ms.progress = (int) res.get(0)[0];
                            ms.total = (int) res.get(1)[0];
                        }
                        ms.currenStep = currentStep;
                        ms.totalSteps = this.monitoringSteps.size();
                        monitoringFollower.onUpdate(ms);
                        if (ms.progress >= ms.total) {
                            k = N_MAX;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }
}
