package bam;

// import java.nio.file.FileSystem;
import java.nio.file.Path;

import bam.exe.ConfigFile;
import bam.exe.Run;

public class BaM {
    private CalibrationConfig calibrationConfig;
    private PredictionConfig[] predictionConfigs;
    private RunOptions runOptions;
    private ConfigFile mainBaMconfig;
    // private CalibrationResult calibrationResult;
    // private PredictionResult[] predictionResult;

    public BaM(CalibrationConfig calibrationConfig, PredictionConfig[] predictionConfigs, RunOptions runOptions) {
        this.calibrationConfig = calibrationConfig;
        this.predictionConfigs = predictionConfigs;
        this.runOptions = runOptions;
        this.mainBaMconfig = null;
    }

    public void writeConfigFiles(String workspace) {
        this.calibrationConfig.writeConfig(workspace);
        if (this.predictionConfigs.length > 0) { // FIXME: check if it is necessary
            ConfigFile predMasterConfig = new ConfigFile();
            predMasterConfig.addItem(this.predictionConfigs.length, "Number of prediction experiments");
            for (PredictionConfig p : this.predictionConfigs) {
                String configFileName = String.format(ConfigFile.CONFIG_PREDICTION, p.getName());
                predMasterConfig.addItem(configFileName,
                        "Config file for experiments - an many lines as the number above", true);
                p.writeConfig(workspace, configFileName);
            }
            predMasterConfig.writeToFile(workspace, ConfigFile.CONFIG_PREDICTION_MASTER);
        }
        this.runOptions.writeConfig(workspace);
        String[] structErrConfNames = this.calibrationConfig.getStructErrConfNames();
        String absoluteWorkspace = Path.of(workspace).toAbsolutePath().toString();
        absoluteWorkspace = String.format("%s/", absoluteWorkspace);
        mainBaMconfig = new ConfigFile();
        mainBaMconfig.addItem(absoluteWorkspace, "workspace", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_RUN_OPTIONS, "Config file: run options", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_MODEL, "Config file: model", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_XTRA, "Config file: xtra model information", true); // NOTE can be empty
                                                                                                    // string
        mainBaMconfig.addItem(ConfigFile.CONFIG_CALIBRATION, "Config file: Data", true); // NOTE can be empty string
        mainBaMconfig.addItem(structErrConfNames,
                "Config file: Remnant sigma (as many files as there are output variables separated by commas)", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_MCMC, "Config file: MCMC", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_MCMC_COOKING, "Config file: cooking of MCMC samples", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_MCMC_SUMMARY, "Config file: summary of MCMC samples", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_RESIDUALS, "Config file: residual diagnostics", true);
        mainBaMconfig.addItem(ConfigFile.CONFIG_PREDICTION_MASTER, " Config file: prediction experiments", true);
    }

    public void run() {
        if (mainBaMconfig == null) {
            System.err.println("Cannot run BaM.exe if writeConfigFiles hasn't been called before!");
            return;
        }
        Run.run(mainBaMconfig);
    }

    public void log() {

        System.out.println("*******************************************");
        System.out.println("**** BaM **********************************");
        System.out.println("*******************************************");
        this.calibrationConfig.log();
        System.out.println("*******************************************");
        for (PredictionConfig p : this.predictionConfigs) {
            p.log();
        }
        System.out.println("*******************************************");
        this.runOptions.log();
        System.out.println("*******************************************");
        System.out.println("*******************************************");
    }

}
