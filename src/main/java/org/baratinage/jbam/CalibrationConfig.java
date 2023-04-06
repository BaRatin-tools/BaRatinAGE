package org.baratinage.jbam;

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

    public Model getModel() {
        return this.model;
    }

    public CalibrationData getCalibrationData() {
        return this.calibrationData;
    }

    public McmcConfig getMcmcConfig() {
        return this.mcmcConfig;
    }

    public McmcCookingConfig getMcmcCookingConfig() {
        return this.mcmcCookingConfig;
    }

    public void toFiles(String workspace) {

        // MCMC configuration files
        this.mcmcConfig.toFiles(workspace);
        this.mcmcCookingConfig.toFiles(workspace);
        this.mcmcSummaryConfig.toFiles(workspace);

        // Model configuration files
        this.model.toFiles(workspace);
        for (ModelOutput mo : this.modelOutputs) {
            mo.toFiles(workspace);
        }

        // Calibration data/configuration files
        this.calibrationData.toDataFile(workspace);
        this.calibrationData.toConfigFile(workspace);
        this.calDataResidualConfig.toFiles(workspace);

    }

    @Override
    public String toString() {
        String str = "";
        str += "\n" + this.model.toString() + "\n\n";
        for (ModelOutput mo : this.modelOutputs) {
            str += mo.toString();
        }

        str += "\n" + this.calibrationData.toString() + "\n\n";
        str += this.mcmcConfig.toString();
        str += this.mcmcCookingConfig.toString();
        str += this.mcmcSummaryConfig.toString();
        str += this.calDataResidualConfig.toString();
        return str;
    }
}
