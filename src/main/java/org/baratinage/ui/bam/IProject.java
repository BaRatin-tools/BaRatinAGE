package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

public class IProject {

    public List<IDataset> dataset;
    public List<IModelDefinition> modelDefinition;
    public List<IPriors> modelPriors;
    public List<ICalibrationData> calibrationData;
    public List<ICalibratedModel> calibratedModel;
    public List<IPredictionData> predictionData;
    public List<IPredictionExperiment> predictionExperiment;
    // public List<UiPredictionExperiment> priorPredictionExperiment;

    public IProject() {
        /**
         *
         * - _datasets_
         * ----- imported data (**D**)
         * ----- gaugings (**CD**)
         * ----- limnigraphs (**PD**)
         * - structural errors (**SE**)
         * - hydraulic configuration (**MD**)
         * ----- priors (**MP**) and prior rating curve (**PD**, **PPE**) and densities
         * (**\***)
         * - rating curve (**MCMC**, **CM**)
         * ----- posterior rating curve (**PD**) and posterior parameters (**\***) + as
         * many items as relevant result exploration possibilities such as comparing
         * prior and posterior parameters / rating curve, visualizing MCMC traces, etc.
         * - hydrographs (**PE** or **PPE**)
         * 
         */

        this.dataset = new ArrayList<>();
        this.modelDefinition = new ArrayList<>();
        this.modelPriors = new ArrayList<>();
        this.calibrationData = new ArrayList<>();
        this.calibratedModel = new ArrayList<>();
        this.predictionData = new ArrayList<>();
        this.predictionExperiment = new ArrayList<>();
    }

}
