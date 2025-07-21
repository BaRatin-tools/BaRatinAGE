package org.baratinage.ui.baratin.hydraulic_configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.baratin.rating_curve.RatingCurveCalibrationResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotData;
import org.baratinage.ui.baratin.rating_curve.RatingCurveResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurveStageGrid;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.json.JSONObject;

public class PriorRatingCurve<HCT extends BamItem & IModelDefinition & IPriors> extends SimpleFlowPanel
        implements IPredictionMaster, IPlotDataProvider {

    private final RatingCurveStageGrid priorRatingCurveStageGrid;
    private final SimpleFlowPanel outOufSyncPanel;
    public final RunBam runBam;
    private final RatingCurveResults resultsPanel;

    private final HCT hydraulicConfiguration;
    private RunConfigAndRes bamRunConfigAndRes;

    private JSONObject jsonBackup;

    public PriorRatingCurve(HCT hct) {
        super(true);

        hydraulicConfiguration = hct;
        priorRatingCurveStageGrid = new RatingCurveStageGrid();
        priorRatingCurveStageGrid.addChangeListener((e) -> {
            checkSync();
        });

        runBam = new RunBam(false, true, false);
        runBam.setPredictionExperiments(this);
        runBam.addOnDoneAction((RunConfigAndRes res) -> {
            bamRunConfigAndRes = res;
            jsonBackup = new JSONObject();
            jsonBackup.put("modelDefinition", BamConfig.getConfig((IModelDefinition) hydraulicConfiguration));
            jsonBackup.put("priors", BamConfig.getConfig((IPriors) hydraulicConfiguration));
            jsonBackup.put("stageGridConfig", priorRatingCurveStageGrid.toJSON());
            buildPlot();
            checkSync();
        });
        runBam.setModelDefintion(hydraulicConfiguration);
        runBam.setPriors(hydraulicConfiguration);

        outOufSyncPanel = new SimpleFlowPanel(true);
        outOufSyncPanel.setPadding(5);

        resultsPanel = new RatingCurveResults(hct.PROJECT, true);
        Misc.setMinimumSize(resultsPanel, null, 350);

        addChild(priorRatingCurveStageGrid, false);
        addChild(new SimpleSep(), false);
        addChild(outOufSyncPanel, false);
        addChild(runBam.runButton, 0, 5);
        addChild(resultsPanel, true);

        T.updateHierarchy(this, priorRatingCurveStageGrid);
        T.updateHierarchy(this, resultsPanel);
        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, outOufSyncPanel);
        T.t(runBam, runBam.runButton, true, "compute_prior_rc");

    }

    private void updateRunBamButton(boolean rerunNeeded) {
        if (!rerunNeeded) {
            T.t(runBam, runBam.runButton, true, "compute_prior_rc");
            runBam.runButton.setForeground(null);
        } else {
            T.t(runBam, runBam.runButton, true, bamRunConfigAndRes == null ? "compute_prior_rc" : "recompute_prior_rc");
            runBam.runButton.setForeground(AppSetup.COLORS.INVALID_FG);
        }
        updateUI();
    }

    public void checkSync() {
        T.clear(outOufSyncPanel);
        T.clear(runBam);
        outOufSyncPanel.removeAll();
        if (jsonBackup == null) {
            updateRunBamButton(false);
            return;
        }
        JSONObject jsonCurrent = new JSONObject();
        try {
            jsonCurrent.put("modelDefinition", BamConfig.getConfig((IModelDefinition) hydraulicConfiguration));
            jsonCurrent.put("priors", BamConfig.getConfig((IPriors) hydraulicConfiguration));
            jsonCurrent.put("stageGridConfig", priorRatingCurveStageGrid.toJSON());
        } catch (Exception e) {
            ConsoleLogger.error(e);
            updateRunBamButton(false);
            return;
        }

        JSONObject filteredCurrentJson = jsonCurrent;
        JSONObject filteredBackupJson = jsonBackup;

        JSONCompareResult comparison = JSONCompare.compare(
                filteredBackupJson,
                filteredCurrentJson);

        if (comparison.matching()) {
            updateRunBamButton(false);
            return;
        }

        List<MsgPanel> outOfSyncMessages = new ArrayList<>();

        JSONCompareResult stageGridConfigCompRes = comparison.children().get("stageGridConfig");

        if (!stageGridConfigCompRes.matching()) {
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
            T.t(outOufSyncPanel, msg.message, true, "oos_stage_grid");
            JButton revertBackBtn = new JButton();
            T.t(outOufSyncPanel, revertBackBtn, true, "cancel_changes");
            revertBackBtn.addActionListener((e) -> {
                priorRatingCurveStageGrid.fromJSON(
                        jsonBackup.getJSONObject("stageGridConfig"));
                checkSync();
            });
            msg.addButton(revertBackBtn);
            outOfSyncMessages.add(msg);
        }

        JSONCompareResult modelDefinitionCompRes = comparison.children().get("modelDefinition");

        if (!modelDefinitionCompRes.matching()) {
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
            T.t(outOufSyncPanel, msg.message, true, "oos_control_matrix");
            outOfSyncMessages.add(msg);
        } else {
            JSONCompareResult priorsCompRes = comparison.children().get("priors");

            if (!priorsCompRes.matching()) {
                MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
                T.t(outOufSyncPanel, msg.message, true, "oos_hydraulic_controls");
                outOfSyncMessages.add(msg);

            }

        }

        for (MsgPanel mp : outOfSyncMessages) {
            outOufSyncPanel.addChild(mp, false);
        }
        if (outOfSyncMessages.size() > 0) {
            updateRunBamButton(true);
        } else {
            updateRunBamButton(false);
        }

        updateUI();

    }

    private void buildPlot() {

        RatingCurveCalibrationResults rcParameters = getRatingCurveCalibrationResults();
        RatingCurvePlotData rcPlotData = getRatingCurvePlotData();

        resultsPanel.updateResults(
                rcPlotData,
                rcParameters);
    }

    @Override
    public PredExpSet getPredExps() {

        PredictionOutput maxpostOutput = PredictionOutput.buildPredictionOutput("maxpost", "Q", false);
        PredictionOutput uParamOutput = PredictionOutput.buildPredictionOutput("uParam", "Q", false);

        PredictionInput predInput = priorRatingCurveStageGrid.getPredictionInput();
        if (predInput == null) {
            ConsoleLogger.warn("No valid rating curve stage grid.");
            return null;
        }

        return new PredExpSet(
                new PredExp(PredictionConfig.buildPriorPrediction(
                        "maxpost",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { maxpostOutput },
                        new PredictionState[] {},
                        false,
                        AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get(), false)),
                new PredExp(PredictionConfig.buildPriorPrediction(
                        "u",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { uParamOutput },
                        new PredictionState[] {},
                        true,
                        AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get(),
                        false)));
    }

    public BamConfig saveConfig(boolean writeFiles) {
        BamConfig config = new BamConfig(0);
        config.JSON.put("stageGridConfig", priorRatingCurveStageGrid.toJSON());
        if (bamRunConfigAndRes != null) {
            config.JSON.put("bamRunId", bamRunConfigAndRes.id);
            config.FILE_PATHS.add(bamRunConfigAndRes.zipRun(writeFiles));
        }
        if (jsonBackup != null) {
            config.JSON.put("backup", jsonBackup);
        }
        config.JSON.put("plotEditor", resultsPanel.ratingCurvePlot.plotEditor.toJSON());

        return config;
    }

    public void loadConfig(JSONObject json) {
        if (json.has("stageGridConfig")) {
            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            priorRatingCurveStageGrid.fromJSON(stageGridJson);
        } else {
            ConsoleLogger.log("missing 'stageGridConfig'");
        }
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            bamRunConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            BamProjectLoader.addDelayedAction(() -> {
                buildPlot();
            });
        } else {
            ConsoleLogger.log("missing 'bamRunZipFileName'");
        }

        if (json.has("backup")) {
            jsonBackup = json.getJSONObject("backup");
        } else {
            ConsoleLogger.log("missing 'backup'");
        }

        if (json.has("plotEditor")) {
            BamProjectLoader.addDelayedAction(() -> {
                resultsPanel.ratingCurvePlot.plotEditor.fromJSON(json.getJSONObject("plotEditor"));
            });
        }
    }

    private RatingCurveCalibrationResults getRatingCurveCalibrationResults() {
        CalibrationResult calResults = bamRunConfigAndRes.getCalibrationResults();
        double[] stage = bamRunConfigAndRes.getPredictionResults()[0].predictionConfig.inputs[0].dataColumns.get(0);
        RatingCurveCalibrationResults rcParameters = new RatingCurveCalibrationResults(
                calResults,
                stage[0],
                stage[stage.length - 1]);
        return rcParameters;
    }

    public RatingCurvePlotData getRatingCurvePlotData() {
        if (bamRunConfigAndRes == null) {
            return null;
        }
        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();

        RatingCurveCalibrationResults rcParameters = getRatingCurveCalibrationResults();

        List<double[]> stageTransitions = rcParameters.getPriorStageTransitions();

        double[] stage = predResults[0].predictionConfig.inputs[0].dataColumns.get(0);
        double[] dischargeMaxpost = predResults[0].outputResults.get(0).spag().get(0);
        List<double[]> dischargeParamU = predResults[1].outputResults.get(0).env().subList(1, 3);

        RatingCurvePlotData rcPlotData = new RatingCurvePlotData(
                stage,
                dischargeMaxpost,
                dischargeParamU,
                null,
                stageTransitions,
                null);

        return rcPlotData;
    }

    @Override
    public HashMap<String, PlotItem> getPlotItems() {
        RatingCurvePlotData rcPlotData = getRatingCurvePlotData();
        if (rcPlotData == null) {
            return new HashMap<>();
        }
        return rcPlotData.getPlotItems();
    }
}
