package org.baratinage.ui.baratin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.baratinage.AppSetup;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.baratin.hydrograph.HydrographPlot;
import org.baratinage.ui.baratin.hydrograph.HydrographTable;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.commons.ExtraDataset;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.translation.T;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionMaster {

    public final RunBam runBam;
    private final HydrographPlot plotPanel;
    private final HydrographTable tablePanel;
    private final SimpleFlowPanel outdatedPanel;

    private final BamItemParent ratingCurveParent;
    private final BamItemParent limnigraphParent;

    private Limnigraph currentLimnigraph;
    private RunConfigAndRes currentConfigAndRes;

    private ExtraDataset extraData;

    public Hydrograph(String uuid, BaratinProject project) {
        super(BamItemType.HYDROGRAPH, uuid, project);

        runBam = new RunBam(false, false, true);
        runBam.setPredictionExperiments(this);

        ratingCurveParent = new BamItemParent(this, BamItemType.RATING_CURVE);
        ratingCurveParent.setComparisonJSONfilter(new JSONFilter(false, false,
                "bamRunId"));
        ratingCurveParent.addChangeListener((chEvt) -> {
            RatingCurve rc = (RatingCurve) ratingCurveParent.getCurrentBamItem();
            // currentRatingCurve = rc;
            runBam.setCalibratedModel(rc);
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        limnigraphParent = new BamItemParent(this, BamItemType.LIMNIGRAPH);
        limnigraphParent.setComparisonJSONfilter(new JSONFilter(true, true,
                "name", "headers", "filePath", "nested"));
        limnigraphParent.addChangeListener((chEvt) -> {
            Limnigraph l = (Limnigraph) limnigraphParent.getCurrentBamItem();
            currentLimnigraph = l;
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        runBam.addOnDoneAction((RunConfigAndRes res) -> {
            extraData = new ExtraDataset(
                    currentLimnigraph.getDateTimeExtraData(),
                    currentLimnigraph.getMissingValuesExtraData());
            currentConfigAndRes = res;
            buildPlot();
            ratingCurveParent.updateBackup();
            limnigraphParent.updateBackup();
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        SimpleFlowPanel parentItemPanel = new SimpleFlowPanel();

        parentItemPanel.addChild(ratingCurveParent, true);
        parentItemPanel.addChild(new SimpleSep(true), false);
        parentItemPanel.addChild(limnigraphParent, true);
        parentItemPanel.addChild(new SimpleSep(true), false);

        SimpleFlowPanel content = new SimpleFlowPanel(true);

        plotPanel = new HydrographPlot();
        tablePanel = new HydrographTable();

        TabContainer tabs = new TabContainer();
        tabs.addTab("plot", TYPE.getIcon(), plotPanel);
        tabs.addTab("chart", AppSetup.ICONS.getCustomAppImageIcon("table.svg"), tablePanel);

        outdatedPanel = new SimpleFlowPanel(true);
        outdatedPanel.setPadding(2);
        outdatedPanel.setGap(2);

        content.addChild(parentItemPanel, false);
        content.addChild(new SimpleSep(), false);
        content.addChild(outdatedPanel, false);
        content.addChild(runBam.runButton, 0, 5);
        content.addChild(tabs, true);
        setContent(content);

        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, plotPanel);
        T.updateHierarchy(this, ratingCurveParent);
        T.updateHierarchy(this, limnigraphParent);
        T.t(runBam, runBam.runButton, false, "compute_qt");
        T.t(this, () -> {
            tabs.setTitleAt(0, T.text("chart"));
            tabs.setTitleAt(1, T.text("table"));
        });

        initializeBamItem();
    }

    private void initializeBamItem() {
        ratingCurveParent.selectDefaultBamItem();
        limnigraphParent.selectDefaultBamItem();
        checkSync();
    }

    private void checkSync() {

        ratingCurveParent.updateSyncStatus();
        limnigraphParent.updateSyncStatus();

        ratingCurveParent.updateValidityView();
        limnigraphParent.updateValidityView();

        List<MsgPanel> warnings = new ArrayList<>();
        warnings.add(ratingCurveParent.getMessagePanel());
        warnings.add(limnigraphParent.getMessagePanel());

        // --------------------------------------------------------------------
        // update message panel
        outdatedPanel.removeAll();

        for (MsgPanel w : warnings) {
            if (w != null) {
                outdatedPanel.addChild(w, false);
            }
        }

        // --------------------------------------------------------------------
        // update run bam button
        T.clear(runBam);

        boolean needBamRerun = currentConfigAndRes != null
                && (!ratingCurveParent.getSyncStatus() || !limnigraphParent.getSyncStatus());
        boolean configIsInvalid = !ratingCurveParent.isConfigValid() || !limnigraphParent.isConfigValid();
        T.t(runBam, runBam.runButton, false, currentConfigAndRes == null ? "compute_qt" : "recompute_qt");
        runBam.runButton.setForeground(configIsInvalid || needBamRerun ? AppSetup.COLORS.INVALID_FG : null);
    }

    @Override
    public BamConfig save(boolean writeFiles) {

        BamConfig config = new BamConfig(0);

        config.JSON.put("ratingCurve", ratingCurveParent.toJSON());
        config.JSON.put("limnigraph", limnigraphParent.toJSON());

        if (currentConfigAndRes != null) {
            config.JSON.put("bamRunId", currentConfigAndRes.id);
            String zipPath = currentConfigAndRes.zipRun(writeFiles);
            config.FILE_PATHS.add(zipPath);
        }

        if (extraData != null) {
            config.JSON.put("extraDataId", extraData.id);
            if (writeFiles) {
                String extraDataPath = extraData.writeData();
                config.FILE_PATHS.add(extraDataPath);
            }
        }

        return config;
    }

    @Override
    public void load(BamConfig config) {

        JSONObject json = config.JSON;

        if (json.has("ratingCurve")) {
            JSONObject o = json.getJSONObject("ratingCurve");
            ratingCurveParent.fromJSON(o);
        } else {
            ConsoleLogger.log("missing 'ratingCurve'");
        }
        if (json.has("limnigraph")) {
            limnigraphParent.fromJSON(json.getJSONObject("limnigraph"));
        } else {
            ConsoleLogger.log("missing 'limnigraph'");
        }

        if (json.has("extraDataId")) {
            String extraDataId = json.getString("extraDataId");
            extraData = new ExtraDataset(extraDataId);
        } else {
            ConsoleLogger.log("missing 'extraDataId'");
        }

        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            currentConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            BamProjectLoader.addDelayedAction(() -> {
                buildPlot();
            });
        } else {
            ConsoleLogger.log("missing 'bamRunId'");
        }

        TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
    }

    @Override
    public PredExpSet getPredExps() {

        PredictionInput errorFreeStage = currentLimnigraph.getErrorFreePredictionInput();
        if (errorFreeStage == null) {
            ConsoleLogger.warn("No valid limnigraph.");
            return null;
        }
        PredictionInput uncertainStage = currentLimnigraph.getUncertainPredictionInput();

        PredictionOutput maxpostOutput = PredictionOutput.buildPredictionOutput("maxpost", "Q", false);
        PredictionOutput uParamLimniOutput = PredictionOutput.buildPredictionOutput("uParamLimni", "Q", false);
        PredictionOutput uStructParamLimniOutput = PredictionOutput.buildPredictionOutput("uStructParamLimni", "Q",
                true);

        PredictionConfig maxpostPrediction = PredictionConfig.buildPosteriorPrediction(
                "maxpost",
                new PredictionInput[] { errorFreeStage },
                new PredictionOutput[] { maxpostOutput },
                new PredictionState[] {},
                false, false);

        List<PredExp> experiments = new ArrayList<>();
        experiments.add(new PredExp(maxpostPrediction));

        if (uncertainStage != null) {
            PredictionOutput uLimniOutput = PredictionOutput.buildPredictionOutput("uLimni", "Q", false);

            experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                    "uLimni",
                    new PredictionInput[] { uncertainStage },
                    new PredictionOutput[] { uLimniOutput },
                    new PredictionState[] {},
                    false, false)));
        } else {
            uncertainStage = errorFreeStage;
        }

        experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                "uLimniParam",
                new PredictionInput[] { uncertainStage },
                new PredictionOutput[] { uParamLimniOutput },
                new PredictionState[] {},
                true, false)));
        experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                "uTotal",
                new PredictionInput[] { uncertainStage },
                new PredictionOutput[] { uStructParamLimniOutput },
                new PredictionState[] {},
                true, false)));

        return new PredExpSet(experiments);
    }

    public void buildPlot() {
        if (currentConfigAndRes == null) {
            ConsoleLogger.error("No results to plot! Aborting.");
            return;
        }
        PredictionResult[] predResults = currentConfigAndRes.getPredictionResults();

        double[] dateTimeExtraData = extraData.data.get(0);
        double[] missingValueExtraData = extraData.data.get(1);
        TreeSet<Integer> mvIndices = new TreeSet<>();
        for (double value : missingValueExtraData) {
            mvIndices.add((int) value);
        }

        LocalDateTime[] dateTimeVector = DateTime.doubleToDateTimeArray(dateTimeExtraData);

        List<double[]> results = new ArrayList<>();

        double[] maxpost = predResults[0].outputResults.get(0).spag().get(0);
        int index = 1;
        List<double[]> limniU = null;
        if (predResults.length > 3) {
            limniU = predResults[index].outputResults.get(0).get95UncertaintyInterval();
            index++;
        }

        List<double[]> paramU = predResults[index].outputResults.get(0).get95UncertaintyInterval();
        index++;
        List<double[]> totalU = predResults[index].outputResults.get(0).get95UncertaintyInterval();

        results.add(maxpost);
        results.addAll(paramU);
        results.addAll(totalU);

        if (limniU != null) {
            limniU = Misc.insertMissingValues(limniU, mvIndices);
        }
        results = Misc.insertMissingValues(results, mvIndices);
        maxpost = results.get(0);
        paramU = results.subList(1, 3);
        totalU = results.subList(3, 5);

        plotPanel.updatePlot(dateTimeVector, maxpost, limniU, paramU, totalU);
        tablePanel.updateTable(dateTimeVector, maxpost, paramU, totalU);

    }

}
