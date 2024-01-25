package org.baratinage.ui.baratin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.AppSetup;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.container.RowColPanel.AXIS;
import org.baratinage.translation.T;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionMaster {

    public final RunBam runBam;
    private final HydrographPlot plotPanel;
    private final DataTable tablePanel;
    private final RowColPanel outdatedPanel;

    private final BamItemParent ratingCurveParent;
    private final BamItemParent limnigraphParent;

    // private RatingCurve currentRatingCurve;
    private Limnigraph currentLimnigraph;
    private RunConfigAndRes currentConfigAndRes;

    private BamConfigRecord backup;

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
            backup = save(true);
            currentConfigAndRes = res;
            buildPlot();
            ratingCurveParent.updateBackup();
            limnigraphParent.updateBackup();
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
        });

        RowColPanel parentItemPanel = new RowColPanel();

        parentItemPanel.appendChild(ratingCurveParent, 1);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        parentItemPanel.appendChild(limnigraphParent, 1);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        plotPanel = new HydrographPlot();
        tablePanel = new DataTable();

        TabContainer tabs = new TabContainer();
        tabs.addTab("plot", TYPE.getIcon(), plotPanel);
        tabs.addTab("chart", AppSetup.ICONS.getCustomAppImageIcon("table.svg"), tablePanel);

        outdatedPanel = new RowColPanel(AXIS.COL);
        outdatedPanel.setPadding(2);
        outdatedPanel.setGap(2);
        outdatedPanel.setColWeight(0, 1);

        content.appendChild(parentItemPanel, 0);
        content.appendChild(new JSeparator(JSeparator.HORIZONTAL), 0);
        content.appendChild(outdatedPanel, 0);
        content.appendChild(runBam.runButton, 0, 5);
        content.appendChild(tabs, 1);
        setContent(content);

        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, plotPanel);
        T.updateHierarchy(this, outdatedPanel);
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

        List<MsgPanel> warnings = new ArrayList<>();

        ratingCurveParent.updateSyncStatus();
        limnigraphParent.updateSyncStatus();
        if (!ratingCurveParent.getSyncStatus()) {
            warnings.add(ratingCurveParent.getOutOfSyncMessage());
        }
        if (!limnigraphParent.getSyncStatus()) {
            warnings.add(limnigraphParent.getOutOfSyncMessage());
        }

        boolean needBamRerun = warnings.size() > 0;

        // --------------------------------------------------------------------
        // update message panel
        T.clear(outdatedPanel);
        outdatedPanel.clear();

        for (MsgPanel w : warnings) {
            outdatedPanel.appendChild(w);
        }
        // --------------------------------------------------------------------
        // update run bam button
        T.clear(runBam);
        if (needBamRerun) {
            T.t(runBam, runBam.runButton, false, "recompute_qt");
            runBam.runButton.setForeground(AppSetup.COLORS.INVALID_FG);
        } else {
            T.t(runBam, runBam.runButton, false, "compute_qt");
            runBam.runButton.setForeground(new JButton().getForeground());
        }

        // FIXME: check if message below is still relevant
        // since text within warnings changes, it is necessary to
        // call Lg.updateRegisteredComponents() so changes are accounted for.
        // T.updateRegisteredObjects();

    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {

        JSONObject json = new JSONObject();

        json.put("ratingCurve", ratingCurveParent.toJSON());
        json.put("limnigraph", limnigraphParent.toJSON());

        String zipPath = null;
        if (currentConfigAndRes != null) {
            json.put("bamRunId", currentConfigAndRes.id);
            zipPath = currentConfigAndRes.zipRun(writeFiles);
        }

        if (backup != null) {
            json.put("backup", BamConfigRecord.toJSON(backup));
        }

        return zipPath == null ? new BamConfigRecord(json) : new BamConfigRecord(json, zipPath);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

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

        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            currentConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            BamProjectLoader.addDelayedAction(() -> {
                buildPlot();
            });
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
    public PredExpSet getPredExps() {

        PredictionInput errorFreeStage = currentLimnigraph.getErrorFreePredictionInput();
        if (errorFreeStage == null) {
            ConsoleLogger.warn("No valid limnigraph.");
            return null;
        }
        PredictionInput uncertainStage = currentLimnigraph.getUncertainPredictionInput();

        List<double[]> extraData = currentLimnigraph.getDateTimeExtraData();

        PredictionOutput maxpostOutput = PredictionOutput.buildPredictionOutput("maxpost", "Q", false);
        PredictionOutput uParamLimniOutput = PredictionOutput.buildPredictionOutput("uParamLimni", "Q", false);
        PredictionOutput uStructParamLimniOutput = PredictionOutput.buildPredictionOutput("uStructParamLimni", "Q",
                true);

        List<PredExp> experiments = new ArrayList<>();
        experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                String.format(BamFilesHelpers.CONFIG_PREDICTION, "maxpost"),
                new PredictionInput[] { errorFreeStage },
                new PredictionOutput[] { maxpostOutput },
                new PredictionState[] {},
                false, false)));

        if (uncertainStage != null) {
            PredictionOutput uLimniOutput = PredictionOutput.buildPredictionOutput("uLimni", "Q", false);

            experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                    String.format(BamFilesHelpers.CONFIG_PREDICTION, "uLimni"),
                    new PredictionInput[] { uncertainStage },
                    new PredictionOutput[] { uLimniOutput },
                    new PredictionState[] {},
                    false, false)));
        } else {
            uncertainStage = errorFreeStage;
        }

        experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                String.format(BamFilesHelpers.CONFIG_PREDICTION, "uLimniParam"),
                new PredictionInput[] { uncertainStage },
                new PredictionOutput[] { uParamLimniOutput },
                new PredictionState[] {},
                true, false)));
        experiments.add(new PredExp(PredictionConfig.buildPosteriorPrediction(
                String.format(BamFilesHelpers.CONFIG_PREDICTION, "uTotal"),
                new PredictionInput[] { uncertainStage },
                new PredictionOutput[] { uStructParamLimniOutput },
                new PredictionState[] {},
                true, false)));

        return new PredExpSet(extraData, experiments);
    }

    public void buildPlot() {
        if (currentConfigAndRes == null) {
            ConsoleLogger.error("No results to plot! Aborting.");
            return;
        }
        PredictionResult[] predResults = currentConfigAndRes.getPredictionResults();

        double[] dateTimeVectorAsDouble = predResults[0].predictionConfig.inputs[0].extraData.get(0);
        LocalDateTime[] dateTimeVector = DateTime.doubleToDateTimeVector(dateTimeVectorAsDouble);

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

        plotPanel.updatePlot(dateTimeVector, maxpost, limniU, paramU, totalU);

        tablePanel.clearColumns();
        tablePanel.addColumn(dateTimeVector);
        tablePanel.addColumn(maxpost);
        tablePanel.addColumn(paramU.get(0));
        tablePanel.addColumn(paramU.get(1));
        tablePanel.addColumn(totalU.get(0));
        tablePanel.addColumn(totalU.get(1));
        tablePanel.updateData();

        tablePanel.setHeaderWidth(200);
        tablePanel.setHeader(0, "Time [yyyy-MM-dd hh:mm:ss]");
        tablePanel.setHeader(1, "Q_maxpost [m3/s]");
        tablePanel.setHeader(2, "Q_param_low [[m3/s]");
        tablePanel.setHeader(3, "Q_param_high [m3/s]");
        tablePanel.setHeader(4, "Q_total_low [m3/s]");
        tablePanel.setHeader(5, "Q_total_high [m3/s]");
        tablePanel.updateHeader();
    }

}
