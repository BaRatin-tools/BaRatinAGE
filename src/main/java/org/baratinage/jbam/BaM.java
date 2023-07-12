package org.baratinage.jbam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class BaM {

    public static final String EXE_COMMAND = BamFilesHelpers.OS.startsWith("windows")
            ? Path.of(BamFilesHelpers.EXE_DIR, String.format("%s.exe", BamFilesHelpers.EXE_NAME)).toString()
            : String.format("./%s", BamFilesHelpers.EXE_NAME);

    @FunctionalInterface
    public interface ConsoleOutputFollower {
        public void onConsoleLog(String logMessage);
    }

    private Process bamExecutionProcess;

    private CalibrationConfig calibrationConfig;
    private PredictionConfig[] predictionConfigs;
    private RunOptions runOptions;
    private CalibrationResult calibrationResult;
    private PredictionResult[] predictionResults;

    public BaM(
            CalibrationConfig calibrationConfig,
            PredictionConfig[] predictionConfigs,
            RunOptions runOptions) {

        this.calibrationConfig = calibrationConfig;
        // FIXME: should check that there's no conflicting names in the variables!
        this.predictionConfigs = predictionConfigs;
        this.runOptions = runOptions;
        // this.calibrationResult = calibrationResult;
        // this.predictionResults = predictionResults;

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

    public void readResults(String workspace) {
        if (this.runOptions.doMcmc) {
            this.calibrationResult = new CalibrationResult(workspace, calibrationConfig);
            if (!this.calibrationResult.getIsValid()) {
                this.calibrationResult = null;
            }
        }
        if (this.runOptions.doPrediction) {
            int n = this.predictionConfigs.length;
            this.predictionResults = new PredictionResult[n];
            for (int k = 0; k < n; k++) {
                this.predictionResults[k] = new PredictionResult(workspace, this.predictionConfigs[k]);
                if (!this.predictionResults[k].getIsValid()) {
                    this.predictionResults = null;
                    break;
                }
            }
        }
    }

    public void toFiles(String workspace, String exeDir) {

        // FIXME: should write to files only the necessary files
        // FIXME: (e.g. no need for huge prediction input files if doPrediction is
        // false)

        // Calibration configuraiton files
        this.calibrationConfig.toFiles(workspace);

        // Prediction configuraiton files
        if (this.predictionConfigs.length > 0) { // FIXME: check if it is necessary
            ConfigFile predMasterConfig = new ConfigFile();
            predMasterConfig.addItem(this.predictionConfigs.length, "Number of prediction experiments");
            for (PredictionConfig p : this.predictionConfigs) {

                predMasterConfig.addItem(p.getPredictionConfigFileName(),
                        "Config file for experiments - an many lines as the number above", true);
                p.toFiles(workspace);
            }
            predMasterConfig.writeToFile(workspace, BamFilesHelpers.CONFIG_PREDICTION_MASTER);
        }

        // Run options configuraiton file
        this.runOptions.toFiles(workspace);

        // Main BaM configuration file
        String[] structErrConfNames = this.calibrationConfig.getStructErrConfNames();
        String absoluteWorkspace = Path.of(workspace).toAbsolutePath().toString();
        // absoluteWorkspace = String.format("%s/", absoluteWorkspace);
        String relativeWorkspace = BamFilesHelpers.relativizePath(absoluteWorkspace).toString()
                + BamFilesHelpers.OS_SEP;
        ConfigFile mainBaMconfig = new ConfigFile();
        mainBaMconfig.addItem(relativeWorkspace, "workspace", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_RUN_OPTIONS, "Config file: run options", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_MODEL, "Config file: model", true);
        // NOTE can be empty string
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_XTRA, "Config file: xtra model information", true);
        // NOTE can be empty string
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_CALIBRATION, "Config file: Data", true);
        mainBaMconfig.addItem(structErrConfNames,
                "Config file: Remnant sigma (as many files as there are output variables separated by commas)", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_MCMC, "Config file: MCMC", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_MCMC_COOKING, "Config file: cooking of MCMC samples", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_MCMC_SUMMARY, "Config file: summary of MCMC samples", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_RESIDUALS, "Config file: residual diagnostics", true);
        mainBaMconfig.addItem(BamFilesHelpers.CONFIG_PREDICTION_MASTER, " Config file: prediction experiments", true);
        mainBaMconfig.writeToFile(exeDir, BamFilesHelpers.CONFIG_BAM);
    }

    public RunOptions getRunOptions() {
        return this.runOptions;
    }

    public Process getBaMexecutionProcess() {
        return this.bamExecutionProcess;
    }

    public String run(String workspace, ConsoleOutputFollower consoleOutputFollower) throws IOException {

        // Delete work space content
        File dir = new File(workspace);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    // no recursion
                } else {
                    f.delete();
                }
            }
        }

        this.toFiles(workspace, BamFilesHelpers.EXE_DIR);

        String[] cmd = { EXE_COMMAND };
        File exeDirectory = new File(BamFilesHelpers.EXE_DIR);
        bamExecutionProcess = Runtime.getRuntime().exec(cmd, null, exeDirectory);
        InputStream inputStream = bamExecutionProcess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferReader = new BufferedReader(inputStreamReader);
        List<String> consoleLines = new ArrayList<String>();
        String currentLine = null;

        try {
            while ((currentLine = bufferReader.readLine()) != null) {
                consoleLines.add(currentLine);
                consoleOutputFollower.onConsoleLog(currentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exitcode = -1;
        try {
            exitcode = bamExecutionProcess.waitFor() * -1;
        } catch (InterruptedException e) {
            System.err.println("BAM RUN INTERRUPTED!");
        }

        if (exitcode != 0) {
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
                if (l.contains("Execution will stop")) {
                    inErrMsg = false;
                }
            }
            switch (exitcode) {
                case -1:
                    System.err.println("A FATAL ERROR has occured");
                    break;
                case -2:
                    System.err.println("A FATAL ERROR has occured while opening the following file.");
                    break;
                case -3:
                    System.err.println("A FATAL ERROR has occured while reading a config file.");
                    break;
                case -4:
                    System.err.println("A FATAL ERROR has occured while generating the prior model.");
                    break;
                case -5:
                    System.err.println("A FATAL ERROR has occured while fitting the model..");
                    break;
                case -6:
                    System.err.println("A FATAL ERROR has occured while post-processing MCMC samples.");
                    break;
                case -7:
                    System.err.println("A FATAL ERROR has occured while propagating uncertainty.");
                    break;
                case -8:
                    System.err.println("A FATAL ERROR has occured while writting to a file.");
                    break;
                default:
                    System.err.printf("An unknown FATAL ERROR has occured. Exit Code=%d\n", exitcode);
                    break;
            }

            return String.join("\n", errMsg);
        }
        bamExecutionProcess = null;
        return "";
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

    static public BaM readBaM(String mainConfigFilePath) {
        ConfigFile configFile = ConfigFile.readConfigFile(mainConfigFilePath);
        String relativeWorkspcePath = configFile.getString(0);
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

        String absoluteWorkspacePath = BamFilesHelpers.absolutizePath(relativeWorkspcePath).toString();
        CalibrationConfig calibrationConfig = CalibrationConfig.readCalibrationConfig(
                absoluteWorkspacePath,
                modelFileName,
                xTraFileName,
                dataFileName,
                structuralErrorFileNames,
                mcmcFileName,
                mcmcCookingFileName,
                mcmcSummaryFileName,
                dataResidualFileName);

        RunOptions runOptions = RunOptions.readRunOptions(absoluteWorkspacePath, runOptionFileName);

        System.out.println(calibrationConfig);
        System.out.println(runOptions);
        System.out.println();

        ConfigFile predMasterConfig = ConfigFile.readConfigFile(absoluteWorkspacePath, predictionFileName);
        int nPred = predMasterConfig.getInt(0);
        PredictionConfig[] predictionConfigs = new PredictionConfig[nPred];
        for (int k = 0; k < nPred; k++) {
            String predictionConfigFileName = predMasterConfig.getString(k + 1);
            predictionConfigs[k] = PredictionConfig.readPredictionConfig(absoluteWorkspacePath,
                    predictionConfigFileName);
        }

        return new BaM(calibrationConfig, predictionConfigs, runOptions);
    }
}
