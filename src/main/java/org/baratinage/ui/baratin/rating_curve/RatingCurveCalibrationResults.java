package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.bam.CalibrationResultsWrapper;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;

public class RatingCurveCalibrationResults extends CalibrationResultsWrapper {

    /**
     * mix model parameters and derived parameters
     * rename derive parameters
     * extract stage transition
     * order
     */

    private final HashMap<String, List<EstimatedParameterWrapper>> gammaParametersPerModelOutput;
    private final List<EstimatedParameterWrapper> allParameters;
    private final EstimatedParameterWrapper logPost;
    private final List<EstimatedParameterWrapper> stageTransition;
    private final String equationString;

    public RatingCurveCalibrationResults(CalibrationResult calibrationResults) throws NoSuchElementException {
        super(calibrationResults);

        List<EstimatedParameterWrapper> rawParameters = extractAllParameters();
        allParameters = new ArrayList<>();
        gammaParametersPerModelOutput = new HashMap<>();
        stageTransition = new ArrayList<>();

        String modelId = calibrationResults.calibrationConfig.model.modelId;
        if (modelId.equals("BaRatin")) {
            int nControls = calibrationResults.calibrationConfig.model.parameters.length / 3;
            allParameters.addAll(getParametersForBaRatin(rawParameters, nControls, false));
            equationString = processControlMatrixEquation(false);
        } else if (modelId.equals("BaRatinBAC")) {
            int nControls = calibrationResults.calibrationConfig.model.parameters.length
                    / 3;
            allParameters.addAll(getParametersForBaRatin(rawParameters, nControls, true));
            equationString = processControlMatrixEquation(true);
        } else if (modelId.equals("TextFile")) {
            allParameters.addAll(getParametersForAnyModel(rawParameters));
            ConfigFile xTraConfig = ConfigFile.parseConfigFileString(calibrationResults.calibrationConfig.model.xTra);
            equationString = xTraConfig.getString(5);
        } else {
            allParameters.addAll(getParametersForAnyModel(rawParameters));
            equationString = "";
        }

        allParameters.addAll(getGammas(rawParameters));
        logPost = extractLogPost();
        allParameters.add(logPost);

    }

    private static List<EstimatedParameterWrapper> getParametersForAnyModel(
            List<EstimatedParameterWrapper> parameters) {
        List<EstimatedParameterWrapper> anyModelParameters = new ArrayList<>();
        anyModelParameters.addAll(extractParameters(parameters, EstimatedParameterWrapper.MODEL));
        anyModelParameters.addAll(extractParameters(parameters, EstimatedParameterWrapper.DERIVED));
        return anyModelParameters;
    }

    private static List<EstimatedParameterWrapper> getParametersForBaRatin(
            List<EstimatedParameterWrapper> parameters,
            int nControls,
            boolean isBAC) {

        List<EstimatedParameterWrapper> baratinParameters = new ArrayList<>();

        for (int i = 0; i < nControls; i++) {

            EstimatedParameterWrapper k;
            EstimatedParameterWrapper b;
            if (isBAC) {
                k = findByName(parameters, String.format("k%d", i + 1));
                b = findByName(parameters, String.format("b_%d", i));
            } else {
                k = findByName(parameters, String.format("k_%d", i));
                b = findByName(parameters, String.format("b%d", i + 1));
            }

            baratinParameters.add(k.copyAndModify(String.format("k_%d", i + 1),
                    String.format("<html>&kappa;<sub>%d</sub></html>", i + 1)));
            baratinParameters.add(
                    findByName(parameters, String.format("a_%d", i))
                            .copyAndModify(
                                    String.format("a_%d", i + 1),
                                    String.format("<html>a<sub>%d</sub></html>", i + 1)));
            baratinParameters.add(
                    findByName(parameters, String.format("c_%d", i))
                            .copyAndModify(
                                    String.format("c_%d", i + 1),
                                    String.format("<html>c<sub>%d</sub></html>", i + 1)));
            baratinParameters.add(b.copyAndModify(String.format("k_%d", i + 1),
                    String.format("<html>b<sub>%d</sub></html>", i + 1)));
        }

        return baratinParameters;
    }

