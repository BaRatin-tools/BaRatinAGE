package org.baratinage.ui.bam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.baratinage.App;
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
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.utils.Misc;
import org.baratinage.utils.ReadWriteZip;

import org.baratinage.ui.commons.DefaultStructuralErrorProvider;

public class RunBam {

    // FIXME: should be stored at the much higher level (Project or App level)?
    // FIXME: workspace should be handled here and be named according to date/time
    // Ideas about workspace:
    // - a parent folder (e.g.named "bam_workspace") contains all current bam run
    // - an actual bam workspace should be unique to each bam run
    // - when closing a project all associated workspaces should be deleted
    // - when importing a project all associated workspaces should be re-created.

    public final String uuid;
    public final Path workspace;
    public final BaM bam;

    // private final String bamRunZipFileName;
    // private final Path runZipFile;

    // private BaM bam;
    // private Path workspace;
    private boolean isConfigured = false;
    private boolean hasResults = false;

    public RunBam(IModelDefinition modelDefinition,
            IPriors priors,
            IStructuralError structuralError,
            ICalibrationData calibrationData,
            IPredictionExperiment[] predictionExperiments) {

        this(
                Misc.getTimeStamp() + "_" + UUID.randomUUID().toString().substring(0, 5),
                modelDefinition, priors, structuralError, calibrationData, predictionExperiments);

    }

    public RunBam(
            String id,
            IModelDefinition modelDefinition,
            IPriors priors,
            IStructuralError structuralError,
            ICalibrationData calibrationData,
            IPredictionExperiment[] predictionExperiments) {

        if (modelDefinition == null) {
            throw new IllegalArgumentException("'modelDefinition' must non null!");
        }
        if (priors == null) {
            throw new IllegalArgumentException("'priors' must non null!");
        }

        uuid = id;
        workspace = Path.of(App.BAM_WORKSPACE, uuid);

        if (!workspace.toFile().exists()) {
            workspace.toFile().mkdir();
        }

        // create BaM object

        // 1) model

        String xTra = modelDefinition.getXtra(workspace.toString());

        Parameter[] parameters = priors.getParameters();

        String[] inputNames = modelDefinition.getInputNames();
        String[] outputNames = modelDefinition.getOutputNames();

        Model model = new Model(
                modelDefinition.getModelId(),
                inputNames.length,
                outputNames.length,
                parameters,
                xTra);

        // 2) strucutral error
        // FIXME currently supporting a single error model for all model outputs
        if (structuralError == null) {
            structuralError = new DefaultStructuralErrorProvider(
                    DefaultStructuralErrorProvider.TYPE.LINEAR);
        }
        StructuralErrorModel structErrorModel = structuralError.getStructuralErrorModel();
        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(outputNames[k], structErrorModel);
        }

        // 3) calibration data

        CalibrationData calibData;

        if (calibrationData == null) {
            double[] fakeDataArray = new double[] { 0 };
            UncertainData[] inputs = new UncertainData[inputNames.length];
            for (int k = 0; k < inputNames.length; k++) {
                inputs[k] = new UncertainData(inputNames[k], fakeDataArray);
            }
            UncertainData[] outputs = new UncertainData[inputNames.length];
            for (int k = 0; k < outputNames.length; k++) {
                outputs[k] = new UncertainData(outputNames[k], fakeDataArray);
            }

            calibData = new CalibrationData("fakeCalibrationData",
                    inputs, outputs);

        } else {
            calibData = calibrationData.getCalibrationData();

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

        // 4) predictions

        PredictionConfig[] predConfigs = new PredictionConfig[predictionExperiments.length];
        for (int k = 0; k < predictionExperiments.length; k++) {
            predConfigs[k] = predictionExperiments[k].getPredictionConfig();
        }

        // 5) run options

        RunOptions runOptions = new RunOptions(
                true,
                true,
                true,
                true);

        // 6) BaM

        bam = new BaM(calibrationConfig, predConfigs, runOptions, null, null);
    };

    public void run() {

        try {
            bam.run(workspace.toString(), txt -> {
                System.out.println("log => " + txt);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        readResultsFromWorkspace();

        ReadWriteZip.zip(Path.of(App.TEMP_DIR, getBamRunZipName()).toString(), workspace.toString());

    }

    public String getBamRunZipName() {
        return uuid + ".zip";
    }

    public void readResultsFromWorkspace() {
        bam.readResults(workspace.toString());
        hasResults = true;
    }

    public BaM getBaM() {
        return bam;
    }

    public CalibrationResult getCalibrationResult() {
        if (!hasResults)
            return null;
        return bam.getCalibrationResults();
    }

    public PredictionResult[] getPredictionResults() {
        if (!hasResults)
            return null;
        return bam.getPredictionResults();
    }
}
