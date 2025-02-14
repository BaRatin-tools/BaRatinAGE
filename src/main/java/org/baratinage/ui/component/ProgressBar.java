package org.baratinage.ui.component;

import javax.swing.JProgressBar;

import org.baratinage.translation.T;
import org.baratinage.utils.Misc;

public class ProgressBar extends JProgressBar {

    public ProgressBar() {
        setStringPainted(true);
        setString("");
        Misc.setMinimumSize(this, null, 25);
    }

    public void update(String id, int progress, int total, int step, int totalStep) {

        setMaximum(total);
        setValue(progress);

        double percent = total > 0 ? (double) progress / (double) total * 100 : 0;
        String stepTxt = T.text("step_n_out_of_m", step, totalStep);
        String s = "";
        if (id.equals("MCMC")) {
            String mainTxt = T.text("mcmc_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.startsWith("Prediction")) {
            String mainTxt = T.text("pred_running");
            s = String.format("%s - %.0f %% - %s", stepTxt, percent, mainTxt);
        } else if (id.equals("starting")) {
            s = T.text("conf_writing");
        } else if (id.equals("canceled")) {
            s = T.text("canceled");
        } else {
            s = T.text("done");
        }
        setString(s);
        repaint();

    }

}
