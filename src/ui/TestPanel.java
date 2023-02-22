package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import bam.BaM;
import bam.utils.Monitoring;
import project.Project;

import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class TestPanel extends JPanel {

    private ProgressBar progressBar;
    private Logger logger;

    private SwingWorker<Void, String> runningWorker;
    private SwingWorker<Void, Void> monitoringWorker;
    protected AbstractButton cancelBamButton;

    public TestPanel() {

        this.setLayout(new GridBagLayout());

        JPanel actionButtons = new JPanel();
        actionButtons.setLayout(new GridBagLayout());
        this.add(actionButtons,
                new GridBagConstraints(
                        0,
                        0,
                        1,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0),
                        0,
                        0));

        JButton launchBamButton = new JButton();
        launchBamButton.setText("Launch BaM");
        launchBamButton.setPreferredSize(new Dimension(500, 50));

        actionButtons.add(launchBamButton,
                new GridBagConstraints(
                        0,
                        0,
                        1,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2),
                        0,
                        0));

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
                            if (this.isCancelled()) {
                                System.out.println("BaM running was canceled!");
                                progressBar.update("canceled", 0, 0, 0, 0);
                            } else {
                                System.out.println("BaM running is done!");
                                progressBar.update("done", 1, 1, 0, 0);
                            }

                            launchBamButton.setEnabled(true);
                            cancelBamButton.setEnabled(false);
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
        cancelBamButton.setText("Cancel");
        cancelBamButton.setEnabled(false);
        cancelBamButton.setPreferredSize(new Dimension(100, 50));

        actionButtons.add(cancelBamButton,
                new GridBagConstraints(
                        1,
                        0,
                        1,
                        1,
                        0.0,
                        1.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2),
                        0,
                        0));

        cancelBamButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (monitoringWorker != null) {
                    monitoringWorker.cancel(true);
                }
            }

        });

        this.progressBar = new ProgressBar();

        this.add(progressBar,

                new GridBagConstraints(
                        0,
                        1,
                        1,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2),
                        0,
                        0)

        );

        logger = new Logger();

        this.add(logger,

                new GridBagConstraints(
                        0,
                        2,
                        1,
                        1,
                        1.0,
                        1000,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2),
                        0,
                        0)

        );

    }
}
