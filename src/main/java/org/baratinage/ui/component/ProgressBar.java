package org.baratinage.ui.component;

import java.awt.Dimension;

import javax.swing.JProgressBar;

import org.baratinage.ui.lg.Lg;

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

        double percent = total > 0 ? (double) progress / (double) total * 100 : 0;
        String stepTxt = Lg.format(Lg.getText("ui", "step_n_out_of_m"), step, totalStep);
        String s = "";
        if (id.equals("MCMC")) {
            String mainTxt = Lg.getText("ui", "mcmc_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.startsWith("Prediction")) {
            String mainTxt = Lg.getText("ui", "pred_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.equals("starting")) {
            s = Lg.getText("ui", "conf_writing");
        } else if (id.equals("canceled")) {
            s = Lg.getText("ui", "canceled");
        } else {
            s = Lg.getText("ui", "done");
        }
        this.setString(s);
        this.repaint();

    }

}
