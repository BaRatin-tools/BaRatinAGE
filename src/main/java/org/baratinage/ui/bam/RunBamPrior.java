package org.baratinage.ui.bam;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.nio.file.Files;
import java.nio.file.Path;
// import java.util.UUID;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipOutputStream;

// import org.baratinage.App;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
// import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
// import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.ui.commons.DefaultStructuralErrorProvider;

public class RunBamPrior extends RunBam {

        public RunBamPrior() {
                super();
        }

        public RunBamPrior(String bamRunZipFileName) {
                super(bamRunZipFileName);
        }

        public void configure(
                        String workspace,
                        IModelDefinition modelDefinition,
                        IPriors priors,
                        PriorPredictionExperiment[] priorPredictionExperiment

        ) {

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

}
