package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;

public interface IPriorPredictionExperiments {

    public String getName();

    public PredictionConfig[] getPredictionConfigs();

    public void setPriorsProvider(IPriors priorsProvider);

    public void setStructuralErrorProvider(IStructuralError structuralErrorProvider);

    public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider);

    public void setPredictionDataProvider(IPredictionData predictionDataProvider);

    public boolean isPredicted();

    public PredictionResult[] getPredictionResults();

    public void runBaM();

}
