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
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.DefaultStructuralErrorModels;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.Misc;

public class RunPanel extends RowColPanel {

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

    public void hasChanged() {
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

    public void setStructuralErrorModel(IStructuralErrorModels structuralErrorModel) {
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

    public void setCalibratedModel(ICalibratedModel calibratedModel) {
        bamCalibratedModel = calibratedModel;
        // setModelDefintion(bamCalibratedModel);
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
            System.err.println(
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
            System.err.println(
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
        IPredictionExperiment[] experiments = bamPredictions.getPredictionExperiments();
        if (experiments == null) {
            return false;
        }
        for (IPredictionExperiment exp : experiments) {
            PredictionConfig predConf = exp.getPredictionConfig();
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
                nInputs = bamModelDef.getOutputNames().length;
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

        String xTra = bamModelDef.getXtra(Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id).toString());

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
            modelOutputs[k] = new ModelOutput(outputNames[k], structErrorModels[k]);
        }

        // --------------------------------------------------------------------
        // 3) calibration data

        CalibrationData calibData;

        if (bamCalibData == null) {
            if (calibRun) {
                System.out.println(
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
        BaM bam;

        IPredictionExperiment[] predExperiments = bamPredictions.getPredictionExperiments();
        PredictionConfig[] predConfigs = new PredictionConfig[predExperiments.length];
        for (int k = 0; k < predExperiments.length; k++) {
            predConfigs[k] = predExperiments[k].getPredictionConfig();
        }

        if (calibRun || priorPredRun) {
            CalibrationConfig calibConfig = bamCalibratedModel == null
                    ? buildCalibrationConfig(runId)
                    : bamCalibratedModel.getCalibrationConfig();
            bam = BaM.buildBamForCalibration(calibConfig, predConfigs);
        } else {
            if (bamCalibratedModel == null) {
                System.err.println(
                        "RunPanel: cannot run BaM for prediction only if no calibration results are provided!");
                return;
            }
            bam = BaM.buildBamForPredictions(bamCalibratedModel.getCalibrationResults(), predConfigs);
        }

        RunDialog runDialog = new RunDialog(runId, bam);
        runDialog.executeBam((RunConfigAndRes runConfigAndRes) -> {
            for (Consumer<RunConfigAndRes> l : runSuccessListeners) {
                l.accept(runConfigAndRes);
            }
        });

    }

    private List<Consumer<RunConfigAndRes>> runSuccessListeners = new ArrayList<>();

    public void addRunSuccessListerner(Consumer<RunConfigAndRes> l) {
        runSuccessListeners.add(l);
    }

    public void removeRunSuccessListerner(Consumer<RunConfigAndRes> l) {
        runSuccessListeners.remove(l);
    }
}
