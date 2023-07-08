package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;

public interface IPredictionExperiment {
    public PredictionConfig getPredictionConfig();

    public boolean isPriorPrediction(); // really necessary?

    public boolean isPredicted();

    public PredictionResult getPredictionResult();

}
