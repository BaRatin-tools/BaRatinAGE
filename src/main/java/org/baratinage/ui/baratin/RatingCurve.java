package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.AppSetup;

import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.translation.T;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredictionExperiment;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.rating_curve.RatingCurveResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurveStageGrid;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.container.RowColPanel;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;

import org.json.JSONObject;

public class RatingCurve extends BamItem implements IPredictionMaster, ICalibratedModel, IMcmc {

    private static class RatingCurveSyncStatus {
        public boolean hydrauConf = false;
        public boolean gaugings = false;
        public boolean structError = false;
        public boolean ratingCurveStageGrid = false;

        public boolean isCalibrationInSync() {
            return hydrauConf && gaugings && structError;
        }

        public boolean isPredictionInSync() {
            return ratingCurveStageGrid;
        }

        public boolean isBamRunInSync() {
            return isCalibrationInSync() && isPredictionInSync();
        }
    }

    private final RatingCurveSyncStatus syncStatus;

    private final BamItemParent hydrauConfParent;
    private final BamItemParent gaugingsParent;
    private final BamItemParent structErrorParent;

    private final RatingCurveStageGrid ratingCurveStageGrid;
    public final RunBam runBam;
    private final RatingCurveResults resultsPanel;
    private final RowColPanel outdatedPanel;

    private RunConfigAndRes bamRunConfigAndRes;

    private BamConfigRecord backup;

