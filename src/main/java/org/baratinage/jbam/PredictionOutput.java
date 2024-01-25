package org.baratinage.jbam;

public class PredictionOutput {
    public final String spagFileName;
    public final String envFileName;
    public final boolean structuralError;
    public final boolean transpose;
    public final boolean createEnvelop;

    public PredictionOutput(
            String spagFileName,
            String envFileName,
            boolean structuralError,
            boolean transpose,
            boolean createEnvelop) {
        this.spagFileName = spagFileName;
        this.envFileName = envFileName;
        this.structuralError = structuralError;
        this.transpose = transpose;
        this.createEnvelop = createEnvelop;
    }

    @Override
    public String toString() {
        return String.format(
                "Prediction output '%s' (%b, %b, %b).",
                this.spagFileName,
                this.structuralError,
                this.transpose,
                this.createEnvelop);
    }

}
