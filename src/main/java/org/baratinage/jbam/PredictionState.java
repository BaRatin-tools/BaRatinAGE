package org.baratinage.jbam;

public class PredictionState {
    public final String spagFileName;
    public final String envFileName;
    public final boolean structuralError;
    public final boolean transpose;
    public final boolean createEnvelop;

    public PredictionState(
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
                "Prediction state '%s' (%b, %b, %b).",
                this.structuralError,
                this.transpose,
                this.createEnvelop);
    }
}
