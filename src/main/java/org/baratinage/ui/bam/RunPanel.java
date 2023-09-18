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
    private IPredictionMaster bamPredictions;
    private ICalibratedModel bamCalibratedModel;

    // private RunConfigAndRes bamRunConfigAndRes;

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

    public boolean canRunCalibration() {
        return bamModelDef != null &&
                bamPriors != null &&
                bamStructError != null &&
                bamCalibData != null;
    }

    public boolean canRunPriorPrediction() {
        return ((bamModelDef != null && bamPriors != null) || bamCalibratedModel != null) &&
                bamPredictions != null;
    }

    public boolean canRunPostPrediction() {
        return (canRunCalibration() || bamCalibratedModel != null)
                && bamPredictions != null;
    }

    private CalibrationConfig buildCalibrationConfig(String id) {

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
            if (calibRun) {
                System.out.println(
                        "RunPanel: if calibRun is true, calibration data should be specified! Using fake data instead...");
            }
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
