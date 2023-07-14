package org.baratinage.ui.bam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.baratinage.App;
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

    public final String id;
    public final Path workspacePath;
    public final Path zipPath;
    public final String zipName;
    public final BaM bam;

    public RunBam(String id) {
        this.id = id;
        workspacePath = Path.of(App.BAM_WORKSPACE, id);
        zipName = id + ".zip";
        zipPath = Path.of(App.TEMP_DIR, zipName);

        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());

        File mainConfigFile = Path.of(workspacePath.toString(), BamFilesHelpers.CONFIG_BAM).toFile();
        mainConfigFile.renameTo(Path.of(BamFilesHelpers.EXE_DIR, BamFilesHelpers.CONFIG_BAM).toFile());

        bam = BaM.readBaM(mainConfigFile.getAbsolutePath());
        bam.readResults(workspacePath.toString());
        System.out.println(bam);
    }

    public RunBam(
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

        id = Misc.getTimeStampedId();
        workspacePath = Path.of(App.BAM_WORKSPACE, id);
        zipName = id + ".zip";
        zipPath = Path.of(App.TEMP_DIR, zipName);

        if (!workspacePath.toFile().exists()) {
            workspacePath.toFile().mkdir();
        }

        // create BaM object

        // 1) model

        String xTra = modelDefinition.getXtra(workspacePath.toString());

        Parameter[] parameters = priors.getParameters();

        String[] inputNames = modelDefinition.getInputNames();
        String[] outputNames = modelDefinition.getOutputNames();

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                modelDefinition.getModelId(),
                inputNames.length,
                outputNames.length,
                parameters,
                xTra,
                BamFilesHelpers.CONFIG_XTRA);

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

            String dataName = "fakeCalibrationData";
            calibData = new CalibrationData(
                    dataName,
                    BamFilesHelpers.CONFIG_CALIBRATION,
                    String.format(BamFilesHelpers.DATA_CALIBRATION, dataName),
                    inputs,
                    outputs);

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
                BamFilesHelpers.CONFIG_RUN_OPTIONS,
                true,
                true,
                true,
                true);

        // 6) BaM

        bam = new BaM(calibrationConfig, predConfigs, runOptions);
    };

    public void run() {

        try {
            bam.run(workspacePath.toString(), txt -> {
                System.out.println("log => " + txt);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        readResultsFromWorkspace();

        // FIXME: inefficient but safer (caller classer doesn't need to call it)
        zipBamRun();

    }

    private void readResultsFromWorkspace() {
        bam.readResults(workspacePath.toString());
    }

    public boolean hasResults() {
        return !(bam.getCalibrationResults() == null && bam.getPredictionResults() == null);
    }

    public void zipBamRun() {
        String mainConfigFilePath = Path.of(BamFilesHelpers.EXE_DIR, BamFilesHelpers.CONFIG_BAM).toString();
        ReadWriteZip.flatZip(zipPath.toString(), workspacePath.toString(), mainConfigFilePath);
    }

    public void unzipBamRun() {
        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());
        readResultsFromWorkspace();
    }

}
