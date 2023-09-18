package org.baratinage.ui.baratin;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JSeparator;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.PredictionExperiment;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.DateTime;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionMaster {

    private final RunPanel runPanel;
    private final HydrographPlot plotPanel;

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

        ratingCurveParent = new BamItemParent(this, BamItemType.RATING_CURVE, "jsonStringBackup");
        ratingCurveParent.addChangeListener((chEvt) -> {
            RatingCurve rc = (RatingCurve) ratingCurveParent.getCurrentBamItem();
            currentRatingCurve = rc;
            runPanel.setCalibratedModel(rc);
        });

        limnigraphParent = new BamItemParent(this, BamItemType.LIMNIGRAPH);
        limnigraphParent.addChangeListener((chEvt) -> {
            Limnigraph l = (Limnigraph) limnigraphParent.getCurrentBamItem();
            currentLimnigraph = l;
        });

        runPanel.addRunSuccessListerner((RunConfigAndRes res) -> {
            jsonStringBackup = toJSON().toString();
            currentConfigAndRes = res;
            buildPlot();
        });

        RowColPanel parentItemPanel = new RowColPanel();

        parentItemPanel.appendChild(ratingCurveParent);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        parentItemPanel.appendChild(limnigraphParent);
        parentItemPanel.appendChild(new JSeparator(JSeparator.VERTICAL));

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        plotPanel = new HydrographPlot();
        content.appendChild(parentItemPanel, 0);
        content.appendChild(new JSeparator(JSeparator.HORIZONTAL), 0);
        content.appendChild(runPanel, 0);
        content.appendChild(plotPanel, 1);
        setContent(content);
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
            ratingCurveParent.fromJSON(json.getJSONObject("ratingCurve"));
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
                predInputs[0].dataColumns.subList(0, 1),
                predInputs[0].extraData.subList(0, 1));

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
                predInputs);
        PredictionExperiment totalU = new PredictionExperiment(
                "totalU",
                true,
                true,
                calibrationConfig,
                predInputs);

        return new PredictionExperiment[] {
                maxpost, paramU, totalU
        };
    }

    public void buildPlot() {
        System.out.println("BUILDING PLOT");
        if (currentConfigAndRes == null) {
            System.err.println("Hydrograph Error: No results to plot! Aborting.");
            return;
        }
        PredictionResult[] predResults = currentConfigAndRes.getPredictionResults();
        System.out.println(predResults);

        double[] dateTimeVectorAsDouble = predResults[0].predictionConfig.inputs[0].extraData.get(0);
        LocalDateTime[] dateTimeVector = DateTime.doubleToDateTimeVector(dateTimeVectorAsDouble);

        String outName = "Output_1";
        double[] maxpost = predResults[0].outputResults.get(outName).spag().get(0);

        List<double[]> paramU = predResults[1].outputResults.get(outName).get95UncertaintyInterval();
        List<double[]> totalU = predResults[2].outputResults.get(outName).get95UncertaintyInterval();

        plotPanel.updatePlot(dateTimeVector, maxpost, paramU, totalU);

    }

}
