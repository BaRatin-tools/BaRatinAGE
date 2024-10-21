package org.baratinage.jbam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.utils.ConsoleLogger;

public class BaM {

    static public BaM buildFromWorkspace(String mainConfigFilePath, String workspacePath) {
        ConsoleLogger.log("building BaM object from workspace...");
        ConsoleLogger.log("config file is '" + mainConfigFilePath + "'");
        ConsoleLogger.log("workspace is '" + workspacePath + "'");

        ConfigFile configFile = ConfigFile.readConfigFile(mainConfigFilePath);
        // String workspacePath = configFile.getString(0);
        String runOptionFileName = configFile.getString(1);
        String modelFileName = configFile.getString(2);
        String xTraFileName = configFile.getString(3);
        String dataFileName = configFile.getString(4);
        String[] structuralErrorFileNames = configFile.getStringArray(5);
        String mcmcFileName = configFile.getString(6);
        String mcmcCookingFileName = configFile.getString(7);
        String mcmcSummaryFileName = configFile.getString(8);
        String dataResidualFileName = configFile.getString(9);
        String predictionFileName = configFile.getString(10);

        CalibrationConfig calibrationConfig = CalibrationConfig.readCalibrationConfig(
                workspacePath,
                modelFileName,
                xTraFileName,
                dataFileName,
                structuralErrorFileNames,
                mcmcFileName,
                mcmcCookingFileName,
                mcmcSummaryFileName,
                dataResidualFileName);

        RunOptions runOptions = RunOptions.readRunOptions(workspacePath, runOptionFileName);

        ConfigFile predMasterConfig = ConfigFile.readConfigFile(workspacePath, predictionFileName);
        int nPred = predMasterConfig.getInt(0);
        PredictionConfig[] predictionConfigs = new PredictionConfig[nPred];
        for (int k = 0; k < nPred; k++) {
            String predictionConfigFileName = predMasterConfig.getString(k + 1);
            predictionConfigs[k] = PredictionConfig.readPredictionConfig(workspacePath,
                    predictionConfigFileName);
        }

        CalibrationResult calibrationResult = null;
        if (runOptions.doMcmc) {
            calibrationResult = new CalibrationResult(workspacePath, calibrationConfig, runOptions);
        }

        PredictionResult[] predictionResults = null;
        if (runOptions.doPrediction) {
            int n = predictionConfigs.length;
            predictionResults = new PredictionResult[n];
            for (int k = 0; k < n; k++) {
                predictionResults[k] = new PredictionResult(workspacePath, predictionConfigs[k]);
            }
        }
        return new BaM(calibrationConfig, predictionConfigs, runOptions, calibrationResult, predictionResults);
    }

    static public BaM buildBamForCalibration(CalibrationConfig calibConfig, PredictionConfig... predConfigs) {
        RunOptions runOptions = new RunOptions(
                BamFilesHelpers.CONFIG_RUN_OPTIONS,
                true,
                true,
                true,
                predConfigs.length > 0);
        return new BaM(calibConfig, predConfigs, runOptions, null, null);
    }

    static public BaM buildBamForPredictions(CalibrationResult calibrationResult, PredictionConfig... predConfigs) {
        RunOptions runOptions = new RunOptions(
                BamFilesHelpers.CONFIG_RUN_OPTIONS,
                false,
                false,
                false,
                predConfigs.length > 0);
        return new BaM(calibrationResult.calibrationConfig, predConfigs, runOptions, calibrationResult, null);
    }

    public static final String EXE_COMMAND = BamFilesHelpers.OS.startsWith("windows")
            ? Path.of(BamFilesHelpers.EXE_DIR, String.format("%s.exe", BamFilesHelpers.EXE_NAME)).toString()
            : String.format("./%s", BamFilesHelpers.EXE_NAME);

    private Process bamExecutionProcess;

    protected CalibrationConfig calibrationConfig;
    protected PredictionConfig[] predictionConfigs;
    protected RunOptions runOptions;
    protected CalibrationResult calibrationResult;
    protected PredictionResult[] predictionResults;

    protected BaM(CalibrationConfig calibrationConfig,
            PredictionConfig[] predictionConfigs,
            RunOptions runOptions,
            CalibrationResult calibrationResult,
            PredictionResult[] predictionResults) {
        this.calibrationConfig = calibrationConfig;
        // FIXME: should check that there's no conflicting names in the variables!
        this.predictionConfigs = predictionConfigs;
        this.runOptions = runOptions;
        this.calibrationResult = calibrationResult;
        this.predictionResults = predictionResults;
    }

    public CalibrationConfig getCalibrationConfig() {
        return this.calibrationConfig;
    }

    public CalibrationResult getCalibrationResults() {
        return this.calibrationResult;
    }

    public PredictionConfig[] getPredictionConfigs() {
        return this.predictionConfigs;
    }

    public PredictionResult[] getPredictionResults() {
        return this.predictionResults;
    }

