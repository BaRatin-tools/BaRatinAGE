package ui;

import java.awt.Dimension;

import javax.swing.JProgressBar;

public class ProgressBar extends JProgressBar {

    public ProgressBar() {
        this.setStringPainted(true);
        this.setString("");
        Dimension dim = this.getPreferredSize();
        dim.height = 16;
        this.setPreferredSize(dim);
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
