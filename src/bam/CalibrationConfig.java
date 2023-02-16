package bam;

public class CalibrationConfig {
    private Model model;
    private ModelOutput[] modelOutputs;
    private CalibrationData calibrationData;
    private McmcConfig mcmcConfig;
    private McmcCookingConfig mcmcCookingConfig;
    private McmcSummaryConfig mcmcSummaryConfig;
    private CalDataResidualConfig calDataResidualConfig;

    public CalibrationConfig(Model model,
            ModelOutput[] modelOutputs,
            CalibrationData calibrationData,
            McmcConfig mcmcConfig,
            McmcCookingConfig mcmcCookingConfig,
            McmcSummaryConfig mcmcSummaryConfig,
            CalDataResidualConfig calDataResidualConfig) {
        this.model = model;
        this.modelOutputs = modelOutputs;
        this.calibrationData = calibrationData;
        this.mcmcConfig = mcmcConfig;
        this.mcmcCookingConfig = mcmcCookingConfig;
        this.mcmcSummaryConfig = mcmcSummaryConfig;
        this.calDataResidualConfig = calDataResidualConfig;
    }

    public String[] getStructErrConfNames() {
        int n = this.modelOutputs.length;
        String[] structErrConfNames = new String[n];
        for (int k = 0; k < n; k++) {
            structErrConfNames[k] = this.modelOutputs[k].getStructErrConfName();
        }
        return structErrConfNames;
    }

    public void writeConfig(String workspace) {
        this.mcmcConfig.writeConfig(workspace);
        this.mcmcCookingConfig.writeConfig(workspace);
        this.mcmcSummaryConfig.writeConfig(workspace);
        this.model.writeConfig(workspace);
        for (ModelOutput mo : this.modelOutputs) {
            mo.writeConfig(workspace);
        }
        this.calibrationData.writeConfig(workspace);
        this.calDataResidualConfig.writeConfig(workspace);
    }

    public void log() {
        System.out.println("Calibration config: ");
        this.model.log();
        for (ModelOutput mo : this.modelOutputs) {
            mo.log();
        }
        this.calibrationData.log();
        this.mcmcConfig.log();
        this.mcmcCookingConfig.log();
        this.mcmcSummaryConfig.log();
        this.calDataResidualConfig.log();
    }
}
