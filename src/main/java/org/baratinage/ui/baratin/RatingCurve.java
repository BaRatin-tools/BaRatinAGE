package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.AppSetup;

import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.translation.T;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.rating_curve.RatingCurveCalibrationResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotData;
import org.baratinage.ui.baratin.rating_curve.RatingCurveResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurveStageGrid;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;

import org.json.JSONObject;

public class RatingCurve extends BamItem
        implements IPredictionMaster, ICalibratedModel, IMcmc, IPlotDataProvider {

    private final BamItemParent hydrauConfParent;
    private final BamItemParent gaugingsParent;
    private final BamItemParent structErrorParent;

    private final RatingCurveStageGrid ratingCurveStageGrid;
    public final RunBam runBam;
    private final RatingCurveResults resultsPanel;
    private final SimpleFlowPanel outdatedPanel;

    private RunConfigAndRes bamRunConfigAndRes;

    private BamConfig backup;

    public RatingCurve(String uuid, BaratinProject project) {
        super(BamItemType.RATING_CURVE, uuid, project);

        runBam = new RunBam(true, false, true);

        // **********************************************************
        // Hydraulic configuration
        // **********************************************************
        hydrauConfParent = new BamItemParent(
                this,
                BamItemType.HYDRAULIC_CONFIG, BamItemType.HYDRAULIC_CONFIG_BAC, BamItemType.HYDRAULIC_CONFIG_QFH);

        String[] hydraulicConfigFilterKeys = new String[] {
                "ui", "bamRunId", "backup", "jsonStringBackup", "priorRatingCurve",
                "stageGridConfig", "allControlOptions", "controlTypeIndex", "isKACmode",
                "isLocked", "isReversed", "description", "autoInitialValue" };
        hydrauConfParent.setComparisonJSONfilter(
                BamItemType.HYDRAULIC_CONFIG, new JSONFilter(true, true, hydraulicConfigFilterKeys));
        hydrauConfParent.setComparisonJSONfilter(
                BamItemType.HYDRAULIC_CONFIG_BAC, new JSONFilter(true, true, hydraulicConfigFilterKeys));

        hydrauConfParent.setComparisonJSONfilter(
                BamItemType.HYDRAULIC_CONFIG_QFH, new JSONFilter(true, true,
                        "eqConfigsAndPriors"));

        hydrauConfParent.addChangeListener((e) -> {
            BamItem bamItem = hydrauConfParent.getCurrentBamItem();
            runBam.setModelDefintion((IModelDefinition) bamItem);
            runBam.setPriors((IPriors) bamItem);

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItemType.GAUGINGS);
        gaugingsParent.setCanBeEmpty(true);
        gaugingsParent.setComparisonJSONfilter(new JSONFilter(true, true,
                "name", "headers", "filePath", "nested"));
        gaugingsParent.addChangeListener((e) -> {
            Gaugings bamItem = (Gaugings) gaugingsParent.getCurrentBamItem();
            runBam.setCalibrationData(bamItem);

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************
        // Structural error
        // **********************************************************
        structErrorParent = new BamItemParent(
                this,
                BamItemType.STRUCTURAL_ERROR);
        structErrorParent.setComparisonJSONfilter(new JSONFilter(true, true, "isLocked", "autoInitialValue"));
        structErrorParent.addChangeListener((e) -> {
            StructuralErrorModelBamItem bamItem = (StructuralErrorModelBamItem) structErrorParent.getCurrentBamItem();
            runBam.setStructuralErrorModel(bamItem);

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************

        SimpleFlowPanel content = new SimpleFlowPanel(true);
        SimpleFlowPanel mainConfigPanel = new SimpleFlowPanel(false);
        SimpleFlowPanel mainContentPanel = new SimpleFlowPanel(true);

        // **********************************************************

        ratingCurveStageGrid = new RatingCurveStageGrid();
        ratingCurveStageGrid.addChangeListener((e) -> {
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        mainConfigPanel.addChild(hydrauConfParent, true);
        mainConfigPanel.addChild(new SimpleSep(true), false);
        mainConfigPanel.addChild(gaugingsParent, true);
        mainConfigPanel.addChild(new SimpleSep(true), false);
        mainConfigPanel.addChild(structErrorParent, true);
        mainConfigPanel.addChild(new SimpleSep(true), false);
        mainConfigPanel.addChild(ratingCurveStageGrid, true);

        runBam.setPredictionExperiments(this);
        runBam.addOnDoneAction((RunConfigAndRes res) -> {

            if (hydrauConfParent.getSyncStatus()
                    && gaugingsParent.getSyncStatus()
                    && structErrorParent.getSyncStatus()) {
                // in this case we make the new result use the previous result id
                // this fixes out of sync issues with child component (hydrographs)
                // when a rating curve is re-computed without any relevant change in the
                // configuration of rc e.g. if only the stage grid config has changed...
                // FIXME: this isn't ideal... Different runs should not share ids
                if (bamRunConfigAndRes != null) {
                    res = res.createCopy(bamRunConfigAndRes.id);
                }
            }

            backup = save(true);
            bamRunConfigAndRes = res;
            updateResults();
            hydrauConfParent.updateBackup();
            gaugingsParent.updateBackup();
            structErrorParent.updateBackup();

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        resultsPanel = new RatingCurveResults(PROJECT);
        outdatedPanel = new SimpleFlowPanel(true);
        outdatedPanel.setPadding(0);
        outdatedPanel.setGap(5);

        mainContentPanel.addChild(outdatedPanel, false);
        mainContentPanel.addChild(runBam.runButton, 0, 5);
        mainContentPanel.addChild(resultsPanel, true);

        content.addChild(mainConfigPanel, false);
        content.addChild(new SimpleSep(), false);
        content.addChild(mainContentPanel, true);

        setContent(content);

        T.updateHierarchy(this, hydrauConfParent);
        T.updateHierarchy(this, gaugingsParent);
        T.updateHierarchy(this, structErrorParent);
        T.updateHierarchy(this, ratingCurveStageGrid);
        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, resultsPanel);

        T.t(runBam, runBam.runButton, true, "compute_posterior_rc");

        initializeBamItem();
    }

    private void initializeBamItem() {
        hydrauConfParent.selectDefaultBamItem();
        gaugingsParent.selectDefaultBamItem();
        structErrorParent.selectDefaultBamItem();

        BamItem currentHydraulicConfig = hydrauConfParent.getCurrentBamItem();

        if (currentHydraulicConfig != null) {
            BamConfig config = currentHydraulicConfig.save(false);
            if (config.JSON.has("stageGridConfig")) {
                ratingCurveStageGrid.fromJSON(config.JSON.getJSONObject("stageGridConfig"));
            }
        }

        checkSync();
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
        BamItem hc = hydrauConfParent.getCurrentBamItem();
        if (hc == null || !(hc instanceof IModelDefinition) || !(hc instanceof IPriors)) {
            return null;
        }
        IModelDefinition modelDef = (IModelDefinition) hc;
        IPriors priors = (IPriors) hc;
        Gaugings g = (Gaugings) gaugingsParent.getCurrentBamItem();
        StructuralErrorModelBamItem se = (StructuralErrorModelBamItem) structErrorParent.getCurrentBamItem();
        if (hc == null || g == null || se == null) {
            return null;
        }

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                modelDef.getModelId(),
                modelDef.getInputNames().length,
                modelDef.getOutputNames().length,
                priors.getParameters(),
                modelDef.getXtra(AppSetup.PATH_BAM_WORKSPACE_DIR),
                BamFilesHelpers.CONFIG_XTRA);

        String[] outputNames = modelDef.getOutputNames();
        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        StructuralErrorModel[] structErrModels = se.getStructuralErrorModels();
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(k, structErrModels[k]);

        }

        return new CalibrationConfig(
                model,
                modelOutputs,
                g.getCalibrationData(),
                getMcmcConfig(),
                getMcmcCookingConfig(),
                new McmcSummaryConfig(),
                new CalDataResidualConfig());
    }

    @Override
    public boolean isCalibrated() {
        return bamRunConfigAndRes != null;
    }

    @Override
    public CalibrationResult getCalibrationResults() {
        return bamRunConfigAndRes == null ? null : bamRunConfigAndRes.getCalibrationResults();
    }

    private boolean isRatingCurveStageGridInSync() {
        if (backup == null) {
            return true;
        }
        JSONObject jsonBackup = backup.JSON;
        if (!jsonBackup.has("stageGridConfig")) {
            return false;
        }
        JSONCompareResult results = JSONCompare.compare(
                ratingCurveStageGrid.toJSON(),
                jsonBackup.getJSONObject("stageGridConfig"));
        return results.matching();
    }

    private void checkSync() {
        outdatedPanel.setPadding(0);
        outdatedPanel.removeAll();

        T.clear(outdatedPanel);

        hydrauConfParent.updateSyncStatus();
        gaugingsParent.updateSyncStatus();
        structErrorParent.updateSyncStatus();

        hydrauConfParent.updateValidityView();
        gaugingsParent.updateValidityView();
        structErrorParent.updateValidityView();

        boolean rcGridInSync = isRatingCurveStageGridInSync();
        boolean rcGridValid = ratingCurveStageGrid.isValueValid();

        List<MsgPanel> warnings = new ArrayList<>();
        warnings.add(hydrauConfParent.getMessagePanel());
        warnings.add(gaugingsParent.getMessagePanel());
        warnings.add(structErrorParent.getMessagePanel());

        if (!rcGridInSync) {
            // FIXME: errorMsg should be a final instance variable to limit memory leak
            MsgPanel errorMsg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
            JButton cancelChangeButton = new JButton();
            cancelChangeButton.addActionListener((e) -> {
                JSONObject json = backup.JSON;
                JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
                ratingCurveStageGrid.fromJSON(stageGridJson);
                TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
            });
            errorMsg.addButton(cancelChangeButton);
            T.t(outdatedPanel, cancelChangeButton, false, "cancel_changes");
            T.t(outdatedPanel, errorMsg.message, false, "oos_stage_grid");
            warnings.add(errorMsg);
        }

        // --------------------------------------------------------------------
        // update message panel
        boolean hasWarning = false;
        for (MsgPanel w : warnings) {
            if (w != null) {
                outdatedPanel.addChild(w, false);
                hasWarning = true;
            }
        }
        if (hasWarning) {
            outdatedPanel.setPadding(5);
        }

        // --------------------------------------------------------------------
        // update run bam button
        T.clear(runBam);

        boolean needBamRerun = bamRunConfigAndRes != null && (!hydrauConfParent.getSyncStatus()
                || !gaugingsParent.getSyncStatus()
                || !structErrorParent.getSyncStatus()
                || !rcGridInSync);
        boolean configIsInvalid = !hydrauConfParent.isConfigValid()
                || !gaugingsParent.isConfigValid()
                || !structErrorParent.isConfigValid()
                || !rcGridValid;

        T.t(runBam, runBam.runButton, true,
                bamRunConfigAndRes != null ? "recompute_posterior_rc" : "compute_posterior_rc");
        runBam.runButton.setForeground(configIsInvalid | needBamRerun ? AppSetup.COLORS.INVALID_FG : null);

        fireChangeListeners();
    }

    @Override
    public BamConfig save(boolean writeFiles) {

        BamConfig config = new BamConfig(0);

        config.JSON.put("hydrauConfig", hydrauConfParent.toJSON());
        config.JSON.put("gaugings", gaugingsParent.toJSON());
        config.JSON.put("structError", structErrorParent.toJSON());

        // **********************************************************
        // Stage grid configuration

        JSONObject stageGridConfigJson = ratingCurveStageGrid.toJSON();
        config.JSON.put("stageGridConfig", stageGridConfigJson);

        // **********************************************************
        // BaM run
        if (bamRunConfigAndRes != null) {
            config.JSON.put("bamRunId", bamRunConfigAndRes.id);
            String zipPath = bamRunConfigAndRes.zipRun(writeFiles);
            config.FILE_PATHS.add(zipPath);
        }

        if (backup != null) {
            config.JSON.put("backup", backup.JSON);
        }

        return config;
    }

    @Override
    public void load(BamConfig config) {

        JSONObject json = config.JSON;

        if (json.has("hydrauConfig")) {
            hydrauConfParent.fromJSON(json.getJSONObject("hydrauConfig"));
        } else {
            ConsoleLogger.log("missing 'hydrauConfig'");
        }

        if (json.has("gaugings")) {
            gaugingsParent.fromJSON(json.getJSONObject("gaugings"));
        } else {
            ConsoleLogger.log("missing 'gaugings'");
        }

        if (json.has("structError")) {
            structErrorParent.fromJSON(json.getJSONObject("structError"));
        } else {
            ConsoleLogger.log("missing 'structError'");
        }

        if (json.has("stageGridConfig")) {
            JSONObject stageGridConfigJson = json.getJSONObject("stageGridConfig");
            ratingCurveStageGrid.fromJSON(stageGridConfigJson);
        } else {
            ConsoleLogger.log("missing 'stageGridConfig'");
        }

        // **********************************************************
        // rating curve BaM results
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            bamRunConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            BamProjectLoader.addDelayedAction(() -> {
                updateResults();
            });
        } else {
            ConsoleLogger.log("missing 'bamRunId'");
        }

        if (json.has("backup")) {
            JSONObject backupJson = json.getJSONObject("backup");
            backup = new BamConfig(backupJson);
            if (backup.VERSION == -1 && backupJson.has("jsonObject")) {
                backup = new BamConfig(backupJson.getJSONObject("jsonObject"));
            }
        } else {
            ConsoleLogger.log("missing 'backup'");
        }

        TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
    }

    @Override
    public PredExpSet getPredExps() {

        PredictionInput predInput = ratingCurveStageGrid.getPredictionInput();
        if (predInput == null) {
            ConsoleLogger.warn("No valid rating curve stage grid.");
            return null;
        }

        PredictionOutput maxpostOutput = PredictionOutput.buildPredictionOutput("maxpost", "Q", false);
        PredictionOutput uParamOutput = PredictionOutput.buildPredictionOutput("uParam", "Q", false);
        PredictionOutput uTotalOutput = PredictionOutput.buildPredictionOutput("uTotal", "Q", true);

        return new PredExpSet(
                new PredExp(PredictionConfig.buildPosteriorPrediction(
                        "maxpost",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { maxpostOutput },
                        new PredictionState[] {},
                        false, false)),
                new PredExp(PredictionConfig.buildPosteriorPrediction(
                        "uParam",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { uParamOutput },
                        new PredictionState[] {},
                        true, false)),
                new PredExp(PredictionConfig.buildPosteriorPrediction(
                        "uTotal",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { uTotalOutput },
                        new PredictionState[] {},
                        true, false)));
    }

    private void updateResults() {

        if (bamRunConfigAndRes == null) {
            return;
        }

        // **********************************************************
        // Raw results
        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();
        CalibrationResult calibrationResults = bamRunConfigAndRes.getCalibrationResults();
        CalibrationConfig calibrationConfig = bamRunConfigAndRes.getCalibrationConfig();

        // **********************************************************
        // Gauging dataset
        double[] stage = calibrationConfig.calibrationData.inputs[0].values;
        double[] discharge = calibrationConfig.calibrationData.outputs[0].values;
        double[] dischargeStd = calibrationConfig.calibrationData.outputs[0].nonSysStd;
        int n = stage.length;
        double[] dischargeMin = new double[n];
        double[] dischargeMax = new double[n];

        for (int k = 0; k < n; k++) {
            double dischargeU = dischargeStd[k] * 2;
            dischargeMin[k] = discharge[k] - dischargeU;
            dischargeMax[k] = discharge[k] + dischargeU;
        }

        List<double[]> gaugings = new ArrayList<>(); // 4 items: h, q, qmin, qmax
        gaugings.add(stage);
        gaugings.add(discharge);
        gaugings.add(dischargeMin);
        gaugings.add(dischargeMax);

        // **********************************************************
        // Parameters
        double[] rc_stage = predResults[0].predictionConfig.inputs[0].dataColumns.get(0);
        RatingCurveCalibrationResults ratingCurveParameterSet = new RatingCurveCalibrationResults(
                calibrationResults,
                rc_stage[0], rc_stage[rc_stage.length - 1]);

        // **********************************************************
        // Rating Curve Plot Data
        RatingCurvePlotData ratingCurvePlotData = new RatingCurvePlotData(
                rc_stage, // stage
                predResults[0].outputResults.get(0).spag().get(0), // discharge
                predResults[1].outputResults.get(0).get95UncertaintyInterval(), // parametric uncertainty
                predResults[2].outputResults.get(0).get95UncertaintyInterval(), // total uncertainty
                ratingCurveParameterSet.getStageTransitions(), // stage transition
                gaugings // gaugings
        );
        resultsPanel.updateResults(ratingCurvePlotData, ratingCurveParameterSet);
    }

    public RatingCurvePlotData getRatingCurvePlotData() {
        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();

        CalibrationResult calibrationResults = bamRunConfigAndRes.getCalibrationResults();

        double[] rc_stage = bamRunConfigAndRes.getPredictionResults()[0].predictionConfig.inputs[0].dataColumns.get(0);
        RatingCurveCalibrationResults ratingCurveParameterSet = new RatingCurveCalibrationResults(
                calibrationResults,
                rc_stage[0],
                rc_stage[rc_stage.length - 1]);

        CalibrationConfig calibrationConfig = bamRunConfigAndRes.getCalibrationConfig();
        double[] stage = calibrationConfig.calibrationData.inputs[0].values;
        double[] discharge = calibrationConfig.calibrationData.outputs[0].values;
        double[] dischargeStd = calibrationConfig.calibrationData.outputs[0].nonSysStd;
        int n = stage.length;
        double[] dischargeMin = new double[n];
        double[] dischargeMax = new double[n];

        for (int k = 0; k < n; k++) {
            double dischargeU = dischargeStd[k] * 2;
            dischargeMin[k] = discharge[k] - dischargeU;
            dischargeMax[k] = discharge[k] + dischargeU;
        }

        List<double[]> gaugings = new ArrayList<>(); // 4 items: h, q, qmin, qmax
        gaugings.add(stage);
        gaugings.add(discharge);
        gaugings.add(dischargeMin);
        gaugings.add(dischargeMax);

        RatingCurvePlotData ratingCurvePlotData = new RatingCurvePlotData(
                predResults[0].predictionConfig.inputs[0].dataColumns.get(0), // stage
                predResults[0].outputResults.get(0).spag().get(0), // discharge
                predResults[1].outputResults.get(0).get95UncertaintyInterval(), // parametric uncertainty
                predResults[2].outputResults.get(0).get95UncertaintyInterval(), // total uncertainty
                ratingCurveParameterSet.getStageTransitions(), // stage transition
                gaugings // gaugings
        );

        return ratingCurvePlotData;
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
