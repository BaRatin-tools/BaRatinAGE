package controleur;

public class OutputVarConfig {
    private String name;
    private boolean transpose;
    private boolean createEnvelopFile;
    private boolean propagateRemnantUncertainty;
    private String envFilePath;
    private String spagFilePath;
    // private boolean hasResults;
    // private Double[][] spaghetti;
    // private Double[][] envelop;

    public OutputVarConfig(String name, boolean transpose, boolean createEnvelopFile,
            boolean propagateRemnantUncertainty) {
        this.name = name;
        this.transpose = transpose;
        this.createEnvelopFile = createEnvelopFile;
        this.propagateRemnantUncertainty = propagateRemnantUncertainty;

        envFilePath = null;
        spagFilePath = null;

        // this.hasResults = false;
    }

    // public void setResults(Double[][] spaghetti, Double[][] envelop) {
    // this.spaghetti = spaghetti;
    // this.envelop = envelop;
    // this.hasResults = true;
    // }

    // public void setResults(Double[][] spaghetti) {
    // this.spaghetti = spaghetti;
    // this.hasResults = true;
    // }

    public void setEnvFilePath(String filePath) {
        envFilePath = filePath;
    }

    public String getEnvFilePath() {
        return envFilePath;
    }

    public void setSpagFilePath(String filePath) {
        spagFilePath = filePath;
    }

    public String getSpagFilePath() {
        return spagFilePath;
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

    // public boolean hasResults() {
    // return hasResults;
    // }

    // public Double[][] getSpaghetti() {
    // return spaghetti;
    // }

    // public Double[][] getEnvelop() {
    // return envelop;
    // }
}
