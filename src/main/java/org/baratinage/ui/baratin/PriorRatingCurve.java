package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.App;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.Distribution.DISTRIB;

import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PriorPredictionExperiment;
// import org.baratinage.ui.bam.RunBamPrior;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.commons.WarningAndActions;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

import org.baratinage.utils.ReadWriteZip;

public class PriorRatingCurve extends GridPanel {

        private RowColPanel plotPanel;

        private IPredictionData predictionDataProvider;
        private IPriors priorsProvider;
        private IModelDefinition modelDefinitionProvider;

        private PriorPredictionExperiment[] predictionConfigs;
        private PredictionResult[] predictionResults;
        private boolean hasResults = false;

        // private RunBamPrior runBamPrior;
        private RunBam runBam;
        private RowColPanel outdatedPanel;

        // FIXME: this has a few features in common with PosteriorRatingCurve
        // FIXME: refactoring may be needed; consider including RunBam class as well
        public PriorRatingCurve() {

                setPadding(5);
                setGap(5);

                outdatedPanel = new RowColPanel();

                JButton runButton = new JButton(
                                String.format("<html>Calculer la courbe de tarage <i>a priori</i></html>"));
                runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
                runButton.addActionListener((e) -> {
                        computePriorRatingCurve();
                });

                plotPanel = new RowColPanel(RowColPanel.AXIS.COL);

                insertChild(outdatedPanel, 0, 0,
                                ANCHOR.C, FILL.BOTH);
                insertChild(runButton, 0, 1,
                                ANCHOR.C, FILL.BOTH);
                insertChild(plotPanel, 0, 2,
                                ANCHOR.C, FILL.BOTH);

                setRowWeight(2, 1);
                setColWeight(0, 1);
        }

        @Deprecated
        public void setWarnings(WarningAndActions[] warnings) {
                outdatedPanel.clear();
                for (WarningAndActions w : warnings) {
                        outdatedPanel.appendChild(w);
                }
                outdatedPanel.updateUI();
        }

        private void computePriorRatingCurve() {
                try {

                        buildPriorPredictionExperiments();

                        runBam = new RunBam(modelDefinitionProvider,
                                        priorsProvider,
                                        null, null,
                                        predictionConfigs);

                        // runBam.configure(
                        // App.BAM_RUN_DIR,
                        // modelDefinitionProvider,
                        // priorsProvider,
                        // predictionConfigs);

                        runBam.run();
                        firePropertyChange("bamHasRun", null, null);

                        predictionResults = runBam.getPredictionResults();
                        hasResults = true;

                        buildRatingCurvePlot();

                } catch (Exception error) {
                        System.err.println("ERROR: An error occured while running BaM!");
                        error.printStackTrace();
                }
        }

        private void buildRatingCurvePlot() {
                if (runBam == null || priorsProvider == null || predictionConfigs == null) {
                        return;
                }

                Parameter[] params = priorsProvider.getParameters();
                System.out.println(params);

                List<double[]> transitionStages = new ArrayList<>();
                for (Parameter p : params) {
                        if (p.getName().startsWith("k_")) {
                                Distribution d = p.getDistribution();
                                if (d.getDistrib() == DISTRIB.GAUSSIAN) {
                                        double[] distParams = d.getParameterValues();
                                        double mean = distParams[0];
                                        double std = distParams[1];
                                        transitionStages.add(new double[] {
                                                        mean, mean - 2 * std, mean + 2 * std
                                        });
                                }

                        }
                }

                PredictionResult[] pprs = runBam.getPredictionResults();
                buildRatingCurvePlot(predictionConfigs[0].getPredictionConfig(), pprs[1], pprs[0], transitionStages);
        }

        private void buildRatingCurvePlot(
                        PredictionConfig predictionConfig,
                        PredictionResult parametricUncertainty,
                        PredictionResult maxpost,
                        List<double[]> transitionStages) {

                double[] stage = predictionConfig.getPredictionInputs()[0].getDataColumns().get(0);
                String outputName = predictionConfig.getPredictionOutputs()[0].getName();
                double[] dischargeMaxpost = maxpost.getOutputResults().get(outputName).spag().get(0);

                List<double[]> dischargeParametricEnv = parametricUncertainty.getOutputResults().get(outputName).env();

                double[] dischargeLow = dischargeParametricEnv.get(1);
                double[] dischargeHigh = dischargeParametricEnv.get(2);

                Plot plot = new Plot("Hauteur d'eau [m]", "Débit [m3/s]", true);

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

                for (int k = 0; k < transitionStages.size(); k++) {
                        double[] transitionStage = transitionStages.get(k);
                        PlotInfiniteLine line = new PlotInfiniteLine("k_" + k, transitionStage[0],
                                        Color.GREEN, 2);
                        PlotInfiniteBand band = new PlotInfiniteBand("Hauteur de transition",
                                        transitionStage[1], transitionStage[2], new Color(100, 255, 100, 100));
                        plot.addXYItem(line, false);
                        plot.addXYItem(band, k == 0);
                }

                plot.addXYItem(mp);
                plot.addXYItem(parEnv);

                PlotContainer plotContainer = new PlotContainer(plot);

                plotPanel.clear();
                plotPanel.appendChild(plotContainer);

        }

        public void setPredictionDataProvider(IPredictionData predictionDataProvider) {
                this.predictionDataProvider = predictionDataProvider;
        }

        private void buildPriorPredictionExperiments() {
                int nReplicates = 500;
                PriorPredictionExperiment ppeMaxpost = new PriorPredictionExperiment("maxpost",
                                false, nReplicates,
                                modelDefinitionProvider, predictionDataProvider);

                PriorPredictionExperiment ppeParamUncertainty = new PriorPredictionExperiment(
                                "parametricUncertainty",
                                true, nReplicates,
                                modelDefinitionProvider, predictionDataProvider);

                predictionConfigs = new PriorPredictionExperiment[] {
                                ppeMaxpost,
                                ppeParamUncertainty
                };
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

        public String getBamRunZipName() {
                if (runBam == null) {
                        return null;
                }
                return runBam.getBamRunZipName();
        }

        // FIXME: general approach may be questionnable? think through. refactor?
        public void setBamRunZipName(String bamRunZipName) {

                if (bamRunZipName == null) {
                        System.out.println("No prior rating curve computed...");
                        return;
                }

                String bamRunUUID = bamRunZipName.substring(0, bamRunZipName.indexOf(".zip"));
                File targetDir = Path.of(App.BAM_WORKSPACE, bamRunUUID).toFile();
                targetDir.mkdir();
                System.out.println("Target dir = " + targetDir);
                boolean unzipSuccess = ReadWriteZip.unzip(Path.of(App.TEMP_DIR, bamRunZipName).toString(),
                                targetDir.toString());
                System.out.println("Unzip success = " + unzipSuccess);

                buildPriorPredictionExperiments();

                runBam = new RunBam(bamRunUUID,
                                modelDefinitionProvider, priorsProvider,
                                null, null,
                                predictionConfigs);
                // runBam.configure(targetTempDir.toString(),
                // modelDefinitionProvider, priorsProvider, predictionConfigs);
                runBam.readResultsFromWorkspace();

                hasResults = true;

                buildRatingCurvePlot();
        }
}
