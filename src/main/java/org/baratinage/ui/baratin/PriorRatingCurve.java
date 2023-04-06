package org.baratinage.ui.baratin;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

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
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;

import org.baratinage.ui.bam.DefaultStructuralErrorProvider;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPriorPredictionExperiment;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.IStructuralError;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

public class PriorRatingCurve extends GridPanel implements IPriorPredictionExperiment {

        RowColPanel plotPanel;

        public PriorRatingCurve() {
                // appendChild(new JLabel("Prior rating curve"));

                JButton runButton = new JButton("Compute prior rating curve");
                runButton.addActionListener((e) -> {
                        runBaM();
                });
                insertChild(runButton, 0, 0,
                                ANCHOR.C, FILL.NONE);
                setRowWeight(1, 1);
                setColWeight(0, 1);

                setName("prior_rating_curve");

                plotPanel = new RowColPanel(RowColPanel.AXIS.COL);

                insertChild(plotPanel, 0, 1,
                                ANCHOR.C, FILL.BOTH);

        }

        private void buildRatingCurvePlot(
                        PredictionConfig predictionConfig,
                        PredictionResult parametricUncertainty,
                        PredictionResult maxpost) {

                double[] stage = predictionConfig.getPredictionInputs()[0].getDataColumns().get(0);
                String outputName = predictionConfig.getPredictionOutputs()[0].getName();
                double[] dischargeMaxpost = maxpost.getOutputResults().get(outputName).spag().get(0);

                List<double[]> dischargeParametricEnv = parametricUncertainty.getOutputResults().get(outputName).env();

                double[] dischargeLow = dischargeParametricEnv.get(1);
                double[] dischargeHigh = dischargeParametricEnv.get(2);

                Plot plot = new Plot("Stage [m]", "Discharge [m3/s]", true);

                PlotItem mp = new PlotLine(
                                "Prior rating curve",
                                stage,
                                dischargeMaxpost,
                                Color.BLACK,
                                5);

                PlotItem parEnv = new PlotBand(
                                "Prior parametric uncertainty",
                                stage,
                                dischargeLow,
                                dischargeHigh,
                                new Color(200, 200, 255, 100));

                plot.addXYItem(mp);
                plot.addXYItem(parEnv);

                PlotContainer plotContainer = new PlotContainer(plot);

                plotPanel.clear();
                plotPanel.appendChild(plotContainer);

        }

        private void runBaM() {

                String workspace = "test/newTestWS";

                String xTra = modelDefinitionProvider.getXtra(workspace);

                Parameter[] parameters = priorsProvider.getParameters();

                String[] inputNames = modelDefinitionProvider.getInputNames();
                String[] outputNames = modelDefinitionProvider.getOutputNames();

                Model model = new Model(
                                modelDefinitionProvider.getModelId(),
                                inputNames.length,
                                outputNames.length,
                                parameters,
                                xTra);

                // we can use only one default error model since prior predictions
                // often don't propagate structural errors
                StructuralErrorModel linearErrModel = structuralErrorProvider.getStructuralErrorModel();

                ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
                for (int k = 0; k < outputNames.length; k++) {
                        modelOutputs[k] = new ModelOutput(outputNames[k], linearErrModel);
                }

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
                McmcConfig mcmcConfig = new McmcConfig();
                McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
                McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();

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

                PredictionConfig predConfig = getPredictionConfig();
                PredictionConfig predConfigMaxPost = new PredictionConfig(
                                predConfig.getName() + "_maxpost",
                                predConfig.getPredictionInputs(),
                                predConfig.getPredictionOutputs(),
                                new PredictionOutput[] {},
                                false,
                                true,
                                500);

                BaM bam = new BaM(
                                fakeCalibrationConfig,
                                new PredictionConfig[] { predConfig, predConfigMaxPost },
                                runOptions,
                                null,
                                null);

                try {
                        bam.run(workspace, txt -> {
                                System.out.println("log => " + txt);
                        });
                } catch (IOException e) {
                        e.printStackTrace();
                }

                bam.readResults(workspace);

                PredictionResult[] predRes = bam.getPredictionsResults();

                // CalibrationResult calRes = bam.getCalibrationResults();

                buildRatingCurvePlot(predConfig, predRes[0], predRes[1]);

        }

        private IPredictionData predictionDataProvider;

        @Override
        public void setPredictionDataProvider(IPredictionData predictionDataProvider) {
                this.predictionDataProvider = predictionDataProvider;
        }

        @Override
        public PredictionConfig getPredictionConfig() {
                PredictionInput[] predInputs = predictionDataProvider.getPredictionInputs();

                String[] outputNames = modelDefinitionProvider.getOutputNames();
                PredictionOutput[] predOutputs = new PredictionOutput[outputNames.length];
                for (int k = 0; k < outputNames.length; k++) {
                        predOutputs[k] = new PredictionOutput(
                                        outputNames[k],
                                        false,
                                        true,
                                        true);
                }

                return new PredictionConfig(
                                getName(),
                                predInputs,
                                predOutputs,
                                new PredictionOutput[] {},
                                true,
                                true,
                                500);

        }

        private IPriors priorsProvider;

        @Override
        public void setPriorsProvider(IPriors priorsProvider) {
                this.priorsProvider = priorsProvider;
        }

        private IStructuralError structuralErrorProvider = new DefaultStructuralErrorProvider(
                        DefaultStructuralErrorProvider.TYPE.LINEAR);

        @Override
        public void setStructuralErrorProvider(IStructuralError structuralErrorProvider) {
                this.structuralErrorProvider = structuralErrorProvider;
        }

        private IModelDefinition modelDefinitionProvider;

        @Override
        public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider) {
                this.modelDefinitionProvider = modelDefinitionProvider;
        }

}
