package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionOutput;

public class PredictionExperiment implements IPredictionExperiment {

    private ICalibratedModel calibratedModel;
    private IPredictionData predictionData;

    private String name;
    private boolean propageteParametricUncertainty;
    private boolean propagateStructuralUncertainty;

    public PredictionExperiment(String name, boolean propageteParametricUncertainty,
            boolean propagateStructuralUncertainty) {
        this.name = name;
        this.propageteParametricUncertainty = propageteParametricUncertainty;
        this.propagateStructuralUncertainty = propagateStructuralUncertainty;
    }

    @Override
    public void setCalibrationModel(ICalibratedModel cm) {
        calibratedModel = cm;
    }

    @Override
    public void setPredictionData(IPredictionData pd) {
        predictionData = pd;
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

}
