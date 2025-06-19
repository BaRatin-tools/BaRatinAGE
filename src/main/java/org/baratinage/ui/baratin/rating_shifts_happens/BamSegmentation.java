package org.baratinage.ui.baratin.rating_shifts_happens;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.ui.bam.run.BamRun;
import org.baratinage.utils.Calc;

public class BamSegmentation {

  public final Integer nSegment;
  public final Integer nObsMin;
  public final String ID;

  private final double[] time;
  private final double[] residuals;
  private final double[] residualsStd;

  private BamRun bam;

  public BamSegmentation(
      String id,
      double[] time,
      double[] residuals,
      double[] residualsStd,
      int nSegment,
      int nObsMin) {

    this.nSegment = nSegment;
    this.nObsMin = nObsMin;
    this.ID = String.format("%s_seg_%d", id, nSegment);
    this.time = time;
    this.residuals = residuals;
    this.residualsStd = residualsStd;

    CalibrationConfig calibrationConfig = getCalibrationConfig();

    bam = new BamRun(
        ID,
        Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, ID),
        BaM.buildBamForCalibration(calibrationConfig));
  }

  public BamSegmentation(
      BamRun bam,
      int nSegment,
      int nObsMin) {

    this.bam = bam;
    this.nSegment = nSegment;
    this.nObsMin = nObsMin;

    ID = bam.id;
    time = bam.getCalibrationConfig().calibrationData.inputs[0].values;
    residuals = bam.getCalibrationConfig().calibrationData.outputs[0].values;
    residualsStd = bam.getCalibrationConfig().calibrationData.outputs[0].nonSysStd;
  }

  public boolean hasResults() {
    return bam != null && bam.hasResults();
  }

  public BamRun getBam() {
    return bam;
  }

  private Parameter[] buildPriorParameters() {

    // parameters describing the mean residual values of each segments
    double meanResidual = Calc.mean(residuals);
    Parameter[] muParameters = buildFlatPriorParameters("mu_", nSegment, meanResidual);

    // parameters describing the transition times between segments
    if (nSegment == 1) {
      return muParameters;
    }

    double[] sequence = Calc.sequence(0, 1, nSegment);
    double[] percentiles = new double[nSegment - 1];
    for (int k = 0; k < nSegment - 1; k++) {
      percentiles[k] = sequence[k + 1];
    }

    double[] initialTaus = Calc.percentiles(time, true, percentiles);
    Parameter[] tauParameters = buildFlatPriorParameters("tau_", initialTaus);

    Parameter[] parameters = Stream
        .concat(Arrays.stream(muParameters), Arrays.stream(tauParameters))
        .toArray(Parameter[]::new);

    return parameters;
  }

  private static String buildXtraString(int nSegments, int tMin, int nMinObs, int xtraOption) {
    ConfigFile cf = new ConfigFile();
    cf.addItem(nSegments, "number of segments");
    cf.addItem(tMin, "???");
    cf.addItem(nMinObs, "minimum number of gaugings per segment");
    cf.addItem(xtraOption, "???");
    return cf.toString();
  }

  private Model buildModel() {

    // build parameters
    Parameter[] parameters = buildPriorParameters();

    // build xtra content
    String xTraString = buildXtraString(nSegment, 0, nObsMin, 1);

    // Model:
    Model model = new Model(
        BamFilesHelpers.CONFIG_MODEL,
        "Segmentation",
        1, 1,
        parameters,
        xTraString,
        BamFilesHelpers.CONFIG_XTRA);

    return model;
  }

  private CalibrationData buildCalibrationData() {

    UncertainData[] inputs = new UncertainData[1];
    inputs[0] = new UncertainData("time", time);

    UncertainData[] outputs = new UncertainData[1];
    outputs[0] = new UncertainData("residuals", residuals, residualsStd);

    String calibrationDataName = "calib_data_" + ID;
    String calibrationDataFile = String.format(BamFilesHelpers.DATA_CALIBRATION, calibrationDataName);
    CalibrationData calibrationData = new CalibrationData(
        calibrationDataName,
        BamFilesHelpers.CONFIG_CALIBRATION,
        calibrationDataFile,
        inputs,
        outputs);

    return calibrationData;
  }

  private CalibrationConfig getCalibrationConfig() {

    Model model = buildModel();

    double stdResiduals = Calc.std(residuals); // will fail if residuals is empty
    if (stdResiduals == 0) {
      stdResiduals = 1;
    }
    ModelOutput[] modelOutputs = buildDefaultModelOutput(stdResiduals);

    CalibrationData calibrationData = buildCalibrationData();

    CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();
    McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
    McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig(true, true);
    McmcConfig mcmcConfig = new McmcConfig();

    CalibrationConfig calibrationConfig = new CalibrationConfig(
        model,
        modelOutputs,
        calibrationData,
        mcmcConfig,
        mcmcCookingConfig,
        mcmcSummaryConfig,
        calDataResidualConfig);

    return calibrationConfig;
  }

  public void runBam(Consumer<Float> progress) {

    bam.executeSync(
        p -> {
          progress.accept(p.totalProgress());
        },
        p -> {
          System.out.println("segmentation: " + p);
        });
  }

  public double getDIC() {
    CalibrationResult results = bam.getCalibrationResults();
    if (results == null) {
      return Double.MAX_VALUE;
    }
    return results.DIC.get("DIC1");
  }

  public List<EstimatedParameter> getTauParameters() {
    List<EstimatedParameter> estimPar = bam
        .getCalibrationResults().estimatedParameters
        .stream()
        .filter(p -> p.name.startsWith("tau"))
        .toList();
    return estimPar;
  }

  public void cancel() {
    if (bam != null) {
      Process p = bam.getBaMexecutionProcess();
      if (p != null) {
        p.destroy();
      }
    }
  }

  private static Parameter[] buildFlatPriorParameters(String prefix, int n, double initialValue) {
    double[] initialValues = new double[n];
    for (int k = 0; k < n; k++) {
      initialValues[k] = initialValue;
    }
    return buildFlatPriorParameters(prefix, initialValues);
  }

  private static Parameter[] buildFlatPriorParameters(String prefix, double[] initialValues) {
    Parameter[] parameters = new Parameter[initialValues.length];
    Distribution flatDist = new Distribution(DistributionType.FLAT);
    for (int k = 0; k < initialValues.length; k++) {
      parameters[k] = new Parameter(prefix + k, initialValues[k], flatDist);
    }
    return parameters;
  }

  private static ModelOutput[] buildDefaultModelOutput(double initialValue) {
    Distribution dist = new Distribution(DistributionType.FLAT_POSITIVE);
    Parameter gamma = new Parameter("gamma", 1, dist);
    String name = "RES";
    String fileName = String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name);
    StructuralErrorModel sem = new StructuralErrorModel(name, fileName, "Constant", gamma);
    ModelOutput[] modelOutputs = new ModelOutput[1];
    modelOutputs[0] = new ModelOutput(0, sem);
    return modelOutputs;
  }

}
