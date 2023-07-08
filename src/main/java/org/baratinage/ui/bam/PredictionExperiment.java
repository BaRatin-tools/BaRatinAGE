package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;

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
            System.err.println("Invalid prediction config! Returning null.");
            return null;
        }

        CalibrationConfig cc = calibratedModel.getCalibrationConfig();
        ModelOutput[] mo = cc.getModelOutputs();

        // FIXME: should be able to set structural error computation boolean per outputs
        PredictionOutput[] predOutputs = new PredictionOutput[mo.length];
        for (int k = 0; k < mo.length; k++) {
            predOutputs[k] = new PredictionOutput(
                    mo[k].getName(),
                    propagateStructuralUncertainty,
                    true,
                    true);
        }

        PredictionConfig predConfig = new PredictionConfig(
                name,
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
