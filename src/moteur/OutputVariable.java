package moteur;

public class OutputVariable {
    private String name;
    private boolean transpose;
    private boolean createEnvelopFile;
    private boolean propagateRemnantUncertainty;
    // private boolean printProgress;
    // private boolean doStatePrediction;

    public OutputVariable(String name, boolean transpose, boolean createEnvelopFile,
            boolean propagateRemnantUncertainty) {
        this.name = name;
        this.transpose = transpose;
        this.createEnvelopFile = createEnvelopFile;
        this.propagateRemnantUncertainty = propagateRemnantUncertainty;
    }

    public boolean shouldTranspose() {
        return transpose;
    }

    public boolean shouldCreateEnvelopFile() {
        return createEnvelopFile;
    }

    public boolean shouldPropagateRemnantUncertainty() {
        return propagateRemnantUncertainty;
    }

    public String getName() {
        return name;
    }
}
