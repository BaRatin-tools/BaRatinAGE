package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.Model;
import org.baratinage.ui.bam.run.BamRun;
import org.baratinage.ui.baratin.rating_shifts_happens.BamSegmentation;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShiftDetectionIteration {

    public final String ID;

    public final Integer nSegmentMax;
    public final Integer nObsMin;
    public final BamRatingCurveRun ratingCurveEstimation;

    private final List<ShiftDetectionIteration> children;

    private final HashMap<Integer, BamSegmentation> segmentations;

    private BamSegmentation bestSegmentation = null;
    private List<BamSegmentation> allBestSegmentations = null;
    private BamSegmentation currentSegmentationEstimation = null;

    public ShiftDetectionIteration(
            double[] time,
            double[] stage,
            double[] stageStd,
            double[] discharge,
            double[] dischargeStd,
            Model ratingCurveModel,
            int nSegmentMax,
            int nObsMin) {

        this.ID = buildId(time);

        this.nSegmentMax = nSegmentMax;
        this.nObsMin = nObsMin;

        ratingCurveEstimation = new BamRatingCurveRun(
                ID,
                time,
                stage,
                stageStd,
                discharge,
                dischargeStd,
                ratingCurveModel);
        children = new ArrayList<>();
        segmentations = new HashMap<>();
    }

    private ShiftDetectionIteration(
            String id,
            int nSegmentMax,
            int nObsMin,
            BamRatingCurveRun ratingCurveEstimation,
            HashMap<Integer, BamSegmentation> segmentations,
            List<ShiftDetectionIteration> children) {

        this.ID = id;
        this.nSegmentMax = nSegmentMax;
        this.nObsMin = nObsMin;

        this.ratingCurveEstimation = ratingCurveEstimation;
        this.segmentations = segmentations;
        this.children = children;
    }

    public ShiftDetectionConfig save(boolean writeToFile) {
        List<String> filePaths = new ArrayList<>();
        JSONObject json = new JSONObject();
        // **************************************
        // save ID, nSegmentMax, nObsMin
        json.put("ID", ID);
        json.put("nSegmentMax", nSegmentMax);
        json.put("nObsMin", nObsMin);

        // **************************************
        // save rating curve
        filePaths.add(ratingCurveEstimation.getBam().zipRun(writeToFile));
        json.put("rc", ratingCurveEstimation.ID);

        // **************************************
        // save array of segmentation IDs, ONE REQUIRED!
        JSONArray segJsonArr = new JSONArray();
        for (Integer key : segmentations.keySet()) {
            BamSegmentation s = segmentations.get(key);
            filePaths.add(s.getBam().zipRun(writeToFile));
            JSONObject j = new JSONObject();
            j.put("nSeg", key);
            j.put("ID", s.ID);
            segJsonArr.put(j);
        }
        json.put("seg", segJsonArr);

        // **************************************
        // save stage std if any
        if (ratingCurveEstimation.stageStd != null) {
            JSONArray arr = new JSONArray();
            for (int k = 0; k < ratingCurveEstimation.stageStd.length; k++) {
                arr.put(ratingCurveEstimation.stageStd[k]);
            }
            json.put("stageStd", arr);
        }

        // **************************************
        // save array of children IDs
        List<ShiftDetectionConfig> childrenConfig = new ArrayList<>();
        for (ShiftDetectionIteration rsd : children) {
            childrenConfig.add(rsd.save(writeToFile));
        }

        ShiftDetectionConfig config = new ShiftDetectionConfig(json, filePaths, childrenConfig);
        return config;
    }

    public static ShiftDetectionIteration load(ShiftDetectionConfig config) {
        // **************************************
        // load ID, nSegmentMax, nObsMin
        String ID = config.config().getString("ID");
        int nSegmentMax = config.config().getInt("nSegmentMax");
        int nObsMin = config.config().getInt("nObsMin");

        // **************************************
        // load all Bam runs
        BamRun rcBam = BamRun.buildFromTempZipArchive(config.config().getString("rc"));
        HashMap<Integer, BamRun> segBam = new HashMap<>();
        JSONArray segJsonArr = config.config().getJSONArray("seg");
        for (int k = 0; k < segJsonArr.length(); k++) {
            JSONObject j = segJsonArr.getJSONObject(k);
            int nSeg = j.getInt("nSeg");
            String segId = j.getString("ID");
            segBam.put(nSeg, BamRun.buildFromTempZipArchive(segId));
        }
        BamRun refSegBam = segBam.get(1);

        // **************************************
        // get inputs/outputs
        CalibrationConfig segCalConfig = refSegBam.getCalibrationConfig();
        double[] time = segCalConfig.calibrationData.inputs[0].values;

        // **************************************
        // load stage std if any
        double[] stageStd = null;
        if (config.config().has("stageStd")) {
            JSONArray arr = config.config().getJSONArray("stageStd");
            stageStd = new double[arr.length()];
            for (int k = 0; k < arr.length(); k++) {
                stageStd[k] = arr.getDouble(k);
            }
        }

        // **************************************
        // load all children
        List<ShiftDetectionIteration> children = new ArrayList<>();
        for (ShiftDetectionConfig c : config.children()) {
            children.add(ShiftDetectionIteration.load(c));
        }

        // **************************************
        // build the actual RatingShifDetection object
        BamRatingCurveRun ratingCurveEstimation = new BamRatingCurveRun(rcBam, time, stageStd);
        HashMap<Integer, BamSegmentation> segmentations = new HashMap<>();
        for (Integer nSeg : segBam.keySet()) {
            BamSegmentation s = new BamSegmentation(segBam.get(nSeg), nSeg, nObsMin);
            segmentations.put(nSeg, s);
        }
        ShiftDetectionIteration rse = new ShiftDetectionIteration(ID, nSegmentMax, nObsMin,
                ratingCurveEstimation,
                segmentations, children);

        return rse;
    }

    private static String buildId(double[] time) {
        String id = Misc.getId();
        String dataId = "nodata";
        if (time.length > 0) {
            dataId = String.format("%s_%s",
                    DateTime.dateTimeToTimeStamp(DateTime.doubleToDateTime(time[0]), "yyMMdd"),
                    DateTime.dateTimeToTimeStamp(DateTime.doubleToDateTime(time[time.length - 1]), "yyMMdd"));
        }
        return String.format("%s_%s", id, dataId);
    }

    public void runShiftDetection(Consumer<Float> progress) {

        bestSegmentation = null;
        allBestSegmentations = null;
        currentSegmentationEstimation = null;

        float rcPart = 0.5f;
        float segPart = (1f - rcPart) / nSegmentMax;
        ratingCurveEstimation.runBam(p -> {
            float partial = p * rcPart;
            progress.accept(partial);
        });

        segmentations.clear();

        for (int k = 1; k <= nSegmentMax; k++) {
            if (ratingCurveEstimation.time.length / k < nObsMin) {
                break; // BaM will fail in this case
            }
            int K = k;
            currentSegmentationEstimation = new BamSegmentation(
                    ratingCurveEstimation.ID,
                    ratingCurveEstimation.time,
                    ratingCurveEstimation.getResiduals(),
                    ratingCurveEstimation.getResidualsStd(),
                    k,
                    nObsMin);
            try {
                currentSegmentationEstimation.runBam(p -> {
                    float partial = p * segPart + (K - 1) * segPart + rcPart;
                    progress.accept(partial);
                });
                if (currentSegmentationEstimation.hasResults()) {
                    segmentations.put(k, currentSegmentationEstimation);
                }
            } catch (Exception e) {
                ConsoleLogger.error(e);
            }
            currentSegmentationEstimation = null;
        }

        buildChildren();

    }

    public void cancel() {
        ratingCurveEstimation.cancel();
        if (currentSegmentationEstimation != null) {
            currentSegmentationEstimation.cancel();
        }
    }

    public String getName() {
        if (ratingCurveEstimation.time.length == 0) {
            return "-";
        }
        String start = DateTime.dateTimeToTimeStamp(
                DateTime.doubleToDateTime(ratingCurveEstimation.time[0]),
                "YYYY-MM-dd HH:mm");
        String end = DateTime.dateTimeToTimeStamp(
                DateTime.doubleToDateTime(ratingCurveEstimation.time[ratingCurveEstimation.time.length - 1]),
                "YYYY-MM-dd HH:mm");
        return String.format("%s -> %s", start, end);
    }

    public ShiftDetectionIteration getRatingShiftDetection(String id) {
        if (ID.equals(id)) {
            return this;
        }
        for (ShiftDetectionIteration rsd : children) {
            ShiftDetectionIteration found = rsd.getRatingShiftDetection(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public List<ShiftDetectionIteration> getChildren() {
        return children;
    }

    public BamSegmentation getBestSegmentation() {
        if (bestSegmentation != null) {
            return bestSegmentation;
        }
        double minDIC = Double.MAX_VALUE;
        Integer bestDICindex = -1;
        for (int k = 1; k <= nSegmentMax; k++) {
            BamSegmentation se = segmentations.get(k);
            if (se == null) {
                continue;
            }
            double DIC = se.getDIC();
            if (DIC < minDIC) {
                minDIC = DIC;
                bestDICindex = k;
            }
        }
        bestSegmentation = bestDICindex >= 0 ? segmentations.get(bestDICindex) : null;
        return bestSegmentation;

    }

    public List<BamSegmentation> getAllBestSegmentation() {
        if (allBestSegmentations != null) {
            return allBestSegmentations;
        }
        allBestSegmentations = new ArrayList<>();
        BamSegmentation se = getBestSegmentation();
        if (se != null) {
            allBestSegmentations.add(se);
            if (children != null) {
                for (ShiftDetectionIteration rsd : children) {
                    List<BamSegmentation> childrenSegmentation = rsd.getAllBestSegmentation();
                    allBestSegmentations.addAll(childrenSegmentation);
                }
            }
        }
        return allBestSegmentations;
    }

    public List<Double> getAllShifts() {
        List<Double> shifts = new ArrayList<>();
        BamSegmentation se = getBestSegmentation();
        if (se == null) {
            return shifts;
        }
        for (EstimatedParameter p : se.getTauParameters()) {
            shifts.add(p.getMaxpost());
        }
        if (children != null) {
            for (ShiftDetectionIteration rsd : children) {
                shifts.addAll(rsd.getAllShifts());
            }
        }
        return shifts;
    }

    private void buildChildren() {

        children.clear();

        BamSegmentation bestSegmention = getBestSegmentation();
        if (bestSegmention == null || bestSegmention.nSegment == 1) {
            return;
        }

        int startIndex = 0;
        for (int i = 0; i < bestSegmention.nSegment; i++) {
            List<EstimatedParameter> tauParameters = bestSegmention.getTauParameters();
            double tau = i == bestSegmention.nSegment - 1
                    ? ratingCurveEstimation.time[ratingCurveEstimation.time.length - 1] + 1
                    : tauParameters.get(i).getMaxpost();
            int endIndex = ratingCurveEstimation.time.length;
            for (int j = startIndex + 1; j < ratingCurveEstimation.time.length; j++) {
                if (ratingCurveEstimation.time[j] >= tau) {
                    endIndex = j;
                    break;
                }
            }

            double[] segmentTime = Arrays.copyOfRange(ratingCurveEstimation.time, startIndex, endIndex);
            double[] segmentStage = Arrays.copyOfRange(ratingCurveEstimation.stage, startIndex, endIndex);
            double[] segmentStageStd = ratingCurveEstimation.stageStd == null ? null
                    : Arrays.copyOfRange(ratingCurveEstimation.stageStd, startIndex, endIndex);
            double[] segmentDischarge = Arrays.copyOfRange(ratingCurveEstimation.discharge, startIndex, endIndex);
            double[] segmentDischargeStd = Arrays.copyOfRange(ratingCurveEstimation.dischargeStd, startIndex, endIndex);

            startIndex = endIndex;

            if (segmentTime.length < nObsMin) {
                continue;
            }

            children.add(new ShiftDetectionIteration(
                    segmentTime,
                    segmentStage,
                    segmentStageStd,
                    segmentDischarge,
                    segmentDischargeStd,
                    ratingCurveEstimation.model,
                    nSegmentMax,
                    nObsMin));

        }

    }

}
