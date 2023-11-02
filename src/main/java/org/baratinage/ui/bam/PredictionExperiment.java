package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.utils.BamFilesHelpers;

// FIXME: should be named PosteriorPredictionExperiment
// FIXME: unclear how isPredicted and getPredictionResult should be implemented...
public class PredictionExperiment implements IPredictionExperiment {

    private String name;
    private boolean propageteParametricUncertainty;
    private boolean propagateStructuralUncertainty;

    private CalibrationConfig calibrationConfig;
    private PredictionInput[] predictionInputs;

    public PredictionExperiment(String name,
            boolean propageteParametricUncertainty,
            boolean propagateStructuralUncertainty,
            CalibrationConfig calibrationConfig,
            PredictionInput... predictionInputs) {
        this.name = name;
        this.propageteParametricUncertainty = propageteParametricUncertainty;
        this.propagateStructuralUncertainty = propagateStructuralUncertainty;
        this.calibrationConfig = calibrationConfig;
        this.predictionInputs = predictionInputs;
    }

    @Override
    public PredictionConfig getPredictionConfig() {
        ModelOutput[] mo = calibrationConfig.modelOutputs;

        // FIXME: should be able to set structural error computation boolean per outputs
        PredictionOutput[] predOutputs = new PredictionOutput[mo.length];
        for (int k = 0; k < mo.length; k++) {
            predOutputs[k] = new PredictionOutput(
                    mo[k].name,
                    String.format(BamFilesHelpers.RESULTS_OUTPUT_SPAG, name, mo[k].name),
                    String.format(BamFilesHelpers.RESULTS_OUTPUT_ENV, name, mo[k].name),
                    propagateStructuralUncertainty,
                    true,
                    true);
        }

        PredictionConfig predConfig = new PredictionConfig(
                name,
                String.format(BamFilesHelpers.CONFIG_PREDICTION, name),
                predictionInputs,
                predOutputs,
                new PredictionOutput[] {},
                propageteParametricUncertainty,
                false,
                -1);
        return predConfig;
    }

    @Override
    public boolean isPriorPrediction() {
        return false;
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
