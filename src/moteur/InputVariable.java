package moteur;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import Utils.FileReadWrite;
import commons.Observation;
import controleur.InputVarConfig;

public class InputVariable {
    private String name;
    private Double[] values;

    private boolean hasUncertainty = false;
    private Double[] nonSystematicStd;
    private Double[] systematicStd;
    private int[] systematicIndices;
    private int nReplication;
    private Double[][] nonSystematicErrors;
    private Double[][] systematicErrors;
    private boolean nonSystematicErrorsGenerated;
    private boolean systematicErrorsGenerated;

    private void set(String name, Double[] values) {
        this.nonSystematicErrorsGenerated = false;
        this.systematicErrorsGenerated = false;
        this.name = name;
        this.values = values;
    }

    private void setInputUncertainty(Double[] nonSystematicStd, Double[] systematicStd,
            int[] systematicIndices, int nReplication) {
        this.hasUncertainty = true;
        this.nonSystematicStd = nonSystematicStd;
        this.systematicStd = systematicStd;
        this.systematicIndices = systematicIndices;
        this.nReplication = nReplication;
    }

    public InputVariable(String name, Double[] values, Double[] nonSystematicStd, Double[] systematicStd,
            int[] systematicIndices, int nReplication) {
        set(name, values);
        setInputUncertainty(nonSystematicStd, systematicStd, systematicIndices, nReplication);
    }

    public InputVariable(String name, Double[] values) {
        set(name, values);
    }

    public InputVariable(Limnigraph limnigraph, int nReplication) {
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
        set(limnigraph.getName(), values);
        setInputUncertainty(values, values, systematicIndices, nReplication);
    }

    private static Double[][] computeNonSystematicErrorsMatrix(int nReplication, Double[] nonSystematicStd) {
        Random pseudoRandomNumberGenerator = new Random();
        int nObs = nonSystematicStd.length;
        Double[][] matrix = new Double[nObs][nReplication];
        for (int k = 0; k < nObs; k++) {
            for (int i = 0; i < nReplication; i++) {
                matrix[k][i] = pseudoRandomNumberGenerator.nextGaussian() * nonSystematicStd[k];
            }
        }
        return matrix;
    }

    private static Double[] createErrorArray(Random pseudoRandomNumberGenerator, Double mean, Double std,
            int nReplication) {
        Double[] randomErrorArray = new Double[nReplication];
        for (int k = 0; k < nReplication; k++) {
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

    private void generateNonSytematicErrors() {
        nonSystematicErrors = computeNonSystematicErrorsMatrix(nReplication,
                nonSystematicStd);
        nonSystematicErrorsGenerated = true;
    }

    private void generateSytematicErrors() {
        systematicErrors = computeSystematicErrorsMatrix(nReplication, systematicStd,
                systematicIndices);
        systematicErrorsGenerated = true;
    }

    public String getName() {
        return name;
    }

    public Double[] getValues() {
        return values;
    }

    public int getNobs() {
        return values.length;
    }

    public InputVarConfig writeToFile(String filePath, boolean withNonSystematicError, boolean withSystematicErrors)
            throws IOException {
        if (!hasUncertainty && (withNonSystematicError | withSystematicErrors)) {
            System.err.println(
                    "Error: cannot write uncertainty matrices if uncertainties were not set when creating the InputVarialbe");
            withNonSystematicError = false;
            withSystematicErrors = false;
        }
        int nRow = values.length;
        int nCol = 1;
        if (withNonSystematicError && !nonSystematicErrorsGenerated) {
            nCol = nReplication;
            generateNonSytematicErrors();
        }
        if (withSystematicErrors && !systematicErrorsGenerated) {
            nCol = nReplication;
            generateSytematicErrors();
        }

        Double[][] data = new Double[nRow][nCol];
        if (withNonSystematicError && withSystematicErrors) {
            for (int k = 0; k < nRow; k++) {
                for (int i = 0; i < nCol; i++) {
                    data[k][i] = values[k] + nonSystematicErrors[k][i] + systematicErrors[k][i];
                }
            }
        } else if (withNonSystematicError && !withSystematicErrors) {
            for (int k = 0; k < nRow; k++) {
                for (int i = 0; i < nCol; i++) {
                    data[k][i] = values[k] + nonSystematicErrors[k][i];
                }
            }
        } else if (!withNonSystematicError && withSystematicErrors) {
            for (int k = 0; k < nRow; k++) {
                for (int i = 0; i < nCol; i++) {
                    data[k][i] = values[k] + systematicErrors[k][i];
                }
            }
        } else if (!withNonSystematicError && !withSystematicErrors) {
            for (int k = 0; k < nRow; k++) {
                for (int i = 0; i < nCol; i++) {
                    data[k][i] = values[k];
                }
            }
        }

        FileReadWrite.writeMatrix(filePath, data);

        return new InputVarConfig(filePath, nRow, nCol);
    }
}
