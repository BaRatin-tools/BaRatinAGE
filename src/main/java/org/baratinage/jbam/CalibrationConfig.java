package org.baratinage.jbam;

public class CalibrationConfig {
    public final Model model;
    public final ModelOutput[] modelOutputs;
    public final CalibrationData calibrationData;
    public final McmcConfig mcmcConfig;
    public final McmcCookingConfig mcmcCookingConfig;
    public final McmcSummaryConfig mcmcSummaryConfig;
    public final CalDataResidualConfig calDataResidualConfig;

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

    public ModelOutput[] getModelOutputs() {
        return this.modelOutputs;
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

    static public CalibrationConfig readCalibrationConfig(
            String workspace,
            String modelConfigFileName,
            String xTraConfigFileName,
            String dataConfigFileName,
            String[] structuralErrorModelFileNames,
            String mcmcConfigFileName,
            String mcmcCookingConfigFileName,
            String mcmcSummaryConfigFileName,
            String dataResidualConfigFileName) {

        Model model = Model.readModel(workspace, modelConfigFileName, xTraConfigFileName);
        int nOutput = model.nOutput;

        if (structuralErrorModelFileNames.length != nOutput) {
            System.err.println(
                    "Number of model output in Model doesn't match the number of stuctural error config file names!");
            return null;
        }

        StructuralErrorModel[] structuralErrorModels = new StructuralErrorModel[nOutput];
        ModelOutput[] modelOutputs = new ModelOutput[nOutput];
        for (int k = 0; k < nOutput; k++) {
            // FIXME: cannot infer any name from configuration files....
            String outputName = "Output_" + (k + 1);
            structuralErrorModels[k] = StructuralErrorModel.readStructuralErrorModel(
                    workspace, structuralErrorModelFileNames[k]);
            modelOutputs[k] = new ModelOutput(outputName, structuralErrorModels[k]);
        }

        CalibrationData calibrationData = CalibrationData.readCalibrationData(
                workspace,
                dataConfigFileName);
        McmcConfig mcmcConfig = McmcConfig.readMcmc(
                workspace,
                mcmcConfigFileName);
        McmcCookingConfig mcmcCookingConfig = McmcCookingConfig.readMcmcCookingConfig(
                workspace,
                mcmcCookingConfigFileName);
        McmcSummaryConfig mcmcSummaryConfig = McmcSummaryConfig.readMcmcSummaryConfig(
                workspace,
                mcmcSummaryConfigFileName);
        CalDataResidualConfig calDataResidualConfig = CalDataResidualConfig.readCalDataResidualConfig(
                workspace,
                dataResidualConfigFileName);

        return new CalibrationConfig(
                model,
                modelOutputs,
                calibrationData,
                mcmcConfig,
                mcmcCookingConfig,
                mcmcSummaryConfig,
                calDataResidualConfig);
    }
}
