package org.baratinage.ui.test;

import java.awt.Dimension;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.utils.Monitoring;
import org.baratinage.project.Project;
import org.baratinage.ui.component.Logger;
import org.baratinage.ui.component.ProgressBar;
import org.baratinage.ui.container.FlexPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;

import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class TestPanel extends FlexPanel {

    private ProgressBar progressBar;
    private Logger logger;
    private ResultPanel resultsPanel;
    private JTabbedPane tabs;

    private SwingWorker<Void, String> runningWorker;
    private SwingWorker<Void, Void> monitoringWorker;
    protected AbstractButton cancelBamButton;

    protected AbstractButton btn;

    private int lgChangedNtimes;

    // private FlexPanel that;

    public TestPanel() {
        super(FlexPanel.AXIS.COL);
        // that = this;
        this.setGap(5);
        this.setPadding(5);

        FlexPanel actionButtons = new FlexPanel(FlexPanel.AXIS.ROW);
        int actionHeight = 50;
        actionButtons.setMinimumSize(new Dimension(100, actionHeight));
        actionButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionHeight));
        actionButtons.setPreferredSize(new Dimension(100, actionHeight));
        this.appendChild(actionButtons);

        JButton lgPicker = new JButton();

        lgChangedNtimes = 0;

        actionButtons.appendChild(lgPicker);

        lgPicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lgChangedNtimes++; // for demonstration purposes only
                String key = Lg.getLocaleKey();
                if (!key.equals("en")) {
                    Lg.setLocale("en");
                } else {
                    Lg.setLocale("fr");
                }

            }

        });

        JButton launchBamButton = new JButton();

        actionButtons.appendChild(launchBamButton, 1.0);

        launchBamButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                launchBamButton.setEnabled(false);
                cancelBamButton.setEnabled(true);

                System.out.println("Create Test BaM project");

                Project proj = new Project();
                BaM bam = proj.createTest();

                System.out.println("Launch BaM");

                if (bam != null) {

                    String workspace = "test/testWorkspace";

                    runningWorker = new SwingWorker<>() {

                        @Override
                        protected Void doInBackground() throws Exception {

                            progressBar.update("starting", 0, 0, 0, 0);
                            String msg = bam.run(workspace, (String logMessage) -> {
                                publish(logMessage);
                            });
                            if (msg != "") {
                                System.err.println(msg);
                                msg = String.format(
                                        "<html>"
                                                + "<div style='font-weight: bold; color: red;'>BaM a rencontré un problème!</div>"
                                                + "<div style='margin-top: 10;'>Message d'erreur de BaM: </div>"
                                                + "<div style='color: black; font-family: monospace; background-color: white; padding: 5;'>%s</div>"
                                                + "</html>",
                                        msg.replaceAll("\\n", "<br>"));

                                JOptionPane.showMessageDialog(new JFrame(), msg, "BaM",
                                        JOptionPane.ERROR_MESSAGE);
                            }

                            return null;
                        }

                        @Override
                        protected void process(List<String> logs) {
                            logger.addLogs(logs.toArray(new String[0]));
                        }

                        @Override
                        protected void done() {

                            launchBamButton.setEnabled(true);
                            cancelBamButton.setEnabled(false);

                            if (this.isCancelled()) {
                                System.out.println("BaM running was canceled!");
                                progressBar.update("canceled", 0, 0, 0, 0);
                            } else {
                                System.out.println("BaM running is done!");
                                progressBar.update("done", 1, 1, 0, 0);

                                bam.readResults(workspace);
                                CalibrationResult calRes = bam.getCalibrationResults();
                                PredictionResult[] predRes = bam.getPredictionResults();
                                System.out.println(calRes);
                                System.out.println(predRes);
                                resultsPanel.setResults(calRes, predRes);
                                tabs.setSelectedIndex(1);

                            }

                        }

                    };

                    runningWorker.execute();

                    monitoringWorker = new SwingWorker<>() {

                        @Override
                        protected Void doInBackground() throws Exception {

                            new Monitoring(bam, workspace, (Monitoring.MonitoringStep m) -> {
                                progressBar.update(m.id, m.progress, m.total, m.currenStep, m.totalSteps);
                            });

                            return null;
                        }

                        @Override
                        protected void done() {

                            if (this.isCancelled()) {

                                System.out.println("Monitoring canceled!");
                                Process bamProcess = bam.getBaMexecutionProcess();
                                if (bamProcess != null) {
                                    bamProcess.destroy();
                                }
                                runningWorker.cancel(true);
                            } else {
                                System.out.println("Monitoring finished!");
                            }
                        }

                    };

                    monitoringWorker.execute();
                    //
                }
            }
        });

        cancelBamButton = new JButton();
        cancelBamButton.setEnabled(false);
        actionButtons.appendChild(cancelBamButton);

        cancelBamButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (monitoringWorker != null) {
                    monitoringWorker.cancel(true);
                }
            }

        });

        this.progressBar = new ProgressBar();
        this.appendChild(this.progressBar);

        logger = new Logger();

        resultsPanel = new ResultPanel();

        tabs = new JTabbedPane();
        this.appendChild(tabs, 1.0);

        tabs.add("logger", logger);
        tabs.add("resultsPanel", resultsPanel);

        Lg.register(new LgElement<JButton>(lgPicker) {
            @Override
            public void setTranslatedText() {
                String mainText = Lg.getText("ui", "change_language");

                String secondaryText = "";
                if (lgChangedNtimes == 0) {
                    secondaryText = Lg.getText("ui", "no_change_done");
                } else if (lgChangedNtimes == 1) {
                    secondaryText = Lg.getText("ui", "one_change_done");
                } else {
                    secondaryText = Lg.format(Lg.getText("ui", "n_changes_done"), lgChangedNtimes);
                }

                String template = "<html><div style='font-size: 13'>%s</div><div style='color: gray; font-size: 11; width: 200'>%s</div></html>";
                component.setText(String.format(template, mainText, secondaryText));
            }
        });

        Lg.registerButton(launchBamButton, "ui", "launch_bam");
        Lg.registerButton(cancelBamButton, "ui", "cancel");

        Lg.register(new LgElement<JTabbedPane>(tabs) {
            @Override
            public void setTranslatedText() {
                component.setTitleAt(0, Lg.getText("ui", "bam_log"));
                component.setTitleAt(1, Lg.getText("ui", "bam_mcmc_res"));
            }
        });
        // tabs.setSelectedIndex(1);
    }
}
