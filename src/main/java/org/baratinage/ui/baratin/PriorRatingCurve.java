package org.baratinage.ui.baratin;

import java.awt.Color;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.App;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PriorPredictionExperiment;
import org.baratinage.ui.bam.RunBamPrior;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

// public class PriorRatingCurve extends GridPanel implements IPriorPredictionExperiments {
public class PriorRatingCurve extends GridPanel {

        private RowColPanel plotPanel;

        private IPredictionData predictionDataProvider;
        private IPriors priorsProvider;
        private IModelDefinition modelDefinitionProvider;

        private PredictionResult[] predictionResults;
        private boolean hasResults = false;

        private RunBamPrior runBamPrior;

        public PriorRatingCurve(
                        IPredictionData predictionDataProvider,
                        IPriors priorsProvider,
                        IModelDefinition modelDefinitionProvider) {
                // appendChild(new JLabel("Prior rating curve"));
                setPadding(5);
                setGap(5);

                this.predictionDataProvider = predictionDataProvider;
                this.priorsProvider = priorsProvider;
                this.modelDefinitionProvider = modelDefinitionProvider;

                JButton runButton = new JButton(
                                String.format("<html>Calculer la courbe de tarage <i>a priori</i></html>"));
                runButton.addActionListener((e) -> {
                        computePriorRatingCurve();
                });
                insertChild(runButton, 0, 0,
                                ANCHOR.C, FILL.BOTH);
                setRowWeight(1, 1);
                setColWeight(0, 1);

                setName("prior_rating_curve");

                plotPanel = new RowColPanel(RowColPanel.AXIS.COL);

                insertChild(plotPanel, 0, 1,
                                ANCHOR.C, FILL.BOTH);

        }

        private void computePriorRatingCurve() {
                try {

                        PriorPredictionExperiment[] ppes = new PriorPredictionExperiment[] {
                                        getMaxpostPriorPredictionExperiment(),
                                        getParametricUncertaintyPriorPredictionExperiment()
                        };

                        runBamPrior = new RunBamPrior();

                        runBamPrior.configure(
                                        App.BAM_RUN_DIR,
                                        modelDefinitionProvider,
                                        priorsProvider,
                                        ppes);

                        runBamPrior.run();

                        PredictionResult[] predictionResults = runBamPrior.getPredictionResults();

                        PredictionConfig predConfig = ppes[0].getPredictionConfig();
                        hasResults = true;

                        buildRatingCurvePlot(
                                        predConfig,
                                        predictionResults[1],
                                        predictionResults[0]);

                } catch (Exception error) {
                        System.err.println("ERROR: An error occured while running BaM!");
                        error.printStackTrace();
                }
                firePropertyChange("bamHasRun", null, null);
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

        public void setPredictionDataProvider(IPredictionData predictionDataProvider) {
                this.predictionDataProvider = predictionDataProvider;
        }

        public PriorPredictionExperiment getMaxpostPriorPredictionExperiment() {
                PriorPredictionExperiment ppeMaxpost = new PriorPredictionExperiment(getName() + "_maxpost",
                                false, 500);
                ppeMaxpost.setModelDefintionProvider(modelDefinitionProvider);
                ppeMaxpost.setPredictionDataProvider(predictionDataProvider);
                return ppeMaxpost;
        }

        public PriorPredictionExperiment getParametricUncertaintyPriorPredictionExperiment() {
                PriorPredictionExperiment ppeParamUncertainty = new PriorPredictionExperiment(
                                getName() + "_parametricUncertainty",
                                true, 500);
                ppeParamUncertainty.setModelDefintionProvider(modelDefinitionProvider);
                ppeParamUncertainty.setPredictionDataProvider(predictionDataProvider);
                return ppeParamUncertainty;
        }

        public void setPriorsProvider(IPriors priorsProvider) {
                this.priorsProvider = priorsProvider;
        }

        public void setModelDefintionProvider(IModelDefinition modelDefinitionProvider) {
                this.modelDefinitionProvider = modelDefinitionProvider;
        }

        public boolean isPredicted() {
                return this.hasResults;
        }

        public PredictionResult[] getPredictionResults() {
                if (isPredicted()) {
                        return predictionResults;
                }
                return null;
        }

        public void setPredictionResults(PredictionResult[] predictionResults) {
                this.predictionResults = predictionResults;
                if (predictionResults != null) {
                        this.hasResults = true;
                }
        }

        public String getBamRunUUID() {
                return runBamPrior.getUUID();
        }

}
