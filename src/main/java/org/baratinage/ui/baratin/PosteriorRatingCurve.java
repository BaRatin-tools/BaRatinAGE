package org.baratinage.ui.baratin;

import java.awt.Color;
import java.util.List;

import javax.swing.JButton;
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
import org.baratinage.jbam.PredictionConfig;
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
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsPlot;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;

public class PosteriorRatingCurve extends RowColPanel implements ICalibratedModel, IMcmc {

    private RatingCurveStageGrid ratingCurveGrid;

    private IModelDefinition modelDefinition;
    private IPriors priors;
    private IStructuralError structuralError;
    private ICalibrationData calibrationData;
    private IPredictionExperiment[] predictionExperiments;

    private RowColPanel plotPanel;

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

        plotPanel = new RowColPanel(AXIS.COL);
        appendChild(plotPanel, 1);
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

        PredictionExperiment[] pe = new PredictionExperiment[4];
        pe[0] = new PredictionExperiment(
                getName() + "_maxpost",
                false,
                false);
        pe[0].setCalibrationModel(this);
        pe[0].setPredictionData(ratingCurveGrid);

        pe[1] = new PredictionExperiment(
                getName() + "_parametric_uncertainty",
                true,
                false);
        pe[1].setCalibrationModel(this);
        pe[1].setPredictionData(ratingCurveGrid);

        pe[2] = new PredictionExperiment(
                getName() + "_structural_uncertainty",
                false,
                true);
        pe[2].setCalibrationModel(this);
        pe[2].setPredictionData(ratingCurveGrid);

        pe[3] = new PredictionExperiment(
                getName() + "_total_uncertainty",
                true,
                true);
        pe[3].setCalibrationModel(this);
        pe[3].setPredictionData(ratingCurveGrid);

        RunBamPost runBamPost = new RunBamPost();

        runBamPost.configure(
                App.BAM_RUN_DIR,
                modelDefinition,
                priors,
                structuralError,
                calibrationData,
                pe);

        runBamPost.run();

        calibtrationResult = runBamPost.getCalibrationResult();
        isCalibrated = true;

        PredictionResult[] predictionResults = runBamPost.getPredictionResults();

        buildRatingCurvePlot(
                predictionResults[0].getPredictionConfig(),
                predictionResults[1],
                predictionResults[2],
                predictionResults[3],
                predictionResults[0],
                ((Gaugings) calibrationData).getGaugingDataset());
        System.out.println("DONE");
    }

    private void buildRatingCurvePlot(
            PredictionConfig predictionConfig,
            PredictionResult parametricUncertainty,
            PredictionResult structuralUncertainty,
            PredictionResult totalUncertainty,
            PredictionResult maxpost,
            GaugingsDataset gaugings) {

        double[] stage = predictionConfig.getPredictionInputs()[0].getDataColumns().get(0);
        String outputName = predictionConfig.getPredictionOutputs()[0].getName();
        double[] dischargeMaxpost = maxpost.getOutputResults().get(outputName).spag().get(0);

        List<double[]> dischargeTotalEnv = totalUncertainty.getOutputResults().get(outputName).env();
        List<double[]> dischargeStructuralEnv = structuralUncertainty.getOutputResults().get(outputName).env();
        List<double[]> dischargeParametricEnv = parametricUncertainty.getOutputResults().get(outputName).env();

        // double[] dischargeLow = dischargeParametricEnv.get(1);
        // double[] dischargeHigh = dischargeParametricEnv.get(2);

        Plot plot = new Plot("Stage [m]", "Discharge [m3/s]", true);

        PlotItem mp = new PlotLine(
                "Posterior rating curve",
                stage,
                dischargeMaxpost,
                Color.BLACK,
                5);

        PlotItem totEnv = new PlotBand(
                "Total uncertainty",
                stage,
                dischargeTotalEnv.get(1),
                dischargeTotalEnv.get(2),
                new Color(200, 200, 200, 100));

        PlotItem strEnv = new PlotBand(
                "Structural uncertainty",
                stage,
                dischargeStructuralEnv.get(1),
                dischargeStructuralEnv.get(2),
                new Color(255, 150, 150, 100));

        PlotItem parEnv = new PlotBand(
                "Parametric uncertainty",
                stage,
                dischargeParametricEnv.get(1),
                dischargeParametricEnv.get(2),
                new Color(255, 150, 255, 100));

        plot.addXYItem(mp);
        plot.addXYItem(parEnv);
        plot.addXYItem(strEnv);
        plot.addXYItem(totEnv);

        GaugingsPlot gaugingsPlot = new GaugingsPlot("", "",
                false, gaugings);

        plot.addXYItem(gaugingsPlot.getGaugingsPoints());

        PlotContainer plotContainer = new PlotContainer(plot);

        plotPanel.clear();
        plotPanel.appendChild(plotContainer);

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
