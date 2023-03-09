package ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JPanel;

import jbam.CalibrationResult;
import jbam.EstimatedParameter;
import ui.plot.PlotContainer;
import ui.plot.PlotLine;
import ui.plot.Plot;
import utils.Calc;

public class McmcResultsPanel extends JPanel {

    public McmcResultsPanel() {
        this.setLayout(new GridBagLayout());
    }

    public void setMcmcResults(CalibrationResult calibrationResult) {
        HashMap<String, EstimatedParameter> estimatedParameters = calibrationResult.getEsimatedParameters();
        int maxpostIndex = calibrationResult.getMaxPostIndex();

        int n = -1;
        for (EstimatedParameter p : estimatedParameters.values()) {
            double[] mcmc = p.getMcmc();
            n = mcmc.length;
            break;
        }
        if (n == -1)
            return;

        double[] x = new double[n];
        for (int k = 0; k < n; k++)
            x[k] = k;

        int i = 0;
        int j = 0;
        int padding = 5;
        for (EstimatedParameter p : estimatedParameters.values()) {

            double[] mcmc = p.getMcmc();

            Plot plot = new Plot("index", p.getName(), false);
            plot.addLine(new PlotLine("", x, mcmc, Color.BLACK, 1));

            double xMean = maxpostIndex;
            // double yMean = Calc.mean(mcmc);
            double[] yRange = Calc.range(mcmc);

            plot.addLine(new PlotLine("",
                    new double[] { xMean, xMean }, yRange, Color.RED, 2));

            PlotContainer plotContainer = new PlotContainer(plot.getChart());

            this.add(plotContainer, new GridBagConstraints(
                    i,
                    j,
                    1,
                    1,
                    1.0,
                    1.0,
                    GridBagConstraints.NORTH,
                    GridBagConstraints.BOTH,
                    new Insets(j == 0 ? padding : 0, i == 0 ? padding : 0, padding, padding),
                    0, 0));

            i++;
            if (i > 2) {
                i = 0;
                j++;
            }
        }

        this.repaint();
        this.updateUI();

    }
}
