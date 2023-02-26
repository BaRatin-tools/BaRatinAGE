package ui;

import java.awt.Dimension;

import javax.swing.JProgressBar;

public class ProgressBar extends JProgressBar {

    public ProgressBar() {
        this.setStringPainted(true);
        this.setString("");
        int height = 25;
        this.setPreferredSize(new Dimension(0, height));
        this.setMinimumSize(new Dimension(0, height));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    public void update(String id, int progress, int total, int step, int totalStep) {

        this.setMaximum(total);
        this.setValue(progress);

        String s = "";
        if (id.equals("MCMC")) {
            s = "MCMC simulations running ...";
        } else if (id.startsWith("Prediction")) {
            s = "Propagation experiment running ...";
        } else if (id.equals("starting")) {
            this.setString("Writing configuration and data files ...");
            this.repaint();
            return;
        } else if (id.equals("canceled")) {
            this.setString("Canceled!");
            this.repaint();
            return;
        } else {
            this.setString("Done!");
            this.repaint();
            return;
        }
        double percent = (double) progress / (double) total * 100;
        this.setString(String.format("Step %d/%d: %s (%.0f %%)", step, totalStep, s, percent));
        this.repaint();

    }

}
