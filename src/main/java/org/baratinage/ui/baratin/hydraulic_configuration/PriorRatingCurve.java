package org.baratinage.ui.baratin.hydraulic_configuration;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.AppSetup;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.baratin.rating_curve.RatingCurveParameters;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlot;
import org.baratinage.ui.baratin.rating_curve.RatingCurveStageGrid;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.json.JSONObject;

public class PriorRatingCurve<HCT extends IModelDefinition & IPriors> extends RowColPanel implements IPredictionMaster {

    private final RatingCurveStageGrid priorRatingCurveStageGrid;
    private final RowColPanel outOufSyncPanel;
    public final RunBam runBam;
    private final RatingCurvePlot plotPanel;

    private final HCT hydraulicConfiguration;
    private RunConfigAndRes bamRunConfigAndRes;

    private JSONObject jsonBackup;

    public PriorRatingCurve(HCT hct) {
        super(AXIS.COL);

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

        outOufSyncPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outOufSyncPanel.setPadding(5);

        plotPanel = new RatingCurvePlot();

        Dimension dimPref = new Dimension(500, 300);
        plotPanel.setPreferredSize(dimPref);
        Dimension dimMin = new Dimension(250, 150);
        plotPanel.setMinimumSize(dimMin);

        appendChild(priorRatingCurveStageGrid, 0);
        appendChild(new JSeparator(), 0);
        appendChild(outOufSyncPanel, 0);
        appendChild(runBam.runButton, 0, 5);
        appendChild(plotPanel, 1);

        T.updateHierarchy(this, priorRatingCurveStageGrid);
        T.updateHierarchy(this, plotPanel);
        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, outOufSyncPanel);
        T.t(runBam, runBam.runButton, true, "compute_prior_rc");

    }

    private void updateRunBamButton(boolean rerunNeeded) {
        if (!rerunNeeded) {
            T.t(runBam, runBam.runButton, true, "compute_prior_rc");
            runBam.runButton.setForeground(new JButton().getForeground());
        } else {
            T.t(runBam, runBam.runButton, true, "recompute_prior_rc");
            runBam.runButton.setForeground(AppSetup.COLORS.INVALID_FG);
        }
        updateUI();
    }

    public void checkSync() {
        T.clear(outOufSyncPanel);
        T.clear(runBam);
        outOufSyncPanel.clear();
        if (jsonBackup == null) {
            updateRunBamButton(false);
            return;
        }

        JSONObject jsonCurrent = new JSONObject();
        jsonCurrent.put("modelDefinition", BamConfig.getConfig((IModelDefinition) hydraulicConfiguration));
        jsonCurrent.put("priors", BamConfig.getConfig((IPriors) hydraulicConfiguration));
        jsonCurrent.put("stageGridConfig", priorRatingCurveStageGrid.toJSON());

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
            outOufSyncPanel.appendChild(mp);
        }
        if (outOfSyncMessages.size() > 0) {
            updateRunBamButton(true);
        } else {
            updateRunBamButton(false);
        }

        updateUI();

    }

    private void buildPlot() {

        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();

        CalibrationResult calResults = bamRunConfigAndRes.getCalibrationResults();
        RatingCurveParameters rcParameters = new RatingCurveParameters(calResults.estimatedParameters);

        List<double[]> activationStage = rcParameters.kacbParameters
                .stream()
                .filter(bep -> bep.shortName.startsWith("k"))
                .map(bep -> {

                    double[] u95 = bep.get95interval();
                    double mp = bep.getMaxpost();
                    return new double[] { mp, u95[0], u95[1] };
                }).collect(Collectors.toList());

        double[] stage = predResults[0].predictionConfig.inputs[0].dataColumns.get(0);
        double[] dischargeMaxpost = predResults[0].outputResults.get(0).spag().get(0);
        List<double[]> dischargeParamU = predResults[1].outputResults.get(0).env().subList(1, 3);

        plotPanel.setPriorPlot(stage, dischargeMaxpost, dischargeParamU, activationStage);
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
                        AppSetup.CONFIG.N_REPLICATES, false)),
                new PredExp(PredictionConfig.buildPriorPrediction(
                        "u",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { uParamOutput },
                        new PredictionState[] {},
                        true,
                        AppSetup.CONFIG.N_REPLICATES,
                        false)));
    }

    public BamConfig saveConfig() {
        BamConfig config = new BamConfig(0);
        config.JSON.put("stageGridConfig", priorRatingCurveStageGrid.toJSON());
        if (bamRunConfigAndRes != null) {
            config.JSON.put("bamRunId", bamRunConfigAndRes.id);
            config.FILE_PATHS.add(bamRunConfigAndRes.zipRun(true));
        }
        if (jsonBackup != null) {
            config.JSON.put("backup", jsonBackup);
        }
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
    }
}