    private static List<EstimatedParameterWrapper> getGammas(List<EstimatedParameterWrapper> parameters) {
        List<EstimatedParameterWrapper> rawGammaParameters = extractParameters(parameters,
                EstimatedParameterWrapper.GAMMA);
        EstimatedParameterWrapper g0;
        EstimatedParameterWrapper g1;
        // this try_catch is for backward compatibility
        // we could simplify by just assuming two parameters in the
        // correct order
        try {
            g0 = findByName(rawGammaParameters, "Y1_gamma_0");
            g1 = findByName(rawGammaParameters, "Y1_gamma_1");
        } catch (NoSuchElementException e) {
            System.err.println(e);
            g0 = findByName(rawGammaParameters, "Y1_gamma1");
            g1 = findByName(rawGammaParameters, "Y1_gamma2");
        }

        List<EstimatedParameterWrapper> gammaParameters = new ArrayList<>();
        gammaParameters.add(g0.copyAndModify("gamma_1",
                "<html>&gamma;<sub>1</sub></html>"));
        gammaParameters.add(g1.copyAndModify("gamma_2",
                "<html>&gamma;<sub>2</sub></html>"));

        return gammaParameters;
    }

    public List<EstimatedParameterWrapper> getStageTransitionParameters() {
        return stageTransition;
    }

    public List<double[]> getStageTransitions() {
        return stageTransition
                .stream()
                .map(bep -> {
                    double[] u95 = bep.parameter.get95interval();
                    double mp = bep.parameter.getMaxpost();
                    return new double[] { mp, u95[0], u95[1] };
                }).collect(Collectors.toList());
    }

    public String getEquationString() {
        return equationString;
    }

    public String processControlMatrixEquation(boolean bac) {
        boolean[][] controlMatrix = ControlMatrix.fromXtra(calibrationResults.calibrationConfig.model.xTra,
                bac);
        int nCtrlSeg = controlMatrix.length;
        String[] equationLines = new String[nCtrlSeg + 1];
        equationLines[0] = "h < " + allParameters.get(0).parameter.getMaxpost() + ": Q = 0";
        for (int i = 0; i < nCtrlSeg; i++) { // for each segment (stage range)
            // retrieve control stage range and initialize equation line
            Double k = allParameters.get(i * 4 + 0).parameter.getMaxpost();
            Double kNext = i < nCtrlSeg - 1 ? allParameters.get((i + 1) * 4 + 0).parameter.getMaxpost() : null;
            String eqStr = kNext != null ? k + " < h < " + kNext : "h > " + k;
            eqStr = eqStr + ": Q = ";
            boolean first = true;
            for (int j = 0; j <= i; j++) { // for each possibly active control
                if (controlMatrix[i][j]) {
                    Double a = allParameters.get(j * 4 + 1).parameter.getMaxpost();
                    Double b = allParameters.get(i * 4 + 3).parameter.getMaxpost();
                    Double c = allParameters.get(i * 4 + 2).parameter.getMaxpost();

                    eqStr = eqStr + (first ? a : processAdd(a));
                    eqStr = eqStr + " * (h" + processSub(b) + ") ^ " + c;
                    first = false;
                }
            }
            equationLines[i + 1] = eqStr;
        }
        // equationTextArea.setText(String.join("\n", equationLines));
        return String.join("\n", equationLines);
    }

    private static String processAdd(Double value) {
        return value < 0 ? " - " + (value * -1) : " + " + value;
    }

    private static String processSub(Double value) {
        return value < 0 ? " + " + (value * -1) : " - " + value;
    }

    @Override
    public EstimatedParameterWrapper getLogPost() {
        return logPost;
    }

    @Override
    public HashMap<String, List<EstimatedParameterWrapper>> getGammaParametersPerModelOutput() {
        return gammaParametersPerModelOutput;
    }

    @Override
    public List<EstimatedParameterWrapper> getAllParameters() {
        return allParameters;
    }

    public List<EstimatedParameterWrapper> getModelAndDerivedParameters() {
        return extractParameters(allParameters, EstimatedParameterWrapper.MODEL, EstimatedParameterWrapper.DERIVED);
    }
}
