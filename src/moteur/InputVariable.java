package moteur;

import java.util.List;
import java.util.Random;

import commons.Observation;

public class InputVariable {
    private String name;
    private Double[] values;
    private Double[] nonSystematicStd;
    private Double[] systematicStd;
    private int[] systematicIndices;
    private Double[][] nonSystematicErrors;
    private Double[][] systematicErrors;
    private boolean nonSystematicErrorsGenerated;
    private boolean systematicErrorsGenerated;
    // Could be Observation[] instead... Unclear whether nonSyst, syst error
    // approach is generic enough to be applicable to other models
    // Observation should be refactored to be more generic anyway (e.g. use a
    // metadata class instead of a time class)
    // or I could make a constructor from a Timeseries object? Limnigraph object?
    // ... where I would actually compute the matrices from the uncertainties?

    private void set(String name, Double[] values, Double[] nonSystematicStd, Double[] systematicStd,
            int[] systematicIndices) {
        this.nonSystematicErrorsGenerated = false;
        this.systematicErrorsGenerated = false;
        this.name = name;
        this.values = values;
        this.nonSystematicStd = nonSystematicStd;
        this.systematicStd = systematicStd;
        this.systematicIndices = systematicIndices;
    }

    public InputVariable(String name, Double[] values, Double[] nonSystematicStd, Double[] systematicStd,
            int[] systematicIndices) {
        set(name, values, nonSystematicStd, systematicStd, systematicIndices);
    }

    public InputVariable(Limnigraph limnigraph) {
        // Convert the list of observation into a regular array of observation
        List<Observation> listOfObservations = limnigraph.getObservations();
        int nObs = listOfObservations.size();
        Observation[] observations = new Observation[nObs];
        for (int k = 0; k < nObs; k++) {
            observations[k] = listOfObservations.get(k);
        }
        // Create the value vector
        Double[] values = new Double[nObs];
        for (int k = 0; k < nObs; k++) {
            values[k] = observations[k].getValue();
        }
        // retrieve the uncertainty related vectors
        nonSystematicStd = limnigraph.getuH();
        systematicStd = limnigraph.getbH();
        systematicIndices = limnigraph.getbHindx();
        // use default constructor
        set(limnigraph.getName(), values, nonSystematicStd, systematicStd, systematicIndices);
    }

    private static Double[][] computeNonSystematicErrorsMatrix(int nReplication, Double[] nonSystematicStd) {
        Random pseudoRandomNumberGenerator = new Random();
        int nObs = nonSystematicStd.length;
        Double[][] matrix = new Double[nObs][nReplication];
        for (int k = 0; k < nObs; k++) {
            for (int i = 0; i < nReplication; i++) {
                // matrix[k][i] = pseudoRandomNumberGenerator.nextGaussian(0.0, (double)
                // nonSystematicStd[k]);
                matrix[k][i] = pseudoRandomNumberGenerator.nextGaussian() * nonSystematicStd[k];
            }
        }
        return matrix;
    }

    private static Double[] createErrorArray(Random pseudoRandomNumberGenerator, Double mean, Double std,
            int nReplication) {
        Double[] randomErrorArray = new Double[nReplication];
        for (int k = 0; k < nReplication; k++) {
            // randomErrorArray[k] = pseudoRandomNumberGenerator.nextGaussian(mean, std);
            randomErrorArray[k] = pseudoRandomNumberGenerator.nextGaussian() * std + mean;
        }
        return randomErrorArray;
    }

    private static Double[][] computeSystematicErrorsMatrix(int nReplication, Double[] systematicStd,
            int[] systematicIndices) {
        Random pseudoRandomNumberGenerator = new Random();
        int nObs = systematicStd.length;
        Double[][] matrix = new Double[nObs][nReplication];
        int currentIndex = systematicIndices[0];
        Double[] currentErrors = createErrorArray(pseudoRandomNumberGenerator, 0.0, systematicStd[0], nReplication);
        for (int k = 0; k < nObs; k++) {
            if (systematicIndices[k] != currentIndex) {
                currentIndex = systematicIndices[k];
                currentErrors = createErrorArray(pseudoRandomNumberGenerator, 0.0, systematicStd[0], nReplication);
            }
            matrix[k] = currentErrors;
        }
        return matrix;
    }

    // public static Double[][][] computeUncertaintyMatrices(Double[] values, int
    // nReplication, Double[] nonSystematicStd,
    // Double[] systematicStd, int[] systematicIndices) {

    // Double[][] nonSystematicErrors =
    // computeNonSystematicErrorsMatrix(nReplication, nonSystematicStd);
    // Double[][] systematicErrors = computeSystematicErrorsMatrix(nReplication,
    // systematicStd, systematicIndices);

    // int nObs = values.length;
    // Double[][][] allMatrics = new Double[3][nObs][nReplication];
    // for (int k = 0; k < nObs; k++) {
    // for (int i = 0; i < nReplication; i++) {
    // allMatrics[0][k][i] = values[k] + nonSystematicErrors[k][i];
    // allMatrics[1][k][i] = values[k] + systematicErrors[k][i];
    // allMatrics[2][k][i] = values[k] + nonSystematicErrors[k][i] +
    // systematicErrors[k][i];
    // }
    // }
    // return allMatrics;
    // }

    public Double[][] getNonSytematicErrors(int nReplication) {
        if (!nonSystematicErrorsGenerated) {
            nonSystematicErrors = computeNonSystematicErrorsMatrix(nReplication,
                    nonSystematicStd);
            nonSystematicErrorsGenerated = true;
        }
        return nonSystematicErrors;
    }

    public Double[][] getSytematicErrors(int nReplication) {
        if (!systematicErrorsGenerated) {
            systematicErrors = computeSystematicErrorsMatrix(nReplication, systematicStd,
                    systematicIndices);
            systematicErrorsGenerated = true;
        }
        return systematicErrors;
    }

    public String getName() {
        return name;
    }

    public Double[] getValues() {
        return values;
    }

}
