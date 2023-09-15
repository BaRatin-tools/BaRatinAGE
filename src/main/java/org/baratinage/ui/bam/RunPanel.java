package org.baratinage.ui.bam;

import java.awt.Font;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.DefaultStructuralErrorModel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.Misc;

public class RunPanel extends RowColPanel {

    private IModelDefinition bamModelDef;
    private IPriors bamPriors;
    private IStructuralError bamStructError;
    private ICalibrationData bamCalibData;
    // private IPredictionExperiment[] bamPredictions = new IPredictionExperiment[]
    // {};
    private IPredictionMaster bamPredictions;
    private RunConfigAndRes bamRunConfigAndRes;

    private final boolean calibRun;
    private final boolean priorPredRun;
    private final boolean postPredRun;

    public final JButton runButton = new JButton();

    public RunPanel(boolean calibRun, boolean priorPredRun, boolean postPredRun) {
        this.calibRun = calibRun;
        this.priorPredRun = priorPredRun;
        this.postPredRun = postPredRun;

        setPadding(5);
        appendChild(runButton);

        runButton.addActionListener((e) -> {
            run();
        });

        runButton.setText(Lg.text("launch_bam"));
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));

        hasChanged();
    }

    private void hasChanged() {
        runButton.setEnabled(canRun());
    }

    public void setModelDefintion(IModelDefinition modelDefinition) {
        bamModelDef = modelDefinition;
        hasChanged();
    }

    public void setPriors(IPriors priors) {
        bamPriors = priors;
        hasChanged();
    }

    public void setStructuralErrorModel(IStructuralError structuralErrorModel) {
        bamStructError = structuralErrorModel;
        hasChanged();
    }

    public void setCalibrationData(ICalibrationData calibrationData) {
        bamCalibData = calibrationData;
        hasChanged();
    }

    public void setPredictionExperiments(IPredictionMaster predictionExperiments) {
        bamPredictions = predictionExperiments;
        hasChanged();
    }

    public void setRunConfigAndRes(RunConfigAndRes runConfigAndRes) {
        bamRunConfigAndRes = runConfigAndRes;
        hasChanged();
    }

    public boolean canRun() {
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
    }

    public boolean canRunCalibration() {
        if (bamModelDef == null || bamPriors == null || bamStructError == null || bamCalibData == null) {
            return false;
        }
        // FIXME: further checks here: model def complete, consistency with priors,
        // number of calib data, ...
        return true;
    }

    public boolean canRunPriorPrediction() {
        if (bamModelDef == null || bamPriors == null && bamPredictions != null) { // should add checks on
                                                                                  // bamRunConfigAndRes;
            return false;
        }
        return true;
    }

    public boolean canRunPostPrediction() {
        return canRunCalibration() && bamPredictions != null; // should add checks on bamRunConfigAndRes;
    }

    private void run() {

        // --------------------------------------------------------------------
        // 0) preparing workspace

        String id = Misc.getTimeStampedId();

        // --------------------------------------------------------------------
        // 1) model

        String xTra = bamModelDef.getXtra(Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id).toString());

        Parameter[] parameters = bamPriors.getParameters();

        String[] inputNames = bamModelDef.getInputNames();
        String[] outputNames = bamModelDef.getOutputNames();

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                bamModelDef.getModelId(),
                inputNames.length,
                outputNames.length,
                parameters,
                xTra,
                BamFilesHelpers.CONFIG_XTRA);

        // --------------------------------------------------------------------
        // 2) strucutral error
        // FIXME currently supporting a single error model for all model outputs
        if (bamStructError == null) {
            bamStructError = new DefaultStructuralErrorModel(
                    DefaultStructuralErrorModel.TYPE.LINEAR);
        }
        StructuralErrorModel structErrorModel = bamStructError.getStructuralErrorModel();
        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(outputNames[k], structErrorModel);
        }

        // --------------------------------------------------------------------
        // 3) calibration data

        CalibrationData calibData;

        if (bamCalibData == null) {
            double[] fakeDataArray = new double[] { 0 };
            UncertainData[] inputs = new UncertainData[inputNames.length];
            for (int k = 0; k < inputNames.length; k++) {
                inputs[k] = new UncertainData(inputNames[k], fakeDataArray);
            }
            UncertainData[] outputs = new UncertainData[inputNames.length];
            for (int k = 0; k < outputNames.length; k++) {
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

        CalibrationConfig calibrationConfig = new CalibrationConfig(
                model,
                modelOutputs,
                calibData,
                mcmcConfig,
                mcmcCookingConfig,
                mcmcSummaryConfig,
                calDataResidualConfig);

        // --------------------------------------------------------------------
        // 4) predictions

        IPredictionExperiment[] predExperiments = bamPredictions.getPredictionExperiments();
        PredictionConfig[] predConfigs = new PredictionConfig[predExperiments.length];
        for (int k = 0; k < predExperiments.length; k++) {
            predConfigs[k] = predExperiments[k].getPredictionConfig();
        }

        // --------------------------------------------------------------------
        // 5) run options

        RunOptions runOptions = new RunOptions(
                BamFilesHelpers.CONFIG_RUN_OPTIONS,
                true,
                true,
                true,
                true); // FIXME: what if there's no prediction?

        // --------------------------------------------------------------------
        // 6) BaM

        BaM bam = new BaM(calibrationConfig, predConfigs, runOptions);

        RunDialog runDialog = new RunDialog(id, bam);
        runDialog.executeBam((RunConfigAndRes runConfigAndRes) -> {
            for (Consumer<RunConfigAndRes> l : runSuccessListeners) {
                l.accept(runConfigAndRes);
            }
        });

    }

    public RunConfigAndRes getConfigAndRes() {
        return bamRunConfigAndRes;
    }

    private List<Consumer<RunConfigAndRes>> runSuccessListeners = new ArrayList<>();

    public void addRunSuccessListerner(Consumer<RunConfigAndRes> l) {
        runSuccessListeners.add(l);
    }

    public void removeRunSuccessListerner(Consumer<RunConfigAndRes> l) {
        runSuccessListeners.remove(l);
    }
}
