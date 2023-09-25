package org.baratinage.ui.component;

import java.awt.Dimension;

import javax.swing.JProgressBar;

import org.baratinage.ui.lg.Lg;

public class ProgressBar extends JProgressBar {

    public ProgressBar() {
        setStringPainted(true);
        setString("");
        int height = 25;
        setPreferredSize(new Dimension(0, height));
        setMinimumSize(new Dimension(0, height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    public void update(String id, int progress, int total, int step, int totalStep) {

        setMaximum(total);
        setValue(progress);

        double percent = total > 0 ? (double) progress / (double) total * 100 : 0;
        String stepTxt = Lg.text("step_n_out_of_m", step, totalStep);
        String s = "";
        if (id.equals("MCMC")) {
            String mainTxt = Lg.text("mcmc_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.startsWith("Prediction")) {
            String mainTxt = Lg.text("pred_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.equals("starting")) {
            s = Lg.text("conf_writing");
        } else if (id.equals("canceled")) {
            s = Lg.text("canceled");
        } else {
            s = Lg.text("done");
        }
        setString(s);
        repaint();

    }

}
