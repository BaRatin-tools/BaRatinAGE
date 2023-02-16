package bam;

public class PredictionOutput {
    private String name;
    private boolean structuralError;
    private boolean transpose;
    private boolean createEnvelop;

    public PredictionOutput(
            String name,
            boolean structuralError,
            boolean transpose,
            boolean createEnvelop) {
        this.name = name;
        this.structuralError = structuralError;
        this.transpose = transpose;
        this.createEnvelop = createEnvelop;
    }

    public String getName() {
        return this.name;
    }

    public boolean getSructuralError() {
        return this.structuralError;
    }

    public boolean getTranspose() {
        return this.transpose;
    }

    public boolean getCreateEnvelop() {
        return this.createEnvelop;
    }

    public void log() {
        System.out.println(
                String.format(
                        "Prediction output '%s' (%b, %b, %b).",
                        this.name,
                        this.structuralError,
                        this.transpose,
                        this.createEnvelop));
    }

}
