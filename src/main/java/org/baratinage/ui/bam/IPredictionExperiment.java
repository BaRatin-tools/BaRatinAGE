package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;

public interface IPredictionExperiment {
    public void setCalibrationModel(ICalibratedModel cm);

    public void setPredictionData(IPredictionData pd);

    public PredictionConfig getPredictionConfig();
    // add public boolean isPredicted()?
    // add public PredictionResult getPredictionResults();
    // add public void runBaM()?

}
