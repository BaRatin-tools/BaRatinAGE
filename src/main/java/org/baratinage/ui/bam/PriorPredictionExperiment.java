package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.utils.BamFilesHelpers;

public class PriorPredictionExperiment implements IPredictionExperiment {

    private final IModelDefinition modelDefinition;
    private final IPredictionData predictionData;
    private final String name;
    private final boolean propageteParametricUncertainty;
    private final int nReplcates;

    public PriorPredictionExperiment(String name, boolean propageteParametricUncertainty, int nReplcates,
            IModelDefinition modelDefinition, IPredictionData predictionData) {
        this.name = name;
        this.propageteParametricUncertainty = propageteParametricUncertainty;
        this.nReplcates = nReplcates;
        this.modelDefinition = modelDefinition;
        this.predictionData = predictionData;
    }

    @Override
    public PredictionConfig getPredictionConfig() {

        PredictionInput[] predInputs = predictionData != null
                ? predictionData.getPredictionInputs()
                : new PredictionInput[0];

        String[] outputNames = modelDefinition != null
                ? modelDefinition.getOutputNames()
                : new String[0];

        PredictionOutput[] predOutputs = new PredictionOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            predOutputs[k] = new PredictionOutput(
                    outputNames[k],
                    String.format(BamFilesHelpers.RESULTS_OUTPUT_SPAG, name, outputNames[k]),
                    String.format(BamFilesHelpers.RESULTS_OUTPUT_ENV, name, outputNames[k]),
                    false,
                    true,
                    true);
        }

        PredictionConfig predExperimentConfig = new PredictionConfig(
                name,
                String.format(BamFilesHelpers.CONFIG_PREDICTION, name),
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

    @Override
    public boolean isPriorPrediction() {
        return true;
    }

}
