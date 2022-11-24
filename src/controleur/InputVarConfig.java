package controleur;

public class InputVarConfig {
    private String filePath;
    private int nObs;
    private int nSpag;

    public InputVarConfig(String filePath, int nObs, int nSpag) {
        this.filePath = filePath;
        this.nObs = nObs;
        this.nSpag = nSpag;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getNumberOfObs() {
        return nObs;
    }

    public int getNumberOfSpag() {
        return nSpag;
    }
}
