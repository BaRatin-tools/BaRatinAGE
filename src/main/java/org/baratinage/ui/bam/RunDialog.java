package org.baratinage.ui.bam;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.utils.Monitoring;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.run.BamRunException;
import org.baratinage.ui.component.ProgressBar;
import org.baratinage.ui.component.SimpleLogger;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;

public class RunDialog extends JDialog {

    private final String id;
    private final BaM bam;
    private final Path workspacePath;

    private final ProgressBar progressBar;
    private final JButton cancelButton;
    private final JButton closeButton;
    private final SimpleLogger logger;
    private final JCheckBox showHideLoggerButton;
    private final JCheckBox closeBamDialogOnSuccessButton;

    private SwingWorker<Void, String> runningWorker;
    private SwingWorker<Void, Void> monitoringWorker;
    private boolean isUserCanceled = false;

    public RunDialog(String id, BaM bam) {
        super(AppSetup.MAIN_FRAME, true);
        this.id = id;
        this.bam = bam;

        workspacePath = Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, id);

        if (!workspacePath.toFile().exists()) {
            workspacePath.toFile().mkdir();
        }

        progressBar = new ProgressBar();
        cancelButton = new JButton();
        closeButton = new JButton();
        logger = new SimpleLogger();
        logger.setPreferredSize(new Dimension(700, 300));
        logger.setMinimumSize(new Dimension(700, 300));

        cancelButton.setText(T.text("cancel"));
        cancelButton.addActionListener((e) -> {
            isUserCanceled = true;
            cancel();
        });

        closeButton.setText(T.text("close"));
        closeButton.setEnabled(false);
        closeButton.addActionListener((e) -> {
            dispose();
        });

        showHideLoggerButton = new JCheckBox();
        showHideLoggerButton.setText(T.text("pref_hide_bam_console"));
        showHideLoggerButton.setSelected(AppSetup.CONFIG.HIDE_BAM_CONSOLE.get());
        showHideLoggerButton.addActionListener(l -> {
            AppSetup.CONFIG.HIDE_BAM_CONSOLE.set(showHideLoggerButton.isSelected());
            AppSetup.CONFIG.saveConfiguration();
            resetContent();
        });
        closeBamDialogOnSuccessButton = new JCheckBox();
        closeBamDialogOnSuccessButton.setText(T.text("pref_close_bam_console_on_success"));
        closeBamDialogOnSuccessButton.setSelected(AppSetup.CONFIG.CLOSE_BAM_DIALOG_ON_SUCCESS.get());
        closeBamDialogOnSuccessButton.addActionListener(l -> {
            AppSetup.CONFIG.CLOSE_BAM_DIALOG_ON_SUCCESS.set(closeBamDialogOnSuccessButton.isSelected());
            AppSetup.CONFIG.saveConfiguration();
            resetContent();
        });

        resetContent();

        setTitle(T.text("bam_running"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

            }
        });

    }

    private void resetContent() {

        SimpleFlowPanel pbPanel = new SimpleFlowPanel();
        pbPanel.setGap(5);
        pbPanel.addChild(progressBar, true);
        pbPanel.addChild(cancelButton, false);

        SimpleFlowPanel mainPanel = new SimpleFlowPanel(true);
        mainPanel.setPadding(5);
        mainPanel.setGap(5);

        mainPanel.addChild(pbPanel, false);
        if (!AppSetup.CONFIG.HIDE_BAM_CONSOLE.get()) {
            mainPanel.addChild(logger, true);
        }

        mainPanel.addChild(showHideLoggerButton, false);
        mainPanel.addChild(closeBamDialogOnSuccessButton, false);

        mainPanel.addChild(closeButton, false);

        setContentPane(mainPanel);
        pack();

    }

    private void cancel() {

        if (runningWorker == null || monitoringWorker == null || runningWorker.isDone()) {
            return;
        }

        ConsoleLogger.log("Cancelling BaM run...");
        runningWorker.cancel(true);
        monitoringWorker.cancel(true);

        Process bamProcess = bam.getBaMexecutionProcess();
        if (bamProcess != null) {
            ConsoleLogger.log("Killing BaM process...");
            bamProcess.destroy();
        }
    }

    public void executeBam(Consumer<RunConfigAndRes> onSuccess) {

        isUserCanceled = false;

        runningWorker = new SwingWorker<>() {

            private boolean success = true;
            private Exception exception = null;

            @Override
            protected Void doInBackground() throws Exception {
                success = true;
                try {
                    ConsoleLogger.log("BaM starting...");
                    bam.run(workspacePath.toString(), txt -> {
                        publish(txt);
                    });
                } catch (InterruptedException e) {
                    ConsoleLogger.error(e);
                    cancel(true);
                } catch (BamRunException | IOException e) {
                    ConsoleLogger.error(e);
                    success = false;
                    exception = e;
                    cancel(true);
                }
                if (success) {
                    ConsoleLogger.log("BaM ran successfully!");
                } else {
                    ConsoleLogger.error("BaM encountered an error!");
                }
                return null;
            }

            @Override
            protected void process(List<String> logs) {
                logger.addLogs(logs.toArray(new String[0]));
            }

            @Override
            protected void done() {

                closeButton.setEnabled(true);
                cancelButton.setEnabled(false);

                if (!success && !isUserCanceled) {
                    setTitle(T.text("bam_error"));
                    progressBar.setString(T.text("bam_error"));
                    if (exception != null) {
                        BamRunException bre = new BamRunException(exception);
                        bre.errorMessageDialog();
                    }
                } else {
                    if (!isCancelled()) {
                        setTitle(T.text("bam_result_processing"));
                        progressBar.setString(T.text("bam_result_processing"));
                        onSuccess.accept(RunConfigAndRes.buildFromWorkspace(id, workspacePath));
                        setTitle(T.text("bam_done"));
                        progressBar.setString(T.text("bam_done"));
                        if (AppSetup.CONFIG.CLOSE_BAM_DIALOG_ON_SUCCESS.get()) {
                            dispose();
                        }
                    } else {
                        setTitle(T.text("bam_canceled"));
                    }
                }
            }

        };

        monitoringWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                Monitoring monitoring = new Monitoring(bam, workspacePath.toString());
                monitoring.setBamWorker(runningWorker);
                monitoring.addMonitoringConsumer((m) -> {
                    progressBar.update(m.id, m.progress, m.total, m.currenStep, m.totalSteps);
                });
                try {
                    monitoring.startMonitoring();
                } catch (InterruptedException e) {
                    ConsoleLogger.error("BaM monitoring interrupted!");
                }
                return null;
            }

        };

        runningWorker.execute();

        monitoringWorker.execute();

        pack();
        setLocationRelativeTo(AppSetup.MAIN_FRAME);
        setVisible(true);

    }

}
