package ui;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import bam.EstimatedParameter;
import ui.container.FlexPanel;
import ui.lg.Lg;
import ui.plot.TestLineChart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.HashMap;

public class ResultPanel extends FlexPanel {

        TestLineChart lineChartPanel;
        McmcResultsPanel mcmcResPanel;
        PredictionResultsPanel predResPanel;

        public ResultPanel() {
                super(FlexPanel.AXIS.COL);

                this.setBackground(Color.RED);

                JTabbedPane resultsTabs = new JTabbedPane();
                this.appendChild(resultsTabs, 1.0);

                FlexPanel mcmcRes = new FlexPanel(FlexPanel.AXIS.COL);

                JLabel titleMcmc = new JLabel();
                titleMcmc.setFont(titleMcmc.getFont().deriveFont(Font.BOLD, 21f));
                Lg.registerLabel(titleMcmc, "ui", "bam_mcmc_res");
                mcmcRes.appendChild(titleMcmc, 5, 10);

                mcmcResPanel = new McmcResultsPanel();
                mcmcRes.appendChild(mcmcResPanel, 1.0);

                FlexPanel predRes = new FlexPanel(FlexPanel.AXIS.COL);

                JLabel titlePred = new JLabel();
                titlePred.setFont(titlePred.getFont().deriveFont(Font.BOLD, 21f));
                Lg.registerLabel(titlePred, "ui", "bam_pred_res");
                predRes.appendChild(titlePred, 5, 10);

                predResPanel = new PredictionResultsPanel();
                predRes.appendChild(predResPanel, 1.0);

                resultsTabs.add("mcmc", mcmcRes);
                resultsTabs.add("pred", predRes);
        }

        public void setMcmcResults(HashMap<String, EstimatedParameter> estimatedParameters, int maxpostIndex) {
                mcmcResPanel.setMcmcResults(estimatedParameters, maxpostIndex);
        }
}
