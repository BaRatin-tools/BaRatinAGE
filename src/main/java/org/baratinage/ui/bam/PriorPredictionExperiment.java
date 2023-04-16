package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;

public class PriorPredictionExperiment implements IPriorPredictionExperiment {

    private IModelDefinition modelDefinitionProvider;
    private IPredictionData predictionDataProvider;

    private String name;
    private boolean propageteParametricUncertainty;
    private int nReplcates;

    public PriorPredictionExperiment(String name, boolean propageteParametricUncertainty, int nReplcates) {
        this.name = name;
        this.propageteParametricUncertainty = propageteParametricUncertainty;
        this.nReplcates = nReplcates;
    }

    @Override
    public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider) {
        this.modelDefinitionProvider = modelDefinitionProvider;
    }

    @Override
    public void setPredictionDataProvider(IPredictionData predictionDataProvider) {
        this.predictionDataProvider = predictionDataProvider;
    }

    @Override
    public PredictionConfig getPredictionConfig() {

        PredictionInput[] predInputs = predictionDataProvider != null
                ? predictionDataProvider.getPredictionInputs()
                : new PredictionInput[0];

        String[] outputNames = modelDefinitionProvider != null
                ? modelDefinitionProvider.getOutputNames()
                : new String[0];

        PredictionOutput[] predOutputs = new PredictionOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            predOutputs[k] = new PredictionOutput(
                    outputNames[k],
                    false,
                    true,
                    true);
        }

        PredictionConfig predExperimentConfig = new PredictionConfig(
                this.name,
                predInputs,
                predOutputs,
                new PredictionOutput[] {},
                propageteParametricUncertainty,
                true,
                nReplcates);

        return predExperimentConfig;
    }

    @Override
    public boolean isPredicted() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPredicted'");
    }

    @Override
    public PredictionResult getPredictionResult() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPredictionResult'");
    }

}
