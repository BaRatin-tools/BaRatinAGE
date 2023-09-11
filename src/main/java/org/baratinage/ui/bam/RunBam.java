package org.baratinage.ui.bam;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.Monitoring;
import org.baratinage.utils.Action;
import org.baratinage.utils.Misc;
import org.baratinage.utils.ReadWriteZip;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.DefaultStructuralErrorModel;
import org.baratinage.ui.component.ProgressBar;
import org.baratinage.ui.component.SimpleLogger;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class RunBam {

    private JDialog monitoringDialog;

    public final String id;
    public final Path workspacePath;
    public final Path zipPath;
    public final String zipName;
    public final BaM bam;

    public RunBam(String id) {
        this.id = id;
        workspacePath = Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id);
        zipName = id + ".zip";
        zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);

        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());

        File mainConfigFile = Path.of(workspacePath.toString(), BamFilesHelpers.CONFIG_BAM).toFile();
        mainConfigFile.renameTo(Path.of(BamFilesHelpers.EXE_DIR, BamFilesHelpers.CONFIG_BAM).toFile());

        bam = BaM.readBaM(mainConfigFile.getAbsolutePath(), workspacePath.toString());
        bam.readResults(workspacePath.toString());
        System.out.println(bam);

    }

    public RunBam(
            IModelDefinition modelDefinition,
            IPriors priors,
            IStructuralError structuralError,
            ICalibrationData calibrationData,
            IPredictionExperiment[] predictionExperiments) {

        if (modelDefinition == null) {
            throw new IllegalArgumentException("'modelDefinition' must non null!");
        }
        if (priors == null) {
            throw new IllegalArgumentException("'priors' must non null!");
        }

        id = Misc.getTimeStampedId();
        workspacePath = Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id);
        zipName = id + ".zip";
        zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);

        if (!workspacePath.toFile().exists()) {
            workspacePath.toFile().mkdir();
        }

        // create BaM object

        // 1) model

        String xTra = modelDefinition.getXtra(workspacePath.toString());

        Parameter[] parameters = priors.getParameters();

        String[] inputNames = modelDefinition.getInputNames();
        String[] outputNames = modelDefinition.getOutputNames();

        Model model = new Model(
                BamFilesHelpers.CONFIG_MODEL,
                modelDefinition.getModelId(),
                inputNames.length,
                outputNames.length,
                parameters,
                xTra,
                BamFilesHelpers.CONFIG_XTRA);

        // 2) strucutral error
        // FIXME currently supporting a single error model for all model outputs
        if (structuralError == null) {
            structuralError = new DefaultStructuralErrorModel(
                    DefaultStructuralErrorModel.TYPE.LINEAR);
        }
        StructuralErrorModel structErrorModel = structuralError.getStructuralErrorModel();
        ModelOutput[] modelOutputs = new ModelOutput[outputNames.length];
        for (int k = 0; k < outputNames.length; k++) {
            modelOutputs[k] = new ModelOutput(outputNames[k], structErrorModel);
        }

        // 3) calibration data

        CalibrationData calibData;

        if (calibrationData == null) {
            double[] fakeDataArray = new double[] { 0 };
            UncertainData[] inputs = new UncertainData[inputNames.length];
            for (int k = 0; k < inputNames.length; k++) {
                inputs[k] = new UncertainData(inputNames[k], fakeDataArray);
            }
            UncertainData[] outputs = new UncertainData[inputNames.length];
            for (int k = 0; k < outputNames.length; k++) {
                outputs[k] = new UncertainData(outputNames[k], fakeDataArray);
            }

            String dataName = "fakeCalibrationData";
            calibData = new CalibrationData(
                    dataName,
                    BamFilesHelpers.CONFIG_CALIBRATION,
                    String.format(BamFilesHelpers.DATA_CALIBRATION, dataName),
                    inputs,
                    outputs);

        } else {
            calibData = calibrationData.getCalibrationData();

        }

        CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();

        McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
        McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();
        // FIXME: a IMcmc should be an argument that provides the MCMC configuration
        McmcConfig mcmcConfig = new McmcConfig();

        CalibrationConfig calibrationConfig = new CalibrationConfig(
                model,
                modelOutputs,
                calibData,
                mcmcConfig,
                mcmcCookingConfig,
                mcmcSummaryConfig,
                calDataResidualConfig);

        // 4) predictions

        PredictionConfig[] predConfigs = new PredictionConfig[predictionExperiments.length];
        for (int k = 0; k < predictionExperiments.length; k++) {
            predConfigs[k] = predictionExperiments[k].getPredictionConfig();
        }

        // 5) run options

        RunOptions runOptions = new RunOptions(
                BamFilesHelpers.CONFIG_RUN_OPTIONS,
                true,
                true,
                true,
                true);

        // 6) BaM

        bam = new BaM(calibrationConfig, predConfigs, runOptions);
    };

    public void run(Action runWhenDone) {

        // setup dialog
        monitoringDialog = new JDialog(AppConfig.AC.APP_MAIN_FRAME, true);

        BamRunMonitoringPanel monitoringPanel = new BamRunMonitoringPanel();

        monitoringDialog.setContentPane(monitoringPanel);
        monitoringDialog.setTitle(Lg.text("bam_running"));

        monitoringPanel.cancelButton.setText(Lg.text("cancel"));
        monitoringPanel.closeButton.setText(Lg.text("close"));
        monitoringPanel.closeButton.setEnabled(false);

        SwingWorker<Void, String> runningWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("BAMRUNNING");
                    bam.run(workspacePath.toString(), txt -> {
                        // System.out.println("log => " + txt);
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
                monitoringPanel.logger.addLogs(logs.toArray(new String[0]));
            }

            @Override
            protected void done() {
                if (!isCancelled()) {
                    monitoringDialog.setTitle(Lg.text("bam_done"));

                    System.out.println("READING RESULTS");
                    readResultsFromWorkspace();
                    System.out.println("ZIPPING RUN");
                    // FIXME: inefficient but safer (caller classer doesn't need to call it)
                    zipBamRun();

                } else {
                    monitoringDialog.setTitle(Lg.text("bam_canceled"));
                }

                monitoringPanel.closeButton.setEnabled(true);
                monitoringPanel.cancelButton.setEnabled(false);
                System.out.println("DONE");
                runWhenDone.run();
            }

        };

        runningWorker.execute();

        SwingWorker<Void, Void> monitoringWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                new Monitoring(bam, workspacePath.toString(), (m) -> {
                    monitoringPanel.progressBar.update(m.id, m.progress, m.total, m.currenStep, m.totalSteps);
                });
                return null;
            }

        };

        monitoringWorker.execute();

        ActionListener onCancelAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                System.out.println("CANCELLING!");
                runningWorker.cancel(true);
                monitoringWorker.cancel(true);

                Process bamProcess = bam.getBaMexecutionProcess();
                if (bamProcess != null) {
                    System.out.println("KILLING BAM PROCESS");
                    bamProcess.destroy();
                }
            }

        };

        monitoringPanel.cancelButton.addActionListener(onCancelAction);

        monitoringPanel.closeButton.addActionListener((e) -> {
            monitoringDialog.dispose();
        });

        monitoringDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!runningWorker.isDone()) {
                    onCancelAction.actionPerformed(null);
                }
            }
        });
        monitoringDialog.pack();
        monitoringDialog.setLocationRelativeTo(AppConfig.AC.APP_MAIN_FRAME);
        monitoringDialog.setVisible(true);

    }

    private void readResultsFromWorkspace() {
        bam.readResults(workspacePath.toString());
    }

    public boolean hasResults() {
        return !(bam.getCalibrationResults() == null && bam.getPredictionResults() == null);
    }

    public void zipBamRun() {
        ReadWriteZip.flatZip(zipPath.toString(), workspacePath.toString());
    }

    public void unzipBamRun() {
        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());
        readResultsFromWorkspace();
    }

    private class BamRunMonitoringPanel extends RowColPanel {

        public final ProgressBar progressBar;
        public final JButton cancelButton;
        public final JButton closeButton;
        public final SimpleLogger logger;

        public BamRunMonitoringPanel() {
            super(AXIS.COL);
            setPadding(5);
            setGap(5);

            progressBar = new ProgressBar();
            cancelButton = new JButton();
            closeButton = new JButton();
            logger = new SimpleLogger();
            logger.setPreferredSize(new Dimension(900, 600));

            RowColPanel pbPanel = new RowColPanel();
            pbPanel.setGap(5);
            pbPanel.appendChild(progressBar, 1);
            pbPanel.appendChild(cancelButton, 0);

            appendChild(pbPanel, 0);
            appendChild(logger, 1);
            appendChild(closeButton, 0);
        }
    }
}
