package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;

public interface IPriorPredictionExperiment {

    public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider);

    public void setPredictionDataProvider(IPredictionData predictionDataProvider);

    public PredictionConfig getPredictionConfig();

    public boolean isPredicted();

    public PredictionResult getPredictionResult();

}
