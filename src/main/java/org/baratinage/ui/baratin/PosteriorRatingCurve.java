package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.App;

import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.EstimatedParameter;
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
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.IStructuralError;
import org.baratinage.ui.bam.PredictionExperiment;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsPlot;

import org.baratinage.ui.container.RowColPanel;

import org.baratinage.ui.lg.LgElement;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;

import org.baratinage.utils.Calc;

public class PosteriorRatingCurve extends RowColPanel implements ICalibratedModel, IMcmc {

    public final RatingCurveStageGrid ratingCurveGrid;
    public final JButton runBamButton;
    public final RowColPanel outdatedPanel;

    private IModelDefinition modelDefinition;
    private IPriors priors;
    private IStructuralError structuralError;
    private ICalibrationData calibrationData;

    private RowColPanel plotPanel;

    private PredictionExperiment[] predictionConfigs;
    private PredictionResult[] predictionResults;

    private CalibrationResult calibtrationResult;

    private RunBam runBam;

    public PosteriorRatingCurve() {
        super(AXIS.COL);
        ratingCurveGrid = new RatingCurveStageGrid();

        appendChild(ratingCurveGrid, 0);
        appendChild(new JSeparator(JSeparator.HORIZONTAL), 0);

        runBamButton = new JButton();
        LgElement.registerButton(runBamButton, "ui", "compute_posterior_rc");
        runBamButton.setFont(runBamButton.getFont().deriveFont(Font.BOLD));
        runBamButton.addActionListener((e) -> {
            computePosteriorRatingCurve();
        });
        outdatedPanel = new RowColPanel();

        appendChild(outdatedPanel, 0, 5);
        appendChild(runBamButton, 0, 5);

        JTabbedPane resultsTabs = new JTabbedPane();

        plotPanel = new RowColPanel(AXIS.COL);

        resultsTabs.add("<html>Courbe de tarage <i>a posteriori</i>&nbsp;&nbsp;</html>", plotPanel);

        appendChild(resultsTabs, 1);

    }

    public void computePosteriorRatingCurve() {

        if (modelDefinition == null ||
                calibrationData == null ||
                structuralError == null ||
                priors == null) {
            System.out.println("Invalid configuration! Aborting.");
            return;
        }

        buildPredictionExperiments();

        runBam = new RunBam(modelDefinition, priors, structuralError, calibrationData, predictionConfigs);

        runBam.run();

        if (runBam.hasResults()) {
            fireChangeListeners();
            buildRatingCurvePlot();
        }
    }

    private void buildPredictionExperiments() {
        predictionConfigs = new PredictionExperiment[3];
        predictionConfigs[0] = new PredictionExperiment(
                "maxpost",
                false,
                false, this, ratingCurveGrid);

        predictionConfigs[1] = new PredictionExperiment(
                "parametric_uncertainty",
                true,
                false, this, ratingCurveGrid);

        predictionConfigs[2] = new PredictionExperiment(
                "total_uncertainty",
                true,
                true, this, ratingCurveGrid);
    }

    private void buildRatingCurvePlot() {

        calibtrationResult = runBam.bam.getCalibrationResults();
        predictionResults = runBam.bam.getPredictionResults();
        if (calibtrationResult == null || predictionResults == null) {
            System.err.println("ERROR: invalid BaM results!");
            return;
        }

        int maxpostIndex = calibtrationResult.getMaxPostIndex();
        HashMap<String, EstimatedParameter> pars = calibtrationResult.getEsimatedParameters();
        List<double[]> transitionStages = new ArrayList<>();
        for (String parName : pars.keySet()) {
            if (parName.startsWith("k_")) {
                EstimatedParameter p = pars.get(parName);
                double[] vals = p.getMcmc();
                double mp = vals[maxpostIndex];
                double[] p95 = Calc.percentiles(vals, new double[] { 0.025, 0.975 });
                transitionStages.add(new double[] {
                        mp, p95[0], p95[1]
                });
            }
        }

        buildRatingCurvePlot(
                predictionResults[0].getPredictionConfig(),
                predictionResults[1],
                predictionResults[2],
                predictionResults[0],
                ((Gaugings) calibrationData).getGaugingDataset(), transitionStages);
    }

    private void buildRatingCurvePlot(
            PredictionConfig predictionConfig,
            PredictionResult parametricUncertainty,
            PredictionResult totalUncertainty,
            PredictionResult maxpost,
            GaugingsDataset gaugings,
            List<double[]> transitionStages) {

        double[] stage = predictionConfig.getPredictionInputs()[0].getDataColumns().get(0);
        String outputName = predictionConfig.getPredictionOutputs()[0].getName();
        double[] dischargeMaxpost = maxpost.getOutputResults().get(outputName).spag().get(0);

        List<double[]> dischargeTotalEnv = totalUncertainty.getOutputResults().get(outputName).env();
        List<double[]> dischargeParametricEnv = parametricUncertainty.getOutputResults().get(outputName).env();

        Plot plot = new Plot("Stage [m]", "Discharge [m3/s]", true);

        PlotItem mp = new PlotLine(
                "Posterior rating curve",
                stage,
                dischargeMaxpost,
                Color.BLACK,
                5);

        PlotItem totEnv = new PlotBand(
                "Structural and parametric uncertainty",
                stage,
                dischargeTotalEnv.get(1),
                dischargeTotalEnv.get(2),
                new Color(200, 200, 200, 100));

        PlotItem parEnv = new PlotBand(
                "Parametric uncertainty",
                stage,
                dischargeParametricEnv.get(1),
                dischargeParametricEnv.get(2),
                new Color(255, 150, 255, 100));

        for (int k = 0; k < transitionStages.size(); k++) {
            double[] transitionStage = transitionStages.get(k);
            PlotInfiniteLine line = new PlotInfiniteLine("k_" + k, transitionStage[0],
                    Color.GREEN, 2);
            PlotInfiniteBand band = new PlotInfiniteBand("Hauteur de transition",
                    transitionStage[1], transitionStage[2], new Color(100, 255, 100, 100));
            plot.addXYItem(line, false);
            plot.addXYItem(band, k == 0);
        }

        plot.addXYItem(mp);
        plot.addXYItem(parEnv);
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
                        modelDefinition.getXtra(App.BAM_WORKSPACE)),
                modelOutputs,
                calibrationData.getCalibrationData(),
                getMcmcConfig(),
                getMcmcCookingConfig(),
                new McmcSummaryConfig(),
                new CalDataResidualConfig());
    }

    @Override
    public boolean isCalibrated() {
        return runBam != null && runBam.bam.getCalibrationResults() != null
                && runBam.bam.getPredictionResults() != null;
    }

    @Override
    public CalibrationResult getCalibrationResults() {
        return calibtrationResult;
    }

    public RatingCurveStageGrid getRatingCurveStageGrid() {
        return ratingCurveGrid;
    }

    public RunBam getRunBam() {
        return runBam;
    }

    public void setRunBam(String runBamId) {

        buildPredictionExperiments();

        runBam = new RunBam(runBamId, modelDefinition, priors, structuralError, calibrationData, predictionConfigs);

        runBam.unzipBamRun();

        buildRatingCurvePlot();
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }
}
