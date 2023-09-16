package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.utils.BamFilesHelpers;

// FIXME: should be named PosteriorPredictionExperiment
// FIXME: unclear how isPredicted and getPredictionResult should be implemented...
public class PredictionExperiment implements IPredictionExperiment {

    private ICalibratedModel calibratedModel;
    private IPredictionData predictionData;

    private String name;
    private boolean propageteParametricUncertainty;
    private boolean propagateStructuralUncertainty;

    public PredictionExperiment(String name,
            boolean propageteParametricUncertainty,
            boolean propagateStructuralUncertainty,
            ICalibratedModel calibratedModel,
            IPredictionData predictionData) {
        this.name = name;
        this.propageteParametricUncertainty = propageteParametricUncertainty;
        this.propagateStructuralUncertainty = propagateStructuralUncertainty;
        this.calibratedModel = calibratedModel;
        this.predictionData = predictionData;
    }

    @Override
    public PredictionConfig getPredictionConfig() {
        if (calibratedModel == null || predictionData == null) {
            System.err.println("PredictionExperiment Error: Invalid prediction config! Returning null.");
            return null;
        }

        CalibrationConfig cc = calibratedModel.getCalibrationConfig();
        ModelOutput[] mo = cc.modelOutputs;

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
                predictionData.getPredictionInputs(),
                predOutputs,
                new PredictionOutput[] {},
                propageteParametricUncertainty,
                true,
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