    public RatingCurve(String uuid, BaratinProject project) {
        super(BamItemType.RATING_CURVE, uuid, project);

        syncStatus = new RatingCurveSyncStatus();

        runBam = new RunBam(true, false, true);

        // **********************************************************
        // Hydraulic configuration
        // **********************************************************
        hydrauConfParent = new BamItemParent(
                this,
                BamItemType.HYDRAULIC_CONFIG);
        hydrauConfParent.setComparisonJSONfilter(new JSONFilter(true, true,
                "ui",
                "bamRunId",
                "backup",
                "jsonStringBackup",
                "stageGridConfig",
                "allControlOptions",
                "controlTypeIndex",
                "isKACmode",
                "isLocked"));
        hydrauConfParent.addChangeListener((e) -> {
            HydraulicConfiguration bamItem = (HydraulicConfiguration) hydrauConfParent.getCurrentBamItem();
            runBam.setModelDefintion(bamItem);
            runBam.setPriors(bamItem);

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItemType.GAUGINGS);
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
        structErrorParent.setComparisonJSONfilter(new JSONFilter(true, true, "isLocked"));
        structErrorParent.addChangeListener((e) -> {
            StructuralErrorModelBamItem bamItem = (StructuralErrorModelBamItem) structErrorParent.getCurrentBamItem();
            runBam.setStructuralErrorModel(bamItem);

            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        RowColPanel mainConfigPanel = new RowColPanel();
        RowColPanel mainContentPanel = new RowColPanel(RowColPanel.AXIS.COL);

        // **********************************************************

        ratingCurveStageGrid = new RatingCurveStageGrid();
        ratingCurveStageGrid.addChangeListener((e) -> {
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        mainConfigPanel.appendChild(hydrauConfParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(gaugingsParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(structErrorParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(ratingCurveStageGrid, 1);

        runBam.setPredictionExperiments(this);
        runBam.addOnDoneAction((RunConfigAndRes res) -> {

            if (syncStatus.isCalibrationInSync()) {
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

        outdatedPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outdatedPanel.setPadding(5);
        outdatedPanel.setGap(5);
        outdatedPanel.setColWeight(0, 1);

        mainContentPanel.appendChild(outdatedPanel, 0);
        mainContentPanel.appendChild(runBam.runButton, 0, 5);
        mainContentPanel.appendChild(resultsPanel, 1);

        content.appendChild(mainConfigPanel, 0);
        content.appendChild(new JSeparator(), 0);
        content.appendChild(mainContentPanel, 1);

        setContent(content);

        T.updateHierarchy(this, hydrauConfParent);
        T.updateHierarchy(this, gaugingsParent);
        T.updateHierarchy(this, structErrorParent);
        T.updateHierarchy(this, ratingCurveStageGrid);
        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, resultsPanel);
        T.updateHierarchy(this, outdatedPanel);

        T.t(runBam, runBam.runButton, true, "compute_posterior_rc");

        initializeBamItem();
    }

    private void initializeBamItem() {
        hydrauConfParent.selectDefaultBamItem();
        gaugingsParent.selectDefaultBamItem();
        structErrorParent.selectDefaultBamItem();

        HydraulicConfiguration currentHydraulicConfig = (HydraulicConfiguration) hydrauConfParent.getCurrentBamItem();

        if (currentHydraulicConfig != null) {
            BamConfigRecord record = currentHydraulicConfig.save(false);
            if (record.jsonObject().has("stageGridConfig")) {
                ratingCurveStageGrid.fromJSON(record.jsonObject().getJSONObject("stageGridConfig"));
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
        HydraulicConfiguration hc = (HydraulicConfiguration) hydrauConfParent.getCurrentBamItem();
        Gaugings g = (Gaugings) gaugingsParent.getCurrentBamItem();
        StructuralErrorModelBamItem se = (StructuralErrorModelBamItem) structErrorParent.getCurrentBamItem();
        if (hc == null || g == null || se == null) {
            return null;
        }

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                hc.getModelId(),
                hc.getInputNames().length,
                hc.getOutputNames().length,
                hc.getParameters(),
                hc.getXtra(AppSetup.PATH_BAM_WORKSPACE_DIR),
                BamFilesHelpers.CONFIG_XTRA);

        String[] outputNames = hc.getOutputNames();
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
        JSONObject jsonBackup = backup.jsonObject();
        if (!jsonBackup.has("stageGridConfig")) {
            return false;
        }
        JSONCompareResult results = JSONCompare.compare(
                ratingCurveStageGrid.toJSON(),
                jsonBackup.getJSONObject("stageGridConfig"));
        return results.matching();
    }

    private void checkSync() {
        outdatedPanel.clear();
        T.clear(outdatedPanel);

        hydrauConfParent.updateSyncStatus();
        gaugingsParent.updateSyncStatus();
        structErrorParent.updateSyncStatus();

        syncStatus.hydrauConf = hydrauConfParent.getSyncStatus();
        syncStatus.gaugings = gaugingsParent.getSyncStatus();
        syncStatus.structError = structErrorParent.getSyncStatus();
        syncStatus.ratingCurveStageGrid = isRatingCurveStageGridInSync();

        List<MsgPanel> warnings = new ArrayList<>();
        if (!syncStatus.hydrauConf) {
            warnings.add(hydrauConfParent.getOutOfSyncMessage());
        }
        if (!syncStatus.gaugings) {
            warnings.add(gaugingsParent.getOutOfSyncMessage());
        }
        if (!syncStatus.structError) {
            warnings.add(structErrorParent.getOutOfSyncMessage());
        }

        if (!syncStatus.ratingCurveStageGrid) {

            // FIXME: errorMsg should be a final instance variable to limit memory leak
            MsgPanel errorMsg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
            JButton cancelChangeButton = new JButton();
            cancelChangeButton.addActionListener((e) -> {
                JSONObject json = backup.jsonObject();
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
        for (MsgPanel w : warnings) {
            outdatedPanel.appendChild(w);
        }

        // --------------------------------------------------------------------
        // update run bam button
        T.clear(runBam);
        if (!syncStatus.isBamRunInSync()) {
            T.t(runBam, runBam.runButton, true, "recompute_posterior_rc");
            runBam.runButton.setForeground(AppSetup.COLORS.INVALID_FG);
        } else {
            T.t(runBam, runBam.runButton, true, "compute_posterior_rc");
            runBam.runButton.setForeground(new JButton().getForeground());
        }

        fireChangeListeners();
    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {

        JSONObject json = new JSONObject();

        json.put("hydrauConfig", hydrauConfParent.toJSON());
        json.put("gaugings", gaugingsParent.toJSON());
        json.put("structError", structErrorParent.toJSON());

        // **********************************************************
        // Stage grid configuration

        JSONObject stageGridConfigJson = ratingCurveStageGrid.toJSON();

        json.put("stageGridConfig", stageGridConfigJson);

        json.put("stageGridConfig", stageGridConfigJson);

        // **********************************************************
        // BaM run
        String zipPath = null;
        if (bamRunConfigAndRes != null) {
            json.put("bamRunId", bamRunConfigAndRes.id);
            zipPath = bamRunConfigAndRes.zipRun(writeFiles);
        }

        if (backup != null) {
            json.put("backup", BamConfigRecord.toJSON(backup));
        }

        return zipPath == null ? new BamConfigRecord(json) : new BamConfigRecord(json, zipPath);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

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
            updateResults();
        } else {
            ConsoleLogger.log("missing 'bamRunId'");
        }

        if (json.has("backup")) {
            JSONObject backupJson = json.getJSONObject("backup");
            backup = BamConfigRecord.fromJSON(backupJson);

        } else {
            ConsoleLogger.log("missing 'backup'");
        }

        TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
    }

    @Override
    public IPredictionExperiment[] getPredictionExperiments() {
        PredictionExperiment[] predictionConfigs = new PredictionExperiment[3];
        CalibrationConfig calibrationConfig = getCalibrationConfig();
        PredictionInput[] predInputs = ratingCurveStageGrid.getPredictionInputs();
        predictionConfigs[0] = new PredictionExperiment(
                "maxpost",
                false,
                false,
                calibrationConfig,
                predInputs);

        predictionConfigs[1] = new PredictionExperiment(
                "parametric_uncertainty",
                true,
                false,
                calibrationConfig,
                predInputs);

        predictionConfigs[2] = new PredictionExperiment(
                "total_uncertainty",
                true,
                true,
                calibrationConfig,
                predInputs);
        return predictionConfigs;
    }

    private void updateResults() {
        // FIXME: this method needs to be reworked!
        if (bamRunConfigAndRes == null) {
            return;
        }

        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();
        CalibrationResult calibrationResults = bamRunConfigAndRes.getCalibrationResults();

        List<EstimatedParameter> parameters = calibrationResults.estimatedParameters;

        double[] dischargeMaxpost = predResults[0].outputResults.get(0).spag().get(0);

        List<double[]> paramU = predResults[1].outputResults.get(0).env().subList(1, 3);
        List<double[]> totalU = predResults[2].outputResults.get(0).env().subList(1, 3);

        CalibrationData calData = bamRunConfigAndRes.getCalibrationConfig().calibrationData;

        double[] stage = calData.inputs[0].values;
        double[] discharge = calData.outputs[0].values;
        double[] dischargeStd = calData.outputs[0].nonSysStd;
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

        resultsPanel.updateResults(
                predResults[0].predictionConfig.inputs[0].dataColumns.get(0),
                dischargeMaxpost,
                paramU,
                totalU,
                gaugings,
                parameters);
    }

}
