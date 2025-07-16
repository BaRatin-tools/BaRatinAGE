package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionState;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.run.BamRun;
import org.baratinage.ui.bam.run.BamRunException;
import org.baratinage.ui.component.CommonDialog;

public class BamRatingCurveRun {

  public final String ID;

  public final double[] time;
  public final double[] stage;
  public final double[] stageStd;
  public final double[] discharge;
  public final double[] dischargeStd;
  public final Model model;

  private BamRun bam;

  public BamRatingCurveRun(
      String id,
      double[] time,
      double[] stage,
      double[] stageStd,
      double[] discharge,
      double[] dischargeStd,
      Model model) {

    this.ID = id + "_rc";
    this.time = time;
    this.stage = stage;
    this.stageStd = stageStd;
    this.discharge = discharge;
    this.dischargeStd = dischargeStd;

    this.model = model;

    CalibrationData calibData = getCalibrationData();

    CalibrationConfig calibrationConfig = new CalibrationConfig(
        model,
        buildDefaultModelOutput(),
        calibData,
        new McmcConfig(),
        new McmcCookingConfig(),
        new McmcSummaryConfig(),
        new CalDataResidualConfig());

    PredictionConfig[] predictionConfigs = getPredictions(calibData);

    bam = new BamRun(
        ID,
        Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, ID),
        BaM.buildBamForCalibration(
            calibrationConfig,
            predictionConfigs));

  }

  public BamRun getBam() {
    return bam;
  }

  public BamRatingCurveRun(BamRun bam, double[] time, double[] stageStd) {
    this.bam = bam;
    this.time = time;
    ID = bam.id;
    stage = bam.getCalibrationConfig().calibrationData.inputs[0].values;
    this.stageStd = stageStd;
    discharge = bam.getCalibrationConfig().calibrationData.outputs[0].values;
    dischargeStd = bam.getCalibrationConfig().calibrationData.outputs[0].nonSysStd;
    model = bam.getCalibrationConfig().model;
  }

  private static ModelOutput[] buildDefaultModelOutput() {
    Distribution defaultDist = new Distribution(DistributionType.UNIFORM, 0, 10000);
    Parameter gamma1 = new Parameter("gamma1", 1, defaultDist);
    Parameter gamma2 = new Parameter("gamma2", 0.1, defaultDist);
    String name = String.format("Q");
    StructuralErrorModel sem = new StructuralErrorModel(
        name,
        String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
        "Linear",
        gamma1,
        gamma2);

    ModelOutput[] modelOuputs = new ModelOutput[1];
    modelOuputs[0] = new ModelOutput(0, sem);
    return modelOuputs;
  }

  private CalibrationData getCalibrationData() {
    String dataFileName = String.format(BamFilesHelpers.DATA_CALIBRATION, ID);
    UncertainData input = new UncertainData("stage", stage);
    UncertainData output = new UncertainData("discharge", discharge, dischargeStd);
    return new CalibrationData(
        ID,
        BamFilesHelpers.CONFIG_CALIBRATION,
        dataFileName,
        new UncertainData[] { input },
        new UncertainData[] { output });
  }

  private PredictionConfig[] getPredictions(CalibrationData calibData) {

    List<double[]> hInput = new ArrayList<>();
    if (stageStd == null) {
      hInput.add(calibData.inputs[0].values);
    } else {
      int nSamples = 200;
      double[][] hInputMatrix = calibData.inputs[0].getErrorMatrix(nSamples);
      for (int k = 0; k < nSamples; k++) {
        hInput.add(hInputMatrix[k]);
      }
    }

    PredictionInput hPredInput = new PredictionInput("gauging_stage", hInput);
    PredictionOutput QPredOutput = PredictionOutput.buildPredictionOutput(
        "gauging_discharge", "Q", true);

    PredExpSet predictionsConfig = new PredExpSet(
        new PredExp(PredictionConfig.buildPosteriorPrediction(
            "maxpost",
            new PredictionInput[] { hPredInput },
            new PredictionOutput[] { QPredOutput },
            new PredictionState[] {},
            true, false)));

    return predictionsConfig.getPredictionConfigs();
  }

  public void runBam(Consumer<Float> progress) {

    bam.executeSync(
        p -> {
          progress.accept(p.totalProgress());
        },
        p -> {
          System.out.println("baratin: " + p);
        },
        e -> {
          if (e instanceof BamRunException) {
            BamRunException bre = (BamRunException) e;
            bre.errorMessageDialog();
          } else {
            CommonDialog.errorDialog(
                T.text("bam_run_error_unknown_error"),
                T.text("bam_run_error_unknown_error"));
          }
        });

  }

  public void cancel() {
    if (bam != null) {
      Process p = bam.getBaMexecutionProcess();
      if (p != null) {
        p.destroy();
      }
    }
  }

  public double[] getResiduals() {
    return bam
        .getCalibrationResults().calibrationDataResiduals.outputResiduals
        .get(0)
        .resValues();
  }

  public double[] getResidualsStd() {
    double[] uQobs = dischargeStd;
    double[] uQsim = bam
        .getPredictionResults()[0].outputResults
        .get(0)
        .env()
        .get(6);
    // sqrt(residualsData[[1]]$uQ_sim^2+residualsData[[1]]$uQ_obs^2)
    double[] std = new double[uQobs.length];
    for (int k = 0; k < uQobs.length; k++) {
      std[k] = Math.sqrt(Math.pow(uQobs[k], 2.0) + Math.pow(uQsim[k],
          2.0));
    }
    return std;
  }

}
