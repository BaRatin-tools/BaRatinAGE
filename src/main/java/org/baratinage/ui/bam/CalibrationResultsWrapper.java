package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;

public abstract class CalibrationResultsWrapper {
    /**
     * Organized version of all the estimated parameter of a BaM run
     * - it gets the names of the model parameters
     * - it gets the names of the structural error model parameters
     * - it assumes no parameter can be named LogPost which is reserved
     * - and that all derived parameters are located after the LogPost
     * column.
     * 
     * It stores the estimated parameters in various objects:
     * - modelParameters
     * - modelDerivedParameters
     * - logPost
     * - structuralModelsParameters (gammaParameters)
     * 
     * This class needs to be extended to accomodate specific needs for specific
     * models such as in the case of BaRatin where derived and original model
     * parameters are mixed and reorder
     */

    protected final CalibrationResult calibrationResults;

    public CalibrationResultsWrapper(CalibrationResult calibrationResults) throws NoSuchElementException {
        this.calibrationResults = calibrationResults;
    }

    protected EstimatedParameterWrapper extractLogPost() {
        EstimatedParameter logPostPar = findParameterByName(calibrationResults.estimatedParameters, "LogPost");
        return new EstimatedParameterWrapper(logPostPar, EstimatedParameterWrapper.LOGPOST);
    }

    protected HashMap<String, List<EstimatedParameterWrapper>> extractGammasPerModelOutput() {
        HashMap<String, List<EstimatedParameterWrapper>> gammaParametersPerModelOutput = new HashMap<>();
        for (ModelOutput mo : calibrationResults.calibrationConfig.modelOutputs) {
            Stream<Parameter> moParamStream = Arrays.stream(mo.structuralErrorModel.parameters);
            List<EstimatedParameterWrapper> gammas = moParamStream.map(p -> {
                String pName = String.format("%s_%s", mo.name, p.name);
                return new EstimatedParameterWrapper(
                        findParameterByName(calibrationResults.estimatedParameters, pName),
                        EstimatedParameterWrapper.GAMMA);
            }).toList();
            gammaParametersPerModelOutput.put(mo.name, gammas);
        }
        return gammaParametersPerModelOutput;
    }

    protected List<EstimatedParameterWrapper> extractGammas() {
        HashMap<String, List<EstimatedParameterWrapper>> gammaParametersPerModelOutput = extractGammasPerModelOutput();
        List<EstimatedParameterWrapper> gammaParameters = new ArrayList<>();
        for (List<EstimatedParameterWrapper> gp : gammaParametersPerModelOutput.values()) {
            gammaParameters.addAll(gp);
        }
        return gammaParameters;
    }

    protected List<EstimatedParameterWrapper> extractModelParameters() {
        Stream<Parameter> modelParamStream = Arrays.stream(calibrationResults.calibrationConfig.model.parameters);
        List<EstimatedParameterWrapper> modelParameters = modelParamStream.map(p -> {
            return new EstimatedParameterWrapper(
                    findParameterByName(calibrationResults.estimatedParameters, p.name),
                    EstimatedParameterWrapper.MODEL);
        }).toList();
        return modelParameters;
    }

    protected List<EstimatedParameterWrapper> extractDerivedParameters() {
        int n = calibrationResults.estimatedParameters.size();
        int logPostIndex = -1;
        for (int k = 0; k < n; k++) {
            if (calibrationResults.estimatedParameters.get(k).name.equals("LogPost")) {
                logPostIndex = k;
                break;
            }
        }
        List<EstimatedParameterWrapper> derivedParameters = new ArrayList<>();
        if (logPostIndex < n - 1) {
            for (int k = logPostIndex + 1; k < n; k++) {
                derivedParameters.add(new EstimatedParameterWrapper(
                        calibrationResults.estimatedParameters.get(k),
                        EstimatedParameterWrapper.DERIVED));
            }
        }
        return derivedParameters;
    }

    protected List<EstimatedParameterWrapper> extractAllParameters() {
        List<EstimatedParameterWrapper> allParameters = new ArrayList<>();
        allParameters.addAll(extractModelParameters());
        allParameters.addAll(extractDerivedParameters());
        allParameters.addAll(extractGammas());
        allParameters.add(extractLogPost());
        return allParameters;
    }

    public abstract List<EstimatedParameterWrapper> getAllParameters();

    public abstract EstimatedParameterWrapper getLogPost();

    public abstract HashMap<String, List<EstimatedParameterWrapper>> getGammaParametersPerModelOutput();

    private static EstimatedParameter findParameterByName(List<EstimatedParameter> parameters, String parameterName)
            throws NoSuchElementException {
        return parameters.stream()
                .filter(p -> p.name.equals(parameterName))
                .findFirst()
                .orElseThrow(() -> {
                    return new NoSuchElementException(
                            String.format("Parameter named \"%s\" not found!", parameterName));
                });
    }

    protected static EstimatedParameterWrapper findByName(List<EstimatedParameterWrapper> parameters,
            String parameterName) throws NoSuchElementException {
        return parameters.stream()
                .filter(p -> p.parameter.name.equals(parameterName))
                .findFirst()
                .orElseThrow(() -> {
                    return new NoSuchElementException(
                            String.format("Parameter named \"%s\" not found!", parameterName));
                });
    }

    protected static List<EstimatedParameterWrapper> extractParameters(
            List<EstimatedParameterWrapper> parameters,
            EstimatedParameterWrapper.TYPE... types) {
        return parameters
                .stream()
                .filter(p -> {
                    for (EstimatedParameterWrapper.TYPE t : types) {
                        if (p.type == t) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }

    // public static List<EstimatedParameterWrapper>
    // getModelParameters(List<EstimatedParameterWrapper> parameters) {
    // return extractParameters(parameters, EstimatedParameterWrapper.MODEL);
    // }

    // public static List<EstimatedParameterWrapper>
    // getDerivedParameters(List<EstimatedParameterWrapper> parameters) {
    // return extractParameters(parameters, EstimatedParameterWrapper.DERIVED);
    // }

    // public static List<EstimatedParameterWrapper> getModelAndDerivedParameters(
    // List<EstimatedParameterWrapper> parameters) {
    // return extractParameters(parameters, EstimatedParameterWrapper.MODEL,
    // EstimatedParameterWrapper.DERIVED);
    // }
}
