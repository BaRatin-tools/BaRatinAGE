package moteur;

public class Prediction {
    private String name;
    private PredictionInputVariable[] predictionInputVariable;
    private OutputVariable[] outputVariables;
    // private int nReplication;
    // private boolean includeInputNonSystematicErrors;
    // private boolean includeInputSystematicErrors;
    private boolean includeParametricUncertainty;

    public Prediction(
            String name,
            PredictionInputVariable[] predictionInputVariable,
            OutputVariable[] outputVariables,
            boolean includeParametricUncertainty) {
        this.name = name;
        this.predictionInputVariable = predictionInputVariable;
        this.outputVariables = outputVariables;
        this.includeParametricUncertainty = includeParametricUncertainty;
    }

    public Prediction(String name, PredictionInputVariable[] predictionInputVariable,
            OutputVariable[] outputVariables) {
        this(name, predictionInputVariable, outputVariables, false);
    }

    public String getName() {
        return name;
    }

    public PredictionInputVariable[] getPredictionInputVariables() {
        return predictionInputVariable;
    }

    public OutputVariable[] getOutputVariables() {
        return outputVariables;
    }

    public boolean includeParametricUncertainty() {
        return includeParametricUncertainty;
    }

}
