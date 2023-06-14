package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;

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
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.ui.plot.PlotBand;

public class PriorRatingCurve extends GridPanel {

        private RowColPanel plotPanel;

        private IPredictionData predictionDataProvider;
        private IPriors priorsProvider;
        private IModelDefinition modelDefinitionProvider;

        private PredictionResult[] predictionResults;
        private boolean hasResults = false;

        private RunBamPrior runBamPrior;
        private JLabel outdatedLabel;

        // FIXME: this has a few features in common with PosteriorRatingCurve
        // FIXME: refactoring may be needed; consider including RunBam class as well
        public PriorRatingCurve() {

                setPadding(5);
                setGap(5);

                setName("prior_rating_curve");

                outdatedLabel = new JLabel();
                outdatedLabel.setForeground(Color.RED);

                JButton runButton = new JButton(
                                String.format("<html>Calculer la courbe de tarage <i>a priori</i></html>"));
                runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
                runButton.addActionListener((e) -> {
                        computePriorRatingCurve();
                });

                plotPanel = new RowColPanel(RowColPanel.AXIS.COL);

                insertChild(outdatedLabel, 0, 0,
                                ANCHOR.C, FILL.BOTH);
                insertChild(runButton, 0, 1,
                                ANCHOR.C, FILL.BOTH);
                insertChild(plotPanel, 0, 2,
                                ANCHOR.C, FILL.BOTH);

                setRowWeight(2, 1);
                setColWeight(0, 1);
        }

        public void setOutdated(boolean isOutdated) {
                if (isOutdated) {
                        outdatedLabel.setText("La courbe de tarage n'est plus à jour et doit être recalculé!");
                } else {
                        outdatedLabel.setText("");
                }
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
                        firePropertyChange("bamHasRun", null, null);

                        predictionResults = runBamPrior.getPredictionResults();

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

        public String getBamRunZipFileName() {
                if (runBamPrior == null) {
                        return null;
                }
                return runBamPrior.getBamRunZipFileName();
        }

        // FIXME: general approach may be questionnable? think through. refactor?
        public void setBamRunZipFileName(String bamRunZipFileName) {

                if (bamRunZipFileName == null) {
                        System.out.println("No prior rating curve computed...");
                        return;
                }

                File targetTempDir = Path.of(App.TEMP_DIR, "unzip").toFile();
                targetTempDir.mkdir();
                System.out.println("Target dir = " + targetTempDir);
                boolean unzipSuccess = ReadWriteZip.unzip(Path.of(App.TEMP_DIR, bamRunZipFileName).toString(),
                                targetTempDir.toString());
                System.out.println("Unzip success = " + unzipSuccess);

                PriorPredictionExperiment[] ppes = new PriorPredictionExperiment[] {
                                getMaxpostPriorPredictionExperiment(),
                                getParametricUncertaintyPriorPredictionExperiment()
                };

                runBamPrior = new RunBamPrior(bamRunZipFileName);
                runBamPrior.configure(targetTempDir.toString(),
                                modelDefinitionProvider, priorsProvider, ppes);
                runBamPrior.readResultsFromWorkspace();

                PredictionResult[] pprs = runBamPrior.getPredictionResults();
                buildRatingCurvePlot(ppes[0].getPredictionConfig(), pprs[1], pprs[0]);
        }
}
