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
import ui.lg.Lg;
import ui.lg.LgElement;

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

    public TestPanel() {
        super(FlexPanel.AXIS.COL, 5);

        FlexPanel actionButtons = new FlexPanel(FlexPanel.AXIS.ROW);
        int actionHeight = 50;
        actionButtons.setMinimumSize(new Dimension(100, actionHeight));
        actionButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionHeight));
        actionButtons.setPreferredSize(new Dimension(100, actionHeight));
        this.appendChild(actionButtons);

        JButton lgPicker = new JButton();

        lgChangedNtimes = 0;

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

                String template = "<html><div>%s</div><div style='color: gray; font-size: smaller; width: 200'>%s</div></html>";
                component.setText(String.format(template, mainText, secondaryText));
            }
        });

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
        Lg.registerButton(launchBamButton, "ui", "launch_bam");

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

                                bam.readResults(workspace);
                                CalibrationResult res = bam.getCalibrationResults();
                                if (res != null) {
                                    resultsPanel.setMcmcResults(res.getEsimatedParameters(), res.getMaxPostIndex());
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
        Lg.registerButton(cancelBamButton, "ui", "cancel");
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

        Lg.register(new LgElement<JTabbedPane>(tabs) {
            @Override
            public void setTranslatedText() {
                component.setTitleAt(0, Lg.getText("ui", "bam_log"));
                component.setTitleAt(1, Lg.getText("ui", "bam_mcmc_res"));
            }
        });
    }
}
