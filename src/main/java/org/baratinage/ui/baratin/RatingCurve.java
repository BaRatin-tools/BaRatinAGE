package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

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
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
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
import org.baratinage.ui.bam.RunPanel;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Calc;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.Throttler;
import org.json.JSONObject;

public class RatingCurve extends BamItem implements IPredictionMaster, ICalibratedModel, IMcmc {

    private BamItemParent hydrauConfParent;
    private BamItemParent gaugingsParent;
    private BamItemParent structErrorParent;

    private RatingCurveStageGrid ratingCurveStageGrid;
    private RunPanel runPanel;
    private RatingCurveResults resultsPanel;
    private RunConfigAndRes bamRunConfigAndRes;

    private RowColPanel outdatedPanel;

    private BamConfigRecord backup;

    public RatingCurve(String uuid, BaratinProject project) {
        super(BamItemType.RATING_CURVE, uuid, project);

        // **********************************************************
        // Hydraulic configuration
        // **********************************************************
        hydrauConfParent = new BamItemParent(
                this,
                BamItemType.HYDRAULIC_CONFIG);
        hydrauConfParent.setComparisonJSONfilter((JSONObject json) -> {
            return JSONFilter.filter(json, true, true,
                    "ui",
                    "bamRunId",
                    "jsonStringBackup",
                    "stageGridConfig",
                    "allControlOptions",
                    "controlTypeIndex",
                    "isKACmode",
                    "isLocked");
        });
        hydrauConfParent.addChangeListener((e) -> {
            HydraulicConfiguration bamItem = (HydraulicConfiguration) hydrauConfParent.getCurrentBamItem();
            runPanel.setModelDefintion(bamItem);
            runPanel.setPriors(bamItem);

            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItemType.GAUGINGS);
        gaugingsParent.setComparisonJSONfilter((JSONObject json) -> {
            return JSONFilter.filter(json, true, true,
                    "name", "headers", "filePath", "nested");
        });
        gaugingsParent.addChangeListener((e) -> {
            Gaugings bamItem = (Gaugings) gaugingsParent.getCurrentBamItem();
            runPanel.setCalibrationData(bamItem);

            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************
        // Structural error
        // **********************************************************
        structErrorParent = new BamItemParent(
                this,
                BamItemType.STRUCTURAL_ERROR);
        structErrorParent.setComparisonJSONfilter((JSONObject json) -> {
            return JSONFilter.filter(json, true, true, "isLocked");
        });
        structErrorParent.addChangeListener((e) -> {
            StructuralErrorModelBamItem bamItem = (StructuralErrorModelBamItem) structErrorParent.getCurrentBamItem();
            runPanel.setStructuralErrorModel(bamItem);

            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        // **********************************************************

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        RowColPanel mainConfigPanel = new RowColPanel();
        RowColPanel mainContentPanel = new RowColPanel(RowColPanel.AXIS.COL);

        // **********************************************************

        ratingCurveStageGrid = new RatingCurveStageGrid();
        ratingCurveStageGrid.addChangeListener((e) -> {
            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        mainConfigPanel.appendChild(hydrauConfParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(gaugingsParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(structErrorParent, 1);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(ratingCurveStageGrid, 1);

        runPanel = new RunPanel(true, false, true);
        runPanel.setPredictionExperiments(this);
        runPanel.addRunSuccessListerner((RunConfigAndRes res) -> {
            backup = save(true);
            bamRunConfigAndRes = res;
            buildPlot();
            hydrauConfParent.updateBackup();
            gaugingsParent.updateBackup();
            structErrorParent.updateBackup();

            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        resultsPanel = new RatingCurveResults();

        outdatedPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outdatedPanel.setPadding(5);
        outdatedPanel.setGap(5);
        outdatedPanel.setColWeight(0, 1);

        mainContentPanel.appendChild(outdatedPanel, 0);
        mainContentPanel.appendChild(runPanel, 0);
        mainContentPanel.appendChild(resultsPanel, 1);

        content.appendChild(mainConfigPanel, 0);
        content.appendChild(new JSeparator(), 0);
        content.appendChild(mainContentPanel, 1);

        setContent(content);

        T.updateHierarchy(this, hydrauConfParent);
        T.updateHierarchy(this, gaugingsParent);
        T.updateHierarchy(this, structErrorParent);
        T.updateHierarchy(this, ratingCurveStageGrid);
        T.updateHierarchy(this, runPanel);
        T.updateHierarchy(this, resultsPanel);
        T.updateHierarchy(this, outdatedPanel);

        T.t(runPanel, runPanel.runButton, true, "compute_posterior_rc");
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
                hc.getXtra(AppConfig.AC.BAM_WORKSPACE_ROOT),
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

        List<MsgPanel> warnings = new ArrayList<>();
        warnings.addAll(hydrauConfParent.getMessages());
        warnings.addAll(gaugingsParent.getMessages());
        warnings.addAll(structErrorParent.getMessages());

        boolean isStageGridOutOfSync = !isRatingCurveStageGridInSync();

        boolean needBamRerun = warnings.size() > 0 || isStageGridOutOfSync;

        if (isStageGridOutOfSync) {

            // FIXME: errorMsg should be a final instance variable to limit memory leak
            MsgPanel errorMsg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
            JButton cancelChangeButton = new JButton();
            cancelChangeButton.addActionListener((e) -> {
                JSONObject json = backup.jsonObject();
                JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
                ratingCurveStageGrid.fromJSON(stageGridJson);

                Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
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
        T.clear(runPanel);
        if (isStageGridOutOfSync || needBamRerun) {
            T.t(runPanel, runPanel.runButton, true, "recompute_posterior_rc");
            runPanel.runButton.setForeground(AppConfig.AC.INVALID_COLOR_FG);
        } else {
            T.t(runPanel, runPanel.runButton, true, "compute_posterior_rc");
            runPanel.runButton.setForeground(new JButton().getForeground());
        }

        // since text within warnings changes, it is necessary to
        // call Lg.updateRegisteredComponents() so changes are accounted for.
        // T.updateTranslations();
        // disabled because throws IllegalStateException
        // with SimpleNumberField class

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
            System.out.println("RatingCurve: missing 'hydrauConfig'");
        }

        if (json.has("gaugings")) {
            gaugingsParent.fromJSON(json.getJSONObject("gaugings"));
        } else {
            System.out.println("RatingCurve: missing 'gaugings'");
        }

        if (json.has("structError")) {
            structErrorParent.fromJSON(json.getJSONObject("structError"));
        } else {
            System.out.println("RatingCurve: missing 'structError'");
        }

        if (json.has("stageGridConfig")) {
            JSONObject stageGridConfigJson = json.getJSONObject("stageGridConfig");
            ratingCurveStageGrid.fromJSON(stageGridConfigJson);
        } else {
            System.out.println("RatingCurve: missing 'stageGridConfig'");
        }

        // **********************************************************
        // prior rating curve BaM results
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            bamRunConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            buildPlot();
        } else {
            System.out.println("RatingCurve: missing 'bamRunId'");
        }

        if (json.has("backup")) {
            JSONObject backupJson = json.getJSONObject("backup");
            backup = BamConfigRecord.fromJSON(backupJson);

        } else {
            System.out.println("RatingCurve: missing 'backup'");
        }

        Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
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

    private void buildPlot() {
        if (bamRunConfigAndRes == null) {
            return;
        }

        PredictionConfig[] predConfigs = bamRunConfigAndRes.getPredictionConfigs();
        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();
        CalibrationResult calibrationResults = bamRunConfigAndRes.getCalibrationResults();

        int maxpostIndex = calibrationResults.maxpostIndex;

        HashMap<String, EstimatedParameter> pars = calibrationResults.estimatedParameters;
        List<double[]> transitionStages = new ArrayList<>();
        List<EstimatedParameter> parameters = new ArrayList<>();
        for (String parName : pars.keySet()) {
            if (parName.startsWith("k_")) {
                EstimatedParameter p = pars.get(parName);
                double[] vals = p.mcmc;
                double mp = vals[maxpostIndex];
                double[] p95 = Calc.percentiles(vals, new double[] { 0.025, 0.975 });
                transitionStages.add(new double[] {
                        mp, p95[0], p95[1]
                });
            }
            parameters.add(pars.get(parName));
        }

        String outputName = predConfigs[0].outputs[0].name;

        double[] dischargeMaxpost = predResults[0].outputResults.get(outputName).spag().get(0);

        List<double[]> paramU = predResults[1].outputResults.get(outputName).env().subList(1, 3);
        List<double[]> totalU = predResults[2].outputResults.get(outputName).env().subList(1, 3);

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

        resultsPanel.updatePlot(
                predConfigs[0].inputs[0].dataColumns.get(0),
                dischargeMaxpost,
                paramU,
                totalU,
                transitionStages,
                gaugings,
                parameters);
    }

}
