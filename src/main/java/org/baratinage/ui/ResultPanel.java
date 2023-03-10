package org.baratinage.ui;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.container.FlexPanel;
import org.baratinage.ui.lg.Lg;

import java.awt.Font;

public class ResultPanel extends FlexPanel {

        McmcResultsPanel mcmcResPanel;
        PredictionResultsPanel predResPanel;

        public ResultPanel() {
                super(FlexPanel.AXIS.COL);

                JTabbedPane resultsTabs = new JTabbedPane();
                this.appendChild(resultsTabs, 1.0);

                FlexPanel mcmcRes = new FlexPanel(FlexPanel.AXIS.COL);

                JLabel titleMcmc = new JLabel();
                titleMcmc.setFont(titleMcmc.getFont().deriveFont(Font.BOLD, 18f));
                Lg.registerLabel(titleMcmc, "ui", "bam_mcmc_res");
                mcmcRes.appendChild(titleMcmc, 5, 10);

                mcmcResPanel = new McmcResultsPanel();
                mcmcRes.appendChild(mcmcResPanel, 1.0);

                FlexPanel predRes = new FlexPanel(FlexPanel.AXIS.COL);

                JLabel titlePred = new JLabel();
                titlePred.setFont(titlePred.getFont().deriveFont(Font.BOLD, 18f));
                Lg.registerLabel(titlePred, "ui", "bam_pred_res");
                predRes.appendChild(titlePred, 5, 10);

                predResPanel = new PredictionResultsPanel();
                predRes.appendChild(predResPanel, 1.0);

                // FIXME: need of proper translations
                resultsTabs.add("mcmc", mcmcRes);
                resultsTabs.add("pred", predRes);

                // resultsTabs.setSelectedIndex(1);
        }

        public void setResults(CalibrationResult calibrationResult, PredictionResult[] predictionResults) {
                if (calibrationResult != null) {
                        mcmcResPanel.setMcmcResults(calibrationResult);
                }
                if (predictionResults != null && calibrationResult != null) {
                        predResPanel.setPredictionResults(predictionResults, calibrationResult.getMaxPostIndex());
                }
        }
}
