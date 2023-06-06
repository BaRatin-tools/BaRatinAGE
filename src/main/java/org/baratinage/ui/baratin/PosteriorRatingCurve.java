package org.baratinage.ui.baratin;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.App;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.IStructuralError;
import org.baratinage.ui.bam.PredictionExperiment;
import org.baratinage.ui.bam.RunBamPost;
import org.baratinage.ui.container.RowColPanel;

public class PosteriorRatingCurve extends RowColPanel implements ICalibratedModel, IMcmc {

    private RatingCurveStageGrid ratingCurveGrid;

    private IModelDefinition modelDefinition;
    private IPriors priors;
    private IStructuralError structuralError;
    private ICalibrationData calibrationData;
    private IPredictionExperiment[] predictionExperiments;

    private boolean isCalibrated = false;

    private CalibrationResult calibtrationResult;

    public PosteriorRatingCurve() {
        super(AXIS.COL);
        ratingCurveGrid = new RatingCurveStageGrid();
        appendChild(ratingCurveGrid, 0);
        appendChild(new JSeparator(JSeparator.VERTICAL), 0);

        JButton runBamButton = new JButton("<html>Calculer la courbe de tarage <i>a posteriori</i></html>");
        runBamButton.addActionListener((e) -> {
            computePosteriorRatingCurve();
        });
        appendChild(runBamButton, 0);
        appendChild(new JLabel("Posterior rating curve"), 1);

    }

    public void computePosteriorRatingCurve() {

        if (modelDefinition == null ||
                calibrationData == null ||
                structuralError == null ||
                priors == null) {
            System.out.println("Invalid configuration! Aborting.");
            return;
        }

        isCalibrated = false;

        PredictionExperiment[] pe = new PredictionExperiment[2];
        pe[0] = new PredictionExperiment(
                getName() + "_maxpost",
                false,
                false);
        pe[0].setCalibrationModel(this);
        pe[0].setPredictionData(ratingCurveGrid);

        pe[1] = new PredictionExperiment(
                getName() + "_uncertainty",
                true,
                true);
        pe[1].setCalibrationModel(this);
        pe[1].setPredictionData(ratingCurveGrid);

        RunBamPost runBamPost = new RunBamPost();

        runBamPost.configure(
                App.BAM_RUN_DIR,
                modelDefinition,
                priors,
                structuralError,
                calibrationData,
                pe);

        // try {
        runBamPost.run();
        // } catch (Exception e) {
        // System.err.println("An error occured while running BaM!");
        // e.printStackTrace();
        // }

        calibtrationResult = runBamPost.getCalibrationResult();
        isCalibrated = true;

        PredictionResult[] predictionResults = runBamPost.getPredictionResults();

        System.out.println("DONE");
    }

    public void setModelDefintion(IModelDefinition md) {
        modelDefinition = md;
    }

    public void setPriors(IPriors p) {
        priors = p;
    }

    public void setStructuralErrorModel(IStructuralError se) {
        structuralError = se;
    }

    public void setCalibrationData(ICalibrationData cd) {
        calibrationData = cd;
    }

    public void setPredictionExperiments(IPredictionExperiment[] pe) {
        predictionExperiments = pe;
    }

    @Override
    public McmcConfig getMcmcConfig() {
        return new McmcConfig();
    }

    @Override
    public McmcCookingConfig getMcmcCookingConfig() {
        return new McmcCookingConfig();
    }

    @Override
    public CalibrationConfig getCalibrationConfig() {
        // FIXME: is it necessary to create an instance of calibrationConfig here?
        // FIXME: There is a lot of duplicated code with RunBamPost!

        StructuralErrorModel structErrModel = structuralError.getStructuralErrorModel();

        String[] outputNames = modelDefinition.getOutputNames();
        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(outputNames[k], structErrModel);
        }

        return new CalibrationConfig(
                new Model(
                        modelDefinition.getModelId(),
                        modelDefinition.getInputNames().length,
                        modelDefinition.getOutputNames().length,
                        priors.getParameters(),
                        modelDefinition.getXtra(App.BAM_RUN_DIR)),
                modelOutputs,
                calibrationData.getCalibrationData(),
                getMcmcConfig(),
                getMcmcCookingConfig(),
                new McmcSummaryConfig(),
                new CalDataResidualConfig());
    }

    @Override
    public boolean isCalibrated() {
        return isCalibrated;
    }

    @Override
    public CalibrationResult getCalibrationResults() {
        if (!isCalibrated) {
            return null;
        }
        return calibtrationResult;
    }

}
