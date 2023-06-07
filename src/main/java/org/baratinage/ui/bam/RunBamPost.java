package org.baratinage.ui.bam;

import java.nio.file.Path;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;

public class RunBamPost extends RunBam {

        public void configure(
                        String workspace,
                        IModelDefinition modelDefinition,
                        IPriors priors,
                        IStructuralError structuralError,
                        ICalibrationData calibrationData,
                        IPredictionExperiment[] predictionExperiments) {

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

                StructuralErrorModel linearErrModel = structuralError.getStructuralErrorModel();

                ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
                for (int k = 0; k < outputNames.length; k++) {
                        modelOutputs[k] = new ModelOutput(outputNames[k], linearErrModel);
                }

                CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();

                McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
                McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();
                // FIXME: a IMcmc should be an argument that provides the MCMC configuration
                McmcConfig mcmcConfig = new McmcConfig();

                CalibrationConfig fakeCalibrationConfig = new CalibrationConfig(
                                model,
                                modelOutputs,
                                calibrationData.getCalibrationData(),
                                mcmcConfig,
                                mcmcCookingConfig,
                                mcmcSummaryConfig,
                                calDataResidualConfig);

                RunOptions runOptions = new RunOptions(
                                true,
                                true,
                                true,
                                true);

                PredictionConfig[] predConfigs = new PredictionConfig[predictionExperiments.length];
                for (int k = 0; k < predictionExperiments.length; k++) {
                        predConfigs[k] = predictionExperiments[k].getPredictionConfig();
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
