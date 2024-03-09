package org.baratinage.ui.bam;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.BaM.BamRunException;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.Monitoring;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.DefaultStructuralErrorModels;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.DirUtils;

public class RunBam {

    private IModelDefinition bamModelDef;
    private IPriors bamPriors;
    private IStructuralErrorModels bamStructError;
    private ICalibrationData bamCalibData;
    private IPredictionMaster bamPredictions;
    private ICalibratedModel bamCalibratedModel;

    private final boolean calibRun;
    private final boolean priorPredRun;
    private final boolean postPredRun;

    public final JButton runButton = new JButton();

    public String description = null;

    public RunBam(boolean calibRun, boolean priorPredRun, boolean postPredRun) {
        this.calibRun = calibRun;
        this.priorPredRun = priorPredRun;
        this.postPredRun = postPredRun;

        runButton.addActionListener((e) -> {
            if (canRun()) {
                run();
            } else {
                popupInvalidConfigurationError();
            }
        });
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
    }

    private void popupInvalidConfigurationError() {
        JOptionPane.showOptionDialog(AppSetup.MAIN_FRAME,
                T.text("cannot_run_invalid_configuration"),
                T.text("error"),
                JOptionPane.OK_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] { T.text("ok") },
                "");
    }

    public void setModelDefintion(IModelDefinition modelDefinition) {
        bamModelDef = modelDefinition;
    }

    public void setPriors(IPriors priors) {
        bamPriors = priors;
    }

    public void setStructuralErrorModel(IStructuralErrorModels structuralErrorModel) {
        bamStructError = structuralErrorModel;
    }

    public void setCalibrationData(ICalibrationData calibrationData) {
        bamCalibData = calibrationData;
    }

    public void setPredictionExperiments(IPredictionMaster predictionExperiments) {
        bamPredictions = predictionExperiments;
    }

    public void setCalibratedModel(ICalibratedModel calibratedModel) {
        bamCalibratedModel = calibratedModel;
    }

    public boolean canRun() {
        try {
            boolean calibOk = true;
            if (calibRun) {
                calibOk = canRunCalibration();
            }
            boolean priorOk = true;
            if (priorPredRun) {
                priorOk = canRunPriorPrediction();
            }
            boolean postOk = true;
            if (postPredRun) {
                postOk = canRunPostPrediction();
            }
            return calibOk && priorOk && postOk;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isBamModelDefValid() {
        if (bamModelDef == null) {
            return false;
        }
        String[] parNames = bamModelDef.getParameterNames();
        if (parNames == null) {
            return false;
        }
        String[] outputNames = bamModelDef.getOutputNames();
        if (outputNames == null) {
            return false;
        }
        String[] inputNames = bamModelDef.getInputNames();
        if (inputNames == null) {
            return false;
        }
        return true;
    }

    private boolean isBamPriorsValid() {
        // bamModelDef must be check beforhand
        if (bamPriors == null) {
            return false;
        }
        Parameter[] pars = bamPriors.getParameters();
        if (pars == null) {
            return false;
        }
        String[] parNames = bamModelDef.getParameterNames();
        if (parNames.length != pars.length) {
            ConsoleLogger.error(
                    "RunPanel Error: number of parameters of bamPriors doesn't match expected number of parameters");
            return false;
        }
        return true;
    }

    private boolean isBamStructErrorValid() {
        // bamModelDef must be check beforhand
        if (bamStructError == null) {
            return false;
        }
        StructuralErrorModel[] strucErrorModels = bamStructError.getStructuralErrorModels();
        if (strucErrorModels == null) {
            return false;
        }
        String[] outputNames = bamModelDef.getOutputNames();
        if (outputNames.length != strucErrorModels.length) {
            ConsoleLogger.error(
                    "RunPanel Error: number of structural error models doesn't match the number of outputs");
            return false;
        }
        return true;
    }

    private boolean isBamCalibDataValid() {
        // bamModelDef must be check beforhand
        if (bamCalibData == null || bamModelDef == null) {
            return false;
        }
        UncertainData[] inputs = bamCalibData.getInputs();
        if (inputs == null) {
            return false;
        }
        UncertainData[] outputs = bamCalibData.getInputs();
        if (outputs == null) {
            return false;
        }
        String[] inputNames = bamModelDef.getInputNames();
        if (inputs.length != inputNames.length) {
            return false;
        }
        String[] outputNames = bamModelDef.getOutputNames();
        if (outputs.length != outputNames.length) {
            return false;
        }
        return true;
    }

    private boolean isBamPredictionValid(int nInputs, int nOutputs) {
        if (bamPredictions == null) {
            return false;
        }
        PredExpSet experiments = bamPredictions.getPredExps();
        if (experiments == null) {
            return false;
        }
        for (PredictionConfig predConf : experiments.getPredictionConfigs()) {
            if (predConf.inputs == null) {
                return false;
            }
            if (predConf.inputs.length != nInputs) {
                return false;
            }
            if (predConf.outputs == null) {
                return false;
            }
            if (predConf.outputs.length != nOutputs) {
                return false;
            }
        }
        return true;
    }

    private boolean isBamCalibratedModelValid() {
        if (bamCalibratedModel == null) {
            return false;
        }
        CalibrationConfig calConfig = bamCalibratedModel.getCalibrationConfig();
        if (calConfig == null) {
            return false;
        }
        if (calConfig.model == null) {
            return false;
        }
        // even if not needed, a calibrated model should be able to provide results
        CalibrationResult calResult = bamCalibratedModel.getCalibrationResults();
        if (calResult == null) {
            return false;
        }
        return true;
    }

    public boolean canRunCalibration() {
        return isBamModelDefValid() &&
                isBamPriorsValid() &&
                isBamStructErrorValid() &&
                isBamCalibDataValid();
    }

    public boolean canRunPriorPrediction() {
        int nInputs;
        int nOutputs;
        if (isBamCalibratedModelValid()) {
            nInputs = bamCalibratedModel.getCalibrationConfig().model.nInput;
            nOutputs = bamCalibratedModel.getCalibrationConfig().model.nInput;
        } else {
            if (isBamModelDefValid() && isBamPriorsValid()) {
                nInputs = bamModelDef.getInputNames().length;
                nOutputs = bamModelDef.getOutputNames().length;
            } else {
                return false;
            }
        }
        if (!isBamPredictionValid(nInputs, nOutputs)) {
            return false;
        }
        return true;
    }

    public boolean canRunPostPrediction() {

        int nInputs = -1;
        int nOutputs = -1;
        if (isBamCalibratedModelValid()) {
            nInputs = bamCalibratedModel.getCalibrationConfig().model.nInput;
            nOutputs = bamCalibratedModel.getCalibrationConfig().model.nInput;
        } else {
            if (canRunCalibration()) {
                nInputs = bamModelDef.getInputNames().length;
                nOutputs = bamModelDef.getOutputNames().length;
            } else {
                return false;
            }
        }
        if (!isBamPredictionValid(nInputs, nOutputs)) {
            return false;
        }
        return true;
    }

    private CalibrationConfig buildCalibrationConfig(String id) {

        // --------------------------------------------------------------------
        // 1) model

        String xTra = bamModelDef.getXtra(Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, id).toString());

        Parameter[] parameters = bamPriors.getParameters();

        String[] inputNames = bamModelDef.getInputNames();
        String[] outputNames = bamModelDef.getOutputNames();

        int nInputs = inputNames.length;
        int nOutputs = outputNames.length;

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                bamModelDef.getModelId(),
                nInputs,
                nOutputs,
                parameters,
                xTra,
                BamFilesHelpers.CONFIG_XTRA);

        // --------------------------------------------------------------------
        // 2) strucutral error
        // FIXME currently supporting a single error model for all model outputs

        if (bamStructError == null) {
            bamStructError = new DefaultStructuralErrorModels(nOutputs);
        }
        StructuralErrorModel[] structErrorModels = bamStructError.getStructuralErrorModels();
        ModelOutput[] modelOutputs = new ModelOutput[nOutputs];
        for (int k = 0; k < nOutputs; k++) {
            modelOutputs[k] = new ModelOutput(k, structErrorModels[k]);
        }

        // --------------------------------------------------------------------
        // 3) calibration data

        CalibrationData calibData;

        if (bamCalibData == null) {
            if (calibRun) {
                ConsoleLogger.log(
                        "RunPanel: if calibRun is true, calibration data should be specified! Using fake data instead...");
            }
            double[] fakeDataArray = new double[] { 0 };
            UncertainData[] inputs = new UncertainData[nInputs];
            for (int k = 0; k < nInputs; k++) {
                inputs[k] = new UncertainData(inputNames[k], fakeDataArray);
            }
            UncertainData[] outputs = new UncertainData[nOutputs];
            for (int k = 0; k < nOutputs; k++) {
                outputs[k] = new UncertainData(outputNames[k], fakeDataArray);
            }

            String dataName = "fakeCalibrationData";
            calibData = new CalibrationData(
                    dataName,
                    BamFilesHelpers.CONFIG_CALIBRATION,
                    String.format(BamFilesHelpers.DATA_CALIBRATION, dataName),
                    inputs,
                    outputs);

        } else {
            calibData = bamCalibData.getCalibrationData();

        }

        CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();

        McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
        McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();

        // FIXME: a IMcmc should be an argument that provides the MCMC configuration
        McmcConfig mcmcConfig = new McmcConfig();

        return new CalibrationConfig(
                model,
                modelOutputs,
                calibData,
                mcmcConfig,
                mcmcCookingConfig,
                mcmcSummaryConfig,
                calDataResidualConfig);
    }

    private void run() {

        String runId = Misc.getTimeStampedId();

        BaM bam = getBaM(runId);

        if (bam != null) {
            RunDialog runDialog = new RunDialog(runId, bam);
            runDialog.executeBam((RunConfigAndRes runConfigAndRes) -> {
                runOnDoneActions(runConfigAndRes);
            });
        } else {
            popupInvalidConfigurationError();
        }

    }

    private BaM getBaM(String runId) {
        BaM bam = null;
        try {
            PredExpSet predictions = bamPredictions.getPredExps();
            if (predictions == null) {
                ConsoleLogger.warn("No valid prediction set");
            }
            PredictionConfig[] predConfigs = predictions.getPredictionConfigs();

            if (calibRun || priorPredRun) {
                CalibrationConfig calibConfig = bamCalibratedModel == null
                        ? buildCalibrationConfig(runId)
                        : bamCalibratedModel.getCalibrationConfig();
                bam = BaM.buildBamForCalibration(calibConfig, predConfigs);
            } else {
                if (bamCalibratedModel == null) {
                    ConsoleLogger.error(
                            "RunPanel: cannot run BaM for prediction only if no calibration results are provided!");
                    return bam;
                }
                bam = BaM.buildBamForPredictions(bamCalibratedModel.getCalibrationResults(), predConfigs);
            }
        } catch (IllegalArgumentException e) {
            ConsoleLogger.error(e);
        }
        return bam;
    }

    private List<Consumer<RunConfigAndRes>> onDoneActions = new ArrayList<>();

    public void addOnDoneAction(Consumer<RunConfigAndRes> l) {
        onDoneActions.add(l);
    }

    public void removeOnDoneAction(Consumer<RunConfigAndRes> l) {
        onDoneActions.remove(l);
    }

    private void runOnDoneActions(RunConfigAndRes result) {
        for (Consumer<RunConfigAndRes> l : onDoneActions) {
            l.accept(result);
        }
    }

    public void runAsync(Runnable onDone, Runnable onError) {
        runAsync(
                s -> {
                },
                p -> {
                },
                onDone, onError);
    }

    public void runAsync(Consumer<Float> onProgress, Runnable onDone, Runnable onError) {
        runAsync(
                s -> {
                },
                onProgress, onDone, onError);
    }

    public void runAsync(Consumer<String> onLog, Consumer<Float> onProgress, Runnable onDone, Runnable onError) {

        if (!canRun()) {
            onError.run();
            return;
        }

        String id = Misc.getTimeStampedId();
        BaM bam = getBaM(id);

        if (bam == null) {
            popupInvalidConfigurationError();
            onError.run();
            return;
        }
        Path workspacePath = Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, id);

        DirUtils.createDir(workspacePath.toString());

        Monitoring monitoring = new Monitoring(bam, workspacePath.toString());

        SwingWorker<Void, String> runningWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                boolean success = false;
                try {
                    ConsoleLogger.log("BaM starting...");
                    bam.run(workspacePath.toString(), txt -> {
                        publish(txt);
                    });
                    success = true;
                } catch (IOException e) {
                    ConsoleLogger.error(e);
                    cancel(true);
                    onError.run();
                } catch (InterruptedException e) {
                    ConsoleLogger.error(e);
                    cancel(true);
                    onError.run();
                } catch (BamRunException e) {
                    ConsoleLogger.error(e);
                    cancel(true);
                    onError.run();
                }
                if (success) {
                    ConsoleLogger.log("BaM ran successfully!");
                } else {
                    ConsoleLogger.error("BaM encountered an error!");
                }
                return null;
            }

            @Override
            protected void process(List<String> logs) {
                for (String s : logs) {
                    ConsoleLogger.log(s);
                    onLog.accept(s);
                }
            }

            @Override
            protected void done() {
                RunConfigAndRes res = RunConfigAndRes.buildFromWorkspace(id, workspacePath);
                runOnDoneActions(res);
                onDone.run();
                ConsoleLogger.log("BaM done...");
            }

        };

        SwingWorker<Void, Void> monitoringWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                monitoring.startMonitoring();
                return null;
            }

        };

        monitoring.addMonitoringConsumer((monitoringStep) -> {

            float stepSize = 1f / (float) monitoringStep.totalSteps;

            float stepProgress = (float) monitoringStep.progress / (float) monitoringStep.total;
            float overallProgress = (float) (monitoringStep.currenStep - 1) / (float) monitoringStep.totalSteps;

            float progress = overallProgress + stepProgress * stepSize;

            onProgress.accept(progress);
        });

        runningWorker.execute();
        monitoringWorker.execute();

    }
}
