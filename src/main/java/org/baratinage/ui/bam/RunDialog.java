package org.baratinage.ui.bam;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.utils.Monitoring;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.component.ProgressBar;
import org.baratinage.ui.component.SimpleLogger;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class RunDialog extends JDialog {

    private final String id;
    private final BaM bam;
    private final Path workspacePath;

    private final ProgressBar progressBar;
    private final JButton cancelButton;
    private final JButton closeButton;
    private final SimpleLogger logger;

    private SwingWorker<Void, String> runningWorker;
    private SwingWorker<Void, Void> monitoringWorker;;

    public RunDialog(String id, BaM bam) {
        super(AppConfig.AC.APP_MAIN_FRAME, true);
        this.id = id;
        this.bam = bam;

        workspacePath = Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id);
        // String zipName = id + ".zip";
        // Path zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);

        if (!workspacePath.toFile().exists()) {
            workspacePath.toFile().mkdir();
        }

        progressBar = new ProgressBar();
        cancelButton = new JButton();
        closeButton = new JButton();
        logger = new SimpleLogger();
        logger.setPreferredSize(new Dimension(900, 600));

        cancelButton.setText(Lg.text("cancel"));
        cancelButton.addActionListener((e) -> {
            cancel();
        });

        closeButton.setText(Lg.text("close"));
        closeButton.setEnabled(false);
        closeButton.addActionListener((e) -> {
            dispose();
        });

        RowColPanel pbPanel = new RowColPanel();
        pbPanel.setGap(5);
        pbPanel.appendChild(progressBar, 1);
        pbPanel.appendChild(cancelButton, 0);

        RowColPanel mainPanel = new RowColPanel(RowColPanel.AXIS.COL);
        mainPanel.setPadding(5);
        mainPanel.setGap(5);

        mainPanel.appendChild(pbPanel, 0);
        mainPanel.appendChild(logger, 1);
        mainPanel.appendChild(closeButton, 0);

        setContentPane(mainPanel);
        setTitle(Lg.text("bam_running"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

            }
        });

    }

    private void cancel() {

        if (runningWorker == null || monitoringWorker == null || runningWorker.isDone()) {
            return;
        }

        System.out.println("CANCELLING!");
        runningWorker.cancel(true);
        monitoringWorker.cancel(true);

        Process bamProcess = bam.getBaMexecutionProcess();
        if (bamProcess != null) {
            System.out.println("KILLING BAM PROCESS");
            bamProcess.destroy();
        }
    }

    public void executeBam(Consumer<RunConfigAndRes> onSuccess) {

        runningWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("BAMRUNNING");
                    bam.run(workspacePath.toString(), txt -> {
                        publish(txt);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
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

                if (!isCancelled()) {
                    setTitle(Lg.text("bam_done"));
                    onSuccess.accept(RunConfigAndRes.buildFromWorkspace(id, workspacePath));
                } else {
                    setTitle(Lg.text("bam_canceled"));
                }

                System.out.println("DONE");

            }

        };

        monitoringWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                new Monitoring(bam, workspacePath.toString(), (m) -> {
                    progressBar.update(m.id, m.progress, m.total, m.currenStep, m.totalSteps);
                });
                return null;
            }

        };

        runningWorker.execute();
        monitoringWorker.execute();

        pack();
        setLocationRelativeTo(AppConfig.AC.APP_MAIN_FRAME);
        setVisible(true);

    }

}
