package moteur;

public class PredictionInputVariable {
    // private boolean hasNonSystematicError;
    // private boolean hasSystematicError;
    // private int nReplication;
    private String name;
    private int nObs;
    private Double[][] data;

    public PredictionInputVariable(String name, InputVariable inputVariable, int nReplication,
            boolean withNonSystematicError,
            boolean withSystematicErrors) {
        this.name = name;
        // this.hasNonSystematicError = withNonSystematicError;
        // this.hasSystematicError = withSystematicErrors;
        // this.nReplication = nReplication;
        Double[] values = inputVariable.getValues();
        nObs = values.length;

        if (nObs == 0) {
            System.err.println("Cannot have a prediction with 0 observation!");
            // throw new Exception("Cannot have a prediction with 0 observation!");
        }

        if (!withNonSystematicError && !withSystematicErrors) {
            data = new Double[nObs][1];
            for (int k = 0; k < nObs; k++) {
                data[k][0] = values[k];
            }
            return;
        }

        Double[][] nonSystematicErrors = inputVariable.getNonSytematicErrors(nReplication);
        Double[][] systematicErrors = inputVariable.getSytematicErrors(nReplication);
        Double nsErrF = withNonSystematicError ? 1.0 : 0.0;
        Double sErrF = withSystematicErrors ? 1.0 : 0.0;

        data = new Double[nObs][nReplication];
        for (int k = 0; k < nObs; k++) {
            for (int i = 0; i < nReplication; i++) {

                data[k][i] = values[k] + nonSystematicErrors[k][i] * nsErrF +
                        systematicErrors[k][i] * sErrF;
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getNobs() {
        return nObs;
    }

    public Double[][] getData() {
        return data;
    }
}
