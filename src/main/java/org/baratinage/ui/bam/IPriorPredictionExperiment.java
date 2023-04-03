package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;

public interface IPriorPredictionExperiment {

    public String getName();

    public PredictionConfig getPredictionConfig();

    public void setPriorsProvider(IPriors priorsProvider);

    public void setStructuralErrorProvider(IStructuralError structuralErrorProvider);

    public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider);

    public void setPredictionDataProvider(IPredictionData predictionDataProvider);

}
