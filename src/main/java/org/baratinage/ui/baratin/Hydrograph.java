package org.baratinage.ui.baratin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredictionExperiment;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunPanel;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.RowColPanel.AXIS;
import org.baratinage.translation.T;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.Throttler;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionMaster {

    private final RunPanel runPanel;
    private final HydrographPlot plotPanel;
    private final RowColPanel outdatedPanel;

    private final BamItemParent ratingCurveParent;
    private final BamItemParent limnigraphParent;

    private RatingCurve currentRatingCurve;
    private Limnigraph currentLimnigraph;
    private RunConfigAndRes currentConfigAndRes;

    private String jsonStringBackup;

    public Hydrograph(String uuid, BaratinProject project) {
        super(BamItemType.HYDROGRAPH, uuid, project);

        runPanel = new RunPanel(false, false, true);
        runPanel.setPredictionExperiments(this);

        ratingCurveParent = new BamItemParent(this, BamItemType.RATING_CURVE);
        ratingCurveParent.setComparisonJSONfilter((JSONObject json) -> {
            return JSONFilter.filter(json, false, false, "bamRunId");
        });
        ratingCurveParent.addChangeListener((chEvt) -> {
            RatingCurve rc = (RatingCurve) ratingCurveParent.getCurrentBamItem();
            currentRatingCurve = rc;
            runPanel.setCalibratedModel(rc);
            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        limnigraphParent = new BamItemParent(this, BamItemType.LIMNIGRAPH);
        limnigraphParent.addChangeListener((chEvt) -> {
            Limnigraph l = (Limnigraph) limnigraphParent.getCurrentBamItem();
            currentLimnigraph = l;
            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        runPanel.addRunSuccessListerner((RunConfigAndRes res) -> {
            jsonStringBackup = toJSON().toString();
            currentConfigAndRes = res;
            buildPlot();
            ratingCurveParent.updateBackup();
            limnigraphParent.updateBackup();
            Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
        });

        RowColPanel parentItemPanel = new RowColPanel();

        parentItemPanel.appendChild(ratingCurveParent, 1);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        parentItemPanel.appendChild(limnigraphParent, 1);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        plotPanel = new HydrographPlot();

        outdatedPanel = new RowColPanel(AXIS.COL);
        outdatedPanel.setPadding(2);
        outdatedPanel.setGap(2);
        outdatedPanel.setColWeight(0, 1);

        content.appendChild(parentItemPanel, 0);
        content.appendChild(new JSeparator(JSeparator.HORIZONTAL), 0);
        content.appendChild(outdatedPanel, 0);
        content.appendChild(runPanel, 0);
        content.appendChild(plotPanel, 1);
        setContent(content);

        // private final RunPanel runPanel;
        // private final HydrographPlot plotPanel;
        // private final RowColPanel outdatedPanel;

        // private final BamItemParent ratingCurveParent;
        // private final BamItemParent limnigraphParent;

        T.updateHierarchy(this, runPanel);
        T.updateHierarchy(this, plotPanel);
        T.updateHierarchy(this, outdatedPanel);
        T.t(runPanel, runPanel.runButton, false, "compute_qt");
    }

    private void checkSync() {

        List<MsgPanel> warnings = new ArrayList<>();
        warnings.addAll(ratingCurveParent.getMessages());
        warnings.addAll(limnigraphParent.getMessages());

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
        T.clear(runPanel);
        if (needBamRerun) {
            T.t(runPanel, runPanel.runButton, false, "recompute_qt");
            runPanel.runButton.setForeground(AppConfig.AC.INVALID_COLOR_FG);
        } else {
            T.t(runPanel, runPanel.runButton, false, "compute_qt");
            runPanel.runButton.setForeground(new JButton().getForeground());
        }

        // FIXME: check if message below is still relevant
        // since text within warnings changes, it is necessary to
        // call Lg.updateRegisteredComponents() so changes are accounted for.
        // T.updateRegisteredObjects();

    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        json.put("ratingCurve", ratingCurveParent.toJSON());
        json.put("limnigraph", limnigraphParent.toJSON());

        if (currentConfigAndRes != null) {
            json.put("bamRunId", currentConfigAndRes.id);
            String zipPath = currentConfigAndRes.zipRun();
            registerFile(zipPath);
        }

        json.put("jsonStringBackup", jsonStringBackup);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        if (json.has("ratingCurve")) {
            JSONObject o = json.getJSONObject("ratingCurve");
            ratingCurveParent.fromJSON(o);
        } else {
            System.out.println("Hydrograph: missing 'ratingCurve'");
        }
        if (json.has("limnigraph")) {
            limnigraphParent.fromJSON(json.getJSONObject("limnigraph"));
        } else {
            System.out.println("Hydrograph: missing 'limnigraph'");
        }
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            currentConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            buildPlot();
        } else {
            System.out.println("Hydrograph: missing 'bamRunId'");
        }
        if (json.has("jsonStringBackup")) {
            jsonStringBackup = json.getString("jsonStringBackup");
        } else {
            System.out.println("Hydrograph: missing 'jsonStringBackup'");
        }
        Throttler.throttle(ID, AppConfig.AC.THROTTLED_DELAY_MS, this::checkSync);
    }

    @Override
    public BamItem clone(String uuid) {
        Hydrograph cloned = new Hydrograph(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

    @Override
    public IPredictionExperiment[] getPredictionExperiments() {
        if (currentLimnigraph == null || currentRatingCurve == null) {
            return null;
        }

        CalibrationConfig calibrationConfig = currentRatingCurve.getCalibrationConfig();
        PredictionInput[] predInputs = currentLimnigraph.getPredictionInputs();

        PredictionInput maxpostInput = new PredictionInput(
                "maxpost_pred_input",
                predInputs[0].dataColumns,
                predInputs[0].extraData);

        PredictionInput uncertainInput = maxpostInput;
        PredictionExperiment limniU = null;
        if (predInputs.length > 1) {
            uncertainInput = new PredictionInput(
                    "uncertain_pred_input",
                    predInputs[1].dataColumns);

            limniU = new PredictionExperiment(
                    "limniU",
                    false,
                    false,
                    calibrationConfig,
                    uncertainInput);
        }

        PredictionExperiment maxpost = new PredictionExperiment(
                "maxpost",
                false,
                false,
                calibrationConfig,
                maxpostInput);

        PredictionExperiment paramU = new PredictionExperiment(
                "paramU",
                true,
                false,
                calibrationConfig,
                uncertainInput);
        PredictionExperiment totalU = new PredictionExperiment(
                "totalU",
                true,
                true,
                calibrationConfig,
                uncertainInput);

        if (limniU == null) {
            return new PredictionExperiment[] {
                    maxpost, paramU, totalU
            };
        } else {
            return new PredictionExperiment[] {
                    maxpost, limniU, paramU, totalU
            };
        }

    }

    public void buildPlot() {
        if (currentConfigAndRes == null) {
            System.err.println("Hydrograph Error: No results to plot! Aborting.");
            return;
        }
        PredictionResult[] predResults = currentConfigAndRes.getPredictionResults();

        double[] dateTimeVectorAsDouble = predResults[0].predictionConfig.inputs[0].extraData.get(0);
        LocalDateTime[] dateTimeVector = DateTime.doubleToDateTimeVector(dateTimeVectorAsDouble);

        String outName = "Output_1";
        double[] maxpost = predResults[0].outputResults.get(outName).spag().get(0);
        int index = 1;
        List<double[]> limniU = null;
        if (predResults.length > 3) {
            limniU = predResults[index].outputResults.get(outName).get95UncertaintyInterval();
            index++;
        }

        List<double[]> paramU = predResults[index].outputResults.get(outName).get95UncertaintyInterval();
        index++;
        List<double[]> totalU = predResults[index].outputResults.get(outName).get95UncertaintyInterval();

        plotPanel.updatePlot(dateTimeVector, maxpost, limniU, paramU, totalU);

    }

}
