package ui;

import java.awt.Dimension;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import bam.BaM;
import bam.CalibrationResult;
import bam.utils.Monitoring;
import project.Project;
import ui.container.FlexPanel;

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

    public TestPanel() {
        super(FlexPanel.AXIS.COL, 5);
        // this.setLayout(new GridBagLayout());

        FlexPanel actionButtons = new FlexPanel(FlexPanel.AXIS.ROW);
        int actionHeight = 50;
        actionButtons.setMinimumSize(new Dimension(100, actionHeight));
        actionButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionHeight));
        actionButtons.setPreferredSize(new Dimension(100, actionHeight));
        this.appendChild(actionButtons);

        JButton lgPicker = new JButton();
        lgPicker.setText("Test changement de langue");
        actionButtons.appendChild(lgPicker);

        lgPicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = Lg.getLanguageKey();
                System.out.println(key);
                if (!key.equals("en")) {
                    Lg.setLanguage("en");
                } else {
                    Lg.setLanguage("fr");
                }

            }

        });

        // JButton btnRemover = new JButton();
        // btnRemover.setText("Remove");
        // actionButtons.appendChild(btnRemover);
        // btnRemover.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // actionButtons.remove(btn);
        // TestPanel.this.updateUI();
        // // TestPanel.this.repaint();
        // }
        // });
        // btn = new JButton();
        // Lg.setText(btn, "will_be_removed");
        // actionButtons.appendChild(btn);

        JButton launchBamButton = new JButton();
        // launchBamButton.setText(Lg.getText("launch_bam"));
        Lg.setText(launchBamButton, "launch_bam");
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
                            bam.run(workspace, (String logMessage) -> {
                                publish(logMessage);
                            });

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

                                if (bam.getRunOptions().doMcmc) {
                                    bam.readResults(workspace);
                                    CalibrationResult res = bam.getCalibrationResults();
                                    resultsPanel.setMcmcResults(res.getEsimatedParameters());
                                    tabs.setSelectedIndex(1);
                                }
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
        // cancelBamButton.setText(Lg.getText("cancel"));
        Lg.setText(cancelBamButton, "cancel");
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

        tabs.add(Lg.getText("bam_log"), logger);
        tabs.add("dgfsdflsdkfmklm", resultsPanel);

        Lg.setText(tabs, "bam_log", 0);
        Lg.setText(tabs, "bam_mcmc_res", 1);

        // tabs.setSelectedIndex(1);
    }
}
