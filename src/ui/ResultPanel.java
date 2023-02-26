package ui;

import javax.swing.JLabel;

import bam.EstimatedParameter;
import ui.container.FlexPanel;
import ui.plot.TestLineChart;

import java.awt.Font;
import java.util.HashMap;

public class ResultPanel extends FlexPanel {

        TestLineChart lineChartPanel;
        McmcResultsPanel mcmcResPanel;

        public ResultPanel() {
                super(FlexPanel.AXIS.COL);
                JLabel title = new JLabel();
                title.setFont(title.getFont().deriveFont(Font.BOLD, 21f));
                title.setText("BaM results");
                this.appendChild(title, 5, 10);

                // JLabel title2 = new JLabel();
                // title2.setText("<html><div style='font-weight: bold; font-size: 16px'>BaM
                // results (same title using HTML)</div></html>");
                // this.appendChild(title2);

                mcmcResPanel = new McmcResultsPanel();
                this.appendChild(mcmcResPanel, 1.0);

        }

        public void setMcmcResults(HashMap<String, EstimatedParameter> estimatedParameters) {
                mcmcResPanel.setMcmcResults(estimatedParameters);
        }
}
