package org.baratinage.jbam.utils;

import java.nio.file.Path;

public class MonitoringStep {
    public final String id;
    public final Path monitorFilePath;
    public int progress;
    public int total;
    public int currenStep;
    public int totalSteps;

    MonitoringStep(
            String id,
            Path monitorFilePath,
            int progress,
            int total,
            int currenStep,
            int totalSteps) {
        this.id = id;
        this.monitorFilePath = monitorFilePath;
        this.progress = progress;
        this.total = total;
        this.currenStep = currenStep;
        this.totalSteps = totalSteps;
    }

    public String getStepId() {
        if (id.equals("MCMC")) {
            return "mcmc_running";
        } else if (id.startsWith("Prediction")) {
            return "pred_running";
        } else if (id.equals("starting")) {
            return "conf_writing";
        } else if (id.equals("canceled")) {
            return "canceled";
        } else {
            return "done";
        }
    }

    @Override
    public String toString() {
        String s = String.format("MonitoringStep '%s' : %d/%d (%d/%d)",
                id,
                progress,
                total,
                currenStep,
                totalSteps);
        return s;
    }
}