package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.Distribution.DISTRIB;

import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PriorPredictionExperiment;
import org.baratinage.ui.bam.RunBam;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

// FIXME: this class may still be a bit too unnecessarily complicated: simplify?
public class PriorRatingCurve extends GridPanel {

        public final JButton runButton;

        private RowColPanel plotPanel;

        private IPredictionData predictionDataProvider;
        private IPriors priorsProvider;
        private IModelDefinition modelDefinitionProvider;

        private PriorPredictionExperiment[] predictionConfigs;
        private PredictionResult[] predictionResults;

        private RunBam runBam;
        private RowColPanel outdatedPanel;

        public PriorRatingCurve() {

                setPadding(5);
                setGap(5);

                outdatedPanel = new RowColPanel();

                runButton = new JButton();
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

        private void computePriorRatingCurve() {
                try {

                        buildPriorPredictionExperiments();

                        runBam = new RunBam(modelDefinitionProvider,
                                        priorsProvider,
                                        null, null,
                                        predictionConfigs);

                        runBam.run();

                        if (runBam.hasResults()) {

                                predictionResults = runBam.bam.getPredictionResults();
                                if (predictionResults == null) {
                                        System.err.println("ERROR: no prediction results found!");
                                        return;
                                }

                                fireChangeListeners();
                                buildRatingCurvePlot();
                        }

                } catch (Exception error) {
                        System.err.println("ERROR: An error occured while running BaM!");
                        error.printStackTrace();
                }
        }

        private void buildRatingCurvePlot() {
                if (runBam == null) {
                        return;
                }

                Parameter[] params = runBam.bam.calibrationConfig.model.parameters;

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

                PredictionResult[] pprs = runBam.bam.getPredictionResults();
                if (pprs == null) {
                        System.err.println("ERROR: not prediction results found!");
                        return;
                }

                PredictionConfig predictionConfig = runBam.bam.predictionConfigs[0];

                buildRatingCurvePlot(predictionConfig, pprs[1], pprs[0], transitionStages);
        }

        private void buildRatingCurvePlot(
                        PredictionConfig predictionConfig,
                        PredictionResult parametricUncertainty,
                        PredictionResult maxpost,
                        List<double[]> transitionStages) {

                double[] stage = predictionConfig.inputs[0].dataColumns.get(0);
                String outputName = predictionConfig.outputs[0].name;
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

        public PredictionResult[] getPredictionResults() {
                return predictionResults;
        }

        public void setPredictionResults(PredictionResult[] predictionResults) {
                this.predictionResults = predictionResults;
        }

        public RunBam getRunBam() {
                return runBam;
        }

        public void setRunBam(String runBamId) {

                if (runBamId == null) {
                        System.out.println("No prior rating curve computed...");
                        return;
                }

                runBam = new RunBam(runBamId);

                buildRatingCurvePlot();
        }

        private final List<ChangeListener> changeListeners = new ArrayList<>();

        public void addChangeListener(ChangeListener l) {
                changeListeners.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
                changeListeners.remove(l);
        }

        public void fireChangeListeners() {
                for (ChangeListener l : changeListeners) {
                        l.stateChanged(new ChangeEvent(this));
                }
        }
}
