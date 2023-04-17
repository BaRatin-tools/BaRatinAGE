package org.baratinage.ui.bam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

public class RunBamPrior {

    // FIXME: this should be stored at the much higher level (Project or even App
    // level)
    // public static final String baratinageTempDir =
    // Path.of(System.getProperty("java.io.tmpdir"),
    // "baratinage").toString();
    // public static final String baratinageTempDir = App.TEMP_DIR;

    private final String uuid = UUID.randomUUID().toString() + ".zip";
    private final Path runZipFile = Path.of(App.TEMP_DIR, uuid);
    // private final Path tempDirPath =
    // Path.of(System.getProperty("java.io.tmpdir"),
    // "baratinage_runbackup", "TEST");
    // "baratinage_runbackup", UUID.randomUUID().toString());
    private BaM bam;
    private Path workspace = Path.of("test/newTestWS");
    private boolean isConfigured = false;
    private boolean hasResults = false;

    public void configure(
            String workspace,
            IModelDefinition modelDefinition,
            IPriors priors,
            PriorPredictionExperiment[] priorPredictionExperiment

    ) {
        // String workspace = "test/newTestWS";
        this.workspace = Path.of(workspace);

        String xTra = modelDefinition.getXtra(workspace);

        Parameter[] parameters = priors.getParameters();

        String[] inputNames = modelDefinition.getInputNames();
        String[] outputNames = modelDefinition.getOutputNames();

        Model model = new Model(
                modelDefinition.getModelId(),
                inputNames.length,
                outputNames.length,
                parameters,
                xTra);

        // we can use only one default error model since prior predictions
        // never (?) propagate structural errors
        IStructuralError structuralError = new DefaultStructuralErrorProvider(
                DefaultStructuralErrorProvider.TYPE.LINEAR);
        StructuralErrorModel linearErrModel = structuralError.getStructuralErrorModel();

        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(outputNames[k], linearErrModel);
        }

        // Creating fake calibration data to satisfy BaM requirements
        double[] fakeCalibrationData = new double[] { 0 };
        UncertainData[] inputs = new UncertainData[inputNames.length];
        for (int k = 0; k < inputNames.length; k++) {
            inputs[k] = new UncertainData(inputNames[k], fakeCalibrationData);
        }
        UncertainData[] outputs = new UncertainData[inputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            outputs[k] = new UncertainData(outputNames[k], fakeCalibrationData);
        }

        CalibrationData calibrationData = new CalibrationData("fakeCalibrationData",
                inputs, outputs);

        CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();

        McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
        McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();
        // FIXME: a IMcmc should be an argument that provides the MCMC configuration
        McmcConfig mcmcConfig = new McmcConfig();

        CalibrationConfig fakeCalibrationConfig = new CalibrationConfig(
                model,
                modelOutputs,
                calibrationData,
                mcmcConfig,
                mcmcCookingConfig,
                mcmcSummaryConfig,
                calDataResidualConfig);

        RunOptions runOptions = new RunOptions(
                true,
                true,
                true,
                true);

        PredictionConfig[] predConfigs = new PredictionConfig[priorPredictionExperiment.length];
        for (int k = 0; k < priorPredictionExperiment.length; k++) {
            predConfigs[k] = priorPredictionExperiment[k].getPredictionConfig();
        }

        bam = new BaM(
                fakeCalibrationConfig,
                predConfigs,
                runOptions,
                null,
                null);

        isConfigured = true;
    }

    public void run() {
        if (!isConfigured) {
            System.err.println("Cannot run BaM if configure() method has been called first!");
            return;
        }
        try {
            bam.run(workspace.toString(), txt -> {
                System.out.println("log => " + txt);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        bam.readResults(workspace.toString());
        hasResults = true;

        try {
            backupBamRun();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backupBamRun() throws IOException {
        // 1/ creata temporary dir

        System.out.println("App.TEMP_DIR; ==> " + App.TEMP_DIR);
        File tD = new File(App.TEMP_DIR);
        boolean created = tD.mkdirs();
        System.out.println("Created ==> " + created);

        // 2/ create zip containing BaM run files

        File zipFile = new File(runZipFile.toString());
        FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

        File[] files = workspace.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                System.out.println("File '" + f + "'.");
                ZipEntry ze = new ZipEntry(f.getName());
                zipOutStream.putNextEntry(ze);
                Files.copy(f.toPath(), zipOutStream);
            }
        }
        zipOutStream.close();
        // Filesdd
    }

    // public String getPathToZip() {
    // return runZipFile.toString();
    // }

    public String getUUID() {
        return this.uuid;
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
