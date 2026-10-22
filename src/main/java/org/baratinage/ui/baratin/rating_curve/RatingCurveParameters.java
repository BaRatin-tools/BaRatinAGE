package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.baratinage.jbam.CalibrationDataResiduals;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.ResultColumn;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.utils.Calc;
import org.baratinage.utils.ParameterSymbol;

public class RatingCurveParameters {

  /**
   * The estimated parameters of one hydraulic control. NOTE: package-private
   * because the equation builder needs them.
   */
  record Ctrl(ResultColumn a, ResultColumn c, ResultColumn k, ResultColumn b) {
  }

  private final String equationString;
  private final String equationLatex;
  private final CalibrationResult calibrationResult;

  private final List<Ctrl> controls;
  private final List<ResultColumn> modelParameters;
  private final List<ResultColumn> gammaParameters;
  private final ResultColumn logPost;

  public RatingCurveParameters(CalibrationResult calibrationResult) {
    this.calibrationResult = calibrationResult;

    String modelId = calibrationResult.calibrationConfig.model.modelId;

    // the goal here is :
    // - reorganize parameters in a logical way for each model
    // - create the text and Latex equation
    if (modelId.equals("BaRatin") || modelId.equals("BaRatinBAC")) {
      boolean isBAC = modelId.equals("BaRatinBAC");
      controls = getBaRatin(isBAC);
      modelParameters = getModelParametersFromControls();
      // NOTE: for BaRatinBAC the xTra file has one extra row
      boolean[][] controlMatrix = ControlMatrix.fromXtra(
          calibrationResult.calibrationConfig.model.xTra,
          isBAC);
      RatingCurveEquationBuilder.Equations equations = RatingCurveEquationBuilder.fromControls(controls, controlMatrix);
      equationString = equations.text();
      equationLatex = equations.latex();
    } else {
      controls = null;
      modelParameters = getResultColumns(
          ResultColumn.KIND.ESTIMATED,
          ResultColumn.KIND.SYNTHETIC,
          ResultColumn.KIND.DERIVED);
      // only text file models define their equation in the xTra file
      if (modelId.equals("TextFile")) {
        equationString = getTextFileEquation(calibrationResult);
        equationLatex = RatingCurveEquationBuilder.latexFromFormula(
            equationString);
      } else {
        equationString = "";
        equationLatex = null;
      }
    }
    logPost = getResultColumns(ResultColumn.KIND.LOGPOST).get(0);
    // for gamma parameters, drop the model output index in the name since there's
    // only one output
    List<ResultColumn> gammas = getResultColumns(ResultColumn.KIND.GAMMA);
    gammaParameters = new ArrayList<>();
    for (ResultColumn g : gammas) {
      gammaParameters.add(g.withSymbol(ParameterSymbol.of(g.symbol.base(), g.symbol.subscripts().get(1))));
    }
  }

  private List<Ctrl> getBaRatin(boolean isBAC) {
    List<Ctrl> controls = new ArrayList<>();
    int nCtrl = calibrationResult.calibrationConfig.model.parameters.length / 3;
    for (int i = 0; i < nCtrl; i++) {
      ResultColumn a = calibrationResult.getResultColumn(
          String.format("a_%d", i));
      ResultColumn c = calibrationResult.getResultColumn(
          String.format("c_%d", i));
      ResultColumn k;
      ResultColumn b;
      if (isBAC) {
        k = calibrationResult.getResultColumn(String.format("k%d", i + 1));
        b = calibrationResult.getResultColumn(String.format("b_%d", i));
        b = b.withSymbol(ParameterSymbol.ofIndices("b", i + 1));
      } else {
        k = calibrationResult.getResultColumn(String.format("k_%d", i));
        b = calibrationResult.getResultColumn(String.format("b%d", i + 1));
        k = k.withSymbol(ParameterSymbol.ofIndices("κ", i + 1));
      }
      a = a.withSymbol(ParameterSymbol.ofIndices("a", i + 1));
      c = c.withSymbol(ParameterSymbol.ofIndices("c", i + 1));
      controls.add(new Ctrl(a, c, k, b));
    }
    return controls;
  }

  private List<ResultColumn> getModelParametersFromControls() {
    List<ResultColumn> modelParameters = new ArrayList<>();
    if (controls == null) {
      return modelParameters;
    }
    for (int i = 0; i < controls.size(); i++) {
      Ctrl ctrl = controls.get(i);
      modelParameters.add(ctrl.k());
      modelParameters.add(ctrl.a());
      modelParameters.add(ctrl.c());
      modelParameters.add(ctrl.b());
    }
    return modelParameters;
  }

  public ResultColumn getLogPost() {
    return logPost;
  }

  public List<ResultColumn> getModelParameters() {
    return modelParameters;
  }

  public List<ResultColumn> getGammaParameters() {
    return gammaParameters;
  }

  private static String getTextFileEquation(
      CalibrationResult calibrationResult) {
    // for text file models, the equation is the sixth item of the xTra file
    // (see QFHModelDefinition.getXtra)
    ConfigFile xTraConfig = ConfigFile.parseConfigFileString(
        calibrationResult.calibrationConfig.model.xTra);
    return xTraConfig.getString(5);
  }

  public List<double[]> getStageTransitions(boolean prior) {
    List<double[]> transitions = new ArrayList<>();
    if (controls == null) {
      return transitions;
    }
    for (int i = 0; i < controls.size(); i++) {
      ResultColumn k = controls.get(i).k();
      Double central = null;
      double[] env = null;
      if (prior) {
        if (k.parameter != null) {
          central = k.parameter.getMedian();
          env = k.parameter.getPercentiles(0.025, 0.975, 2);
        } else {
          // Prior run = BaM MCMC on empty calibration data, so these samples are
          // prior draws of the derived parameter
          // this is for BaRatinBAC where k parameters are derived.
          double[] q = Calc.percentiles(k.samples, false, 0.5, 0.025, 0.975);
          central = q[0];
          env = new double[] { q[1], q[2] };
        }
      } else {
        central = k.getMaxpost();
        env = k.get95interval();
      }
      transitions.add(new double[] { central, env[0], env[1] });
    }
    return transitions;
  }

  public List<ResultColumn> getResultColumns(ResultColumn.KIND... filters) {
    if (filters.length == 0) {
      return calibrationResult.resultColumns;
    } else {
      Set<ResultColumn.KIND> filterSet = Set.of(filters);
      return calibrationResult.resultColumns
          .stream()
          .filter(rc -> filterSet.contains(rc.kind))
          .toList();
    }
  }

  public String getEquationString() {
    return equationString;
  }

  public String getEquationLatex() {
    return equationLatex;
  }

  public CalibrationDataResiduals getResiduals() {
    return calibrationResult.calibrationDataResiduals;
  }
}
