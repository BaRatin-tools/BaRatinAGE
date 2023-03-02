package bam.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import bam.BaM;
import bam.PredictionConfig;

public class Monitoring {

    private final static String MONITOR_FILE_SUFFIX = ".monitor";
    // private final BaM bam;

    public class MonitoringStep {
        public String id;
        public Path monitorFilePath;
        public int progress;
        public int total;
        public int currenStep;
        public int totalSteps;

        MonitoringStep(String id, int progress, int total, Path monitorFilePath) {
            this.id = id;
            this.progress = progress;
            this.total = total;
            this.monitorFilePath = monitorFilePath;
            this.currenStep = 1;
            this.totalSteps = 1;
        }
    }

    private List<MonitoringStep> monitoringSteps;
    private MonitoringFollower monitoringFollower;

    @FunctionalInterface
    public interface MonitoringFollower {
        public void onUpdate(MonitoringStep monitoringStep);
    }

    public Monitoring(BaM bam, String workspace, MonitoringFollower monitoringFollower) {

        // this.bam = bam;
        this.monitoringSteps = new ArrayList<>();
        this.monitoringFollower = monitoringFollower;

        // int currentStep = 1;
        int mcmcSamples = bam.getCalibrationConfig().getMcmcConfig().numberOfMcmcSamples();
        if (bam.getRunOptions().doMcmc) {
            this.monitoringSteps
                    .add(new MonitoringStep("MCMC", 0, mcmcSamples,
                            Path.of(workspace, ConfigFile.CONFIG_MCMC + Monitoring.MONITOR_FILE_SUFFIX)));
        }

        if (bam.getRunOptions().doPrediction) {
            int cookedMcmcSamples = bam.getCalibrationConfig().getMcmcCookingConfig()
                    .numberOfCookedMcmcSamples(mcmcSamples);
            for (PredictionConfig predConfig : bam.getPredictionsConfigs()) {
                this.monitoringSteps.add(new MonitoringStep("Prediction_" + predConfig.getName(), 0, cookedMcmcSamples,
                        Path.of(workspace, predConfig.getConfigFileName() + Monitoring.MONITOR_FILE_SUFFIX)));
                ;
            }
        }

        try {
            this.startMonitoring();
        } catch (InterruptedException e) {
            System.err.println("MONITORING INTERRUPTED!");
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
                // System.out.printf("-");
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