    protected void toFiles(String workspace) {

        // FIXME: assuming that that these filenames are fixed which may not
        // in particular for the prediction master file
        // FIXME: looks like PredictionMaster class is needed.
        String bamMasterConfigFileName = BamFilesHelpers.CONFIG_BAM;
        String predictionMasterConfigFileName = BamFilesHelpers.CONFIG_PREDICTION_MASTER;

        // FIXME: should write to files only the necessary files
        // FIXME: (e.g. no need for huge prediction input files if doPrediction is
        // false)

        calibrationConfig.toFiles(workspace);

        if (calibrationResult != null) {
            calibrationResult.toFiles(workspace);
        } else {
            if (!runOptions.doMcmc && runOptions.doPrediction) {
                ConsoleLogger.error("doing a prediction-only run is not possible if calibrationResult is null!");
            }
        }

        if (runOptions.doPrediction) { // FIXME: if there's no prediction master file, is it ok for BaM?
            // Prediction configuraiton files
            ConfigFile predMasterConfig = new ConfigFile();

            predMasterConfig.addItem(predictionConfigs.length, "Number of prediction experiments");
            for (PredictionConfig p : predictionConfigs) {
                predMasterConfig.addItem(p.predictionConfigFileName,
                        "Config file for experiments - an many lines as the number above", true);
                p.toFiles(workspace);
            }
            predMasterConfig.writeToFile(workspace, predictionMasterConfigFileName);

        }
        // Run options configuraiton file
        runOptions.toFiles(workspace);

        // Main BaM configuration file

        String[] structErrConfNames = calibrationConfig.getStructErrConfNames();

        String absoluteWorkspace = Path.of(workspace).toAbsolutePath().toString();

        ConfigFile mainBaMconfig = new ConfigFile();
        mainBaMconfig.addItem(absoluteWorkspace + BamFilesHelpers.OS_SEP, "workspace", true);
        mainBaMconfig.addItem(runOptions.fileName, "Config file: run options", true);
        mainBaMconfig.addItem(calibrationConfig.model.fileName, "Config file: model", true);
        // NOTE can be empty string
        mainBaMconfig.addItem(calibrationConfig.model.xTraFileName, "Config file: xtra model information", true);
        // NOTE can be empty string
        mainBaMconfig.addItem(calibrationConfig.calibrationData.fileName, "Config file: Data", true);
        mainBaMconfig.addItem(structErrConfNames,
                "Config file: Remnant sigma (as many files as there are output variables separated by commas)", true);
        mainBaMconfig.addItem(calibrationConfig.mcmcConfig.fileName, "Config file: MCMC", true);
        mainBaMconfig.addItem(calibrationConfig.mcmcCookingConfig.fileName, "Config file: cooking of MCMC samples",
                true);
        mainBaMconfig.addItem(calibrationConfig.mcmcSummaryConfig.fileName, "Config file: summary of MCMC samples",
                true);
        mainBaMconfig.addItem(calibrationConfig.calDataResidualConfig.fileName, "Config file: residual diagnostics",
                true);
        mainBaMconfig.addItem(predictionMasterConfigFileName, " Config file: prediction experiments", true);
        mainBaMconfig.writeToFile(workspace, bamMasterConfigFileName);
    }

    public RunOptions getRunOptions() {
        return this.runOptions;
    }

    public Process getBaMexecutionProcess() {
        return this.bamExecutionProcess;
    }

    public static class BamRunException extends Exception {
        public BamRunException(String errMessage) {
            super(errMessage);
        }
    }

    // FIXME: refactor to use ExeRun class?
    public void run(String workspace, Consumer<String> consoleOutputFollower)
            throws BamRunException, IOException, InterruptedException {

        // Delete workspace content
        File workspaceDirFile = new File(workspace);
        if (workspaceDirFile.exists()) {
            ConsoleLogger.log("Deleting workspace content...");
            for (File f : workspaceDirFile.listFiles()) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }

        this.toFiles(workspace);

        String mainConfigFilePath = Path.of(workspace, BamFilesHelpers.CONFIG_BAM).toAbsolutePath().toString();

        String[] cmd = { EXE_COMMAND, "-cf", mainConfigFilePath };
        String cmdString = String.join(" ", cmd);
        ConsoleLogger.log("BaM run command is '" + cmdString + "'.");

        File exeDirectory = new File(BamFilesHelpers.EXE_DIR);
        bamExecutionProcess = Runtime.getRuntime().exec(cmd, null, exeDirectory);

        InputStream inputStream = bamExecutionProcess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferReader = new BufferedReader(inputStreamReader);
        List<String> consoleLines = new ArrayList<String>();
        String currentLine = null;

        while ((currentLine = bufferReader.readLine()) != null) {
            consoleLines.add(currentLine);
            consoleOutputFollower.accept(currentLine);
            ConsoleLogger.log(currentLine);
        }

        // FIXME: all cases except default have message that should be captured!
        List<String> errMsg = new ArrayList<>();
        boolean inErrMsg = false;
        for (String l : consoleLines) {
            if (l.contains("FATAL ERROR")) {
                inErrMsg = true;
            }
            if (inErrMsg) {
                errMsg.add(l);
            }
        }
        if (inErrMsg) {
            throw new BamRunException(String.join("\n", errMsg));
        }
        if (bamExecutionProcess.exitValue() != 0) {
            throw new BamRunException("BaM encountered an uncaugth Fatal Error!");
        }
        bamExecutionProcess = null;
    }

    @Override
    public String toString() {

        List<String> str = new ArrayList<>();

        str.add("*".repeat(70));
        str.add("*".repeat(3) + "  BaM  " + " ".repeat(55) + "*".repeat(5));
        str.add("*".repeat(70));

        String[] calStrings = this.calibrationConfig.toString().split("\n");
        for (String s : calStrings)
            str.add("***  " + s);

        str.add("***  ");
        str.add("*".repeat(70));
        str.add("***  ");
        for (PredictionConfig p : this.predictionConfigs) {

            String[] predStrings = p.toString().split("\n");
            for (String s : predStrings)
                str.add("***  " + s);
            str.add("***  ");
        }

        str.add("*".repeat(70));

        str.add("***  ");
        String[] runStrings = runOptions.toString().split("\n");
        for (String s : runStrings)
            str.add("***  " + s);

        str.add("***  ");
        str.add("*".repeat(70));
        str.add("*".repeat(70) + "\n");

        return String.join("\n", str);
    }

}
