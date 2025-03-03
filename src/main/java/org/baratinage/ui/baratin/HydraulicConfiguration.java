package org.baratinage.ui.baratin;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.AppSetup;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.jbam.PredictionState;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PredExp;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;
import org.baratinage.ui.baratin.rating_curve.RatingCurveResults;
import org.baratinage.ui.baratin.rating_curve.RatingCurveStageGrid;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class HydraulicConfiguration
        extends BamItem
        implements IModelDefinition, IPriors, IPredictionMaster {

    private final ControlMatrix controlMatrix;
    private final HydraulicControlPanels hydraulicControls;
    private final RatingCurveStageGrid priorRatingCurveStageGrid;
    private final RatingCurveResults resultsPanel;

    private boolean isTabView = false;
    private final RowColPanel priorRatingCurvePanel;
    private final Title controlMatrixTitle;
    private final Title priorRCplotTitle;
    private final Title priorSpecificationTitle;
    private final TabContainer mainContainerTab;

    private RowColPanel outOufSyncPanel;

    public RunBam runBam;
    private RunConfigAndRes bamRunConfigAndRes;

    private BamConfigRecord backup;

    public static final ImageIcon controlMatrixIcon = AppSetup.ICONS.getCustomAppImageIcon("control_matrix.svg");
    public static final ImageIcon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final ImageIcon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    public HydraulicConfiguration(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG, uuid, project);

        controlMatrix = new ControlMatrix();
        controlMatrix.addChangeListener((e) -> {
            fireChangeListeners();
            updateHydraulicControls(controlMatrix.getControlMatrix());
            checkPriorRatingCurveSync();
        });

        hydraulicControls = new HydraulicControlPanels();
        hydraulicControls.addChangeListener((e) -> {
            fireChangeListeners();
            checkPriorRatingCurveSync();
        });

        priorRatingCurveStageGrid = new RatingCurveStageGrid();
        priorRatingCurveStageGrid.addChangeListener((e) -> {
            // fireChangeListeners();
            checkPriorRatingCurveSync();
        });

        // **************************************************
        // Results panel

        resultsPanel = new RatingCurveResults(PROJECT, true);

        // prevents the control matrix to be too small in some cases
        // (e.g. after project load)
        controlMatrix.setPreferredSize(new Dimension(500, 250));
        controlMatrix.setMinimumSize(new Dimension(250, 150));

        runBam = new RunBam(false, true, false);
        runBam.setModelDefintion(this);
        runBam.setPriors(this);
        runBam.setPredictionExperiments(this);
        runBam.addOnDoneAction((RunConfigAndRes res) -> {
            bamRunConfigAndRes = res;
            backup = save(true);
            buildPlot();
            checkPriorRatingCurveSync();
        });

        priorRatingCurvePanel = new RowColPanel(RowColPanel.AXIS.COL);

        outOufSyncPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outOufSyncPanel.setPadding(5);

        priorRatingCurvePanel.appendChild(priorRatingCurveStageGrid, 0);
        priorRatingCurvePanel.appendChild(new JSeparator(), 0);
        priorRatingCurvePanel.appendChild(outOufSyncPanel, 0);
        priorRatingCurvePanel.appendChild(runBam.runButton, 0, 5);
        priorRatingCurvePanel.appendChild(resultsPanel, 1);

        // **********************************************************************
        // SPECIFIC TO SPLIT PANE / TAB SYSTEM APPROACHES

        controlMatrixTitle = new Title(controlMatrixIcon, "");
        priorRCplotTitle = new Title(priorRatingCurveIcon, "");
        priorSpecificationTitle = new Title(priorSpecificationIcon, "");

        mainContainerTab = new TabContainer();

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                TimedActions.throttle(
                        "hydraulic_config_resize_action",
                        250,
                        HydraulicConfiguration.this::setPanelView);
            }
        });

        setSplitPaneView();

        // **********************************************************************

        boolean[][] mat = controlMatrix.getControlMatrix();
        updateHydraulicControls(mat);

        T.updateHierarchy(this, controlMatrix);
        T.updateHierarchy(this, hydraulicControls);
        T.updateHierarchy(this, priorRatingCurveStageGrid);
        T.updateHierarchy(this, resultsPanel);
        T.updateHierarchy(this, runBam);
        T.updateHierarchy(this, outOufSyncPanel);

        T.t(this, () -> {
            controlMatrixTitle.setText(T.html("control_matrix"));
            priorRCplotTitle.setText(T.html("prior_rating_curve"));
            priorSpecificationTitle.setText(T.html("prior_parameter_specification"));
            if (mainContainerTab.getTabCount() > 2) {
                mainContainerTab.setTitleAt(0, T.html("control_matrix"));
                mainContainerTab.setTitleAt(1, T.html("prior_parameter_specification"));
                mainContainerTab.setTitleAt(2, T.html("prior_rating_curve"));
            }
        });

    }

    private void setPanelView() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        ConsoleLogger.log("panel size is : " + panelWidth + " x " + panelHeight);
        if (panelWidth == 0 || panelHeight == 0) {
            return;
        }
        if (panelWidth < 1100 || panelHeight < 800) {
            if (!isTabView) {
                setTabView();
            }
            isTabView = true;
        } else {
            if (isTabView) {
                setSplitPaneView();
            }
            isTabView = false;
        }
    }

    private void setSplitPaneView() {

        RowColPanel controlMatrixContainer = new RowColPanel(RowColPanel.AXIS.COL);

        controlMatrixContainer.appendChild(controlMatrixTitle, 0);
        controlMatrixContainer.appendChild(controlMatrix, 1);

        RowColPanel priorRCplotPanel = new RowColPanel(RowColPanel.AXIS.COL);

        priorRCplotTitle.setIcon(priorRatingCurveIcon);
        priorRCplotPanel.appendChild(priorRCplotTitle, 0);
        priorRCplotPanel.appendChild(priorRatingCurvePanel, 1);

        RowColPanel priorSepecificationPanel = new RowColPanel(RowColPanel.AXIS.COL);

        priorSpecificationTitle.setIcon(priorSpecificationIcon);
        priorSepecificationPanel.appendChild(priorSpecificationTitle, 0);
        priorSepecificationPanel.appendChild(hydraulicControls, 1);

        SplitContainer mainContainer = SplitContainer.build2Left1RightSplitContainer(
                controlMatrixContainer,
                priorRCplotPanel,
                priorSepecificationPanel);
        setContent(mainContainer);
        T.updateTranslation(this);
        updateUI();
    }

    private void setTabView() {

        mainContainerTab.removeAll();
        mainContainerTab.addTab("control_matrix", controlMatrixIcon, controlMatrix);
        mainContainerTab.addTab("prior_parameter_specification",
                priorSpecificationIcon,
                hydraulicControls);
        mainContainerTab.addTab("prior_rating_curve", priorRatingCurveIcon,
                priorRatingCurvePanel);

        setContent(mainContainerTab);
        T.updateTranslation(this);
        updateUI();
    }

    private void checkPriorRatingCurveSync() {
        T.clear(outOufSyncPanel);
        T.clear(runBam);
        outOufSyncPanel.clear();
        if (backup == null) {
            T.t(runBam, runBam.runButton, true, "compute_prior_rc");
            runBam.runButton.setForeground(new JButton().getForeground());
            updateUI();
            return;
        }

        JSONObject currentJson = save(false).jsonObject();

        JSONObject backupJson = backup.jsonObject();

        JSONFilter filter = new JSONFilter(true, true,
                "backup",
                "allControlOptions",
                "controlTypeIndex",
                "isKACmode", "isLocked",
                "isReversed",
                "description");
        JSONObject filteredCurrentJson = filter.apply(currentJson);
        JSONObject filteredBackupJson = filter.apply(backupJson);

        JSONCompareResult comparison = JSONCompare.compare(
                filteredBackupJson,
                filteredCurrentJson);

        if (comparison.matching()) {
            // FIXME: refactoring needed
            T.t(runBam, runBam.runButton, true, "compute_prior_rc");
            runBam.runButton.setForeground(new JButton().getForeground());
            return;
        }

        List<MsgPanel> outOfSyncMessages = new ArrayList<>();

        JSONCompareResult stageGridComparison = comparison.children().get("stageGridConfig");

        if (!stageGridComparison.matching()) {
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
            T.t(outOufSyncPanel, msg.message, true, "oos_stage_grid");
            JButton revertBackBtn = new JButton();
            T.t(outOufSyncPanel, revertBackBtn, true, "cancel_changes");
            revertBackBtn.addActionListener((e) -> {
                priorRatingCurveStageGrid.fromJSON(
                        backupJson.getJSONObject("stageGridConfig"));
                checkPriorRatingCurveSync();
            });
            msg.addButton(revertBackBtn);
            outOfSyncMessages.add(msg);
        }

        JSONCompareResult controlMatrixComparison = comparison.children().get("controlMatrix");

        if (!controlMatrixComparison.matching()) {
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
            T.t(outOufSyncPanel, msg.message, true, "oos_control_matrix");
            JButton revertBackBtn = new JButton();
            T.t(outOufSyncPanel, revertBackBtn, true, "cancel_changes");
            revertBackBtn.addActionListener((e) -> {
                controlMatrix.fromJSON(
                        backupJson.getJSONObject("controlMatrix"));
                checkPriorRatingCurveSync();
                fireChangeListeners();
            });
            msg.addButton(revertBackBtn);
            outOfSyncMessages.add(msg);
        } else {

            JSONCompareResult hydraulicControlsComparison = comparison.children().get("hydraulicControls");

            if (!hydraulicControlsComparison.matching()) {
                MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
                T.t(outOufSyncPanel, msg.message, true, "oos_hydraulic_controls");
                JButton revertBackBtn = new JButton();
                T.t(outOufSyncPanel, revertBackBtn, true, "cancel_changes");
                revertBackBtn.addActionListener((e) -> {
                    hydraulicControls.fromJSON(
                            backupJson.getJSONObject("hydraulicControls"));
                    checkPriorRatingCurveSync();
                    fireChangeListeners();
                });
                msg.addButton(revertBackBtn);
                outOfSyncMessages.add(msg);

            }

        }

        for (MsgPanel mp : outOfSyncMessages) {
            outOufSyncPanel.appendChild(mp);
        }
        if (outOfSyncMessages.size() > 0) {
            T.t(runBam, runBam.runButton, true, "recompute_prior_rc");
            runBam.runButton.setForeground(AppSetup.COLORS.INVALID_FG);
        } else {
            T.t(runBam, runBam.runButton, true, "compute_prior_rc");
            runBam.runButton.setForeground(new JButton().getForeground());
        }

        updateUI();

    }

    private void updateHydraulicControls(boolean[][] controlMatrix) {
        hydraulicControls.setHydraulicControls(controlMatrix.length);
    }

    @Override
    public String getModelId() {
        return "BaRatin";
    }

    @Override
    public String[] getParameterNames() {
        Parameter[] parameters = getParameters();
        if (parameters == null) {
            return null;
        }
        String[] parameterNames = new String[parameters.length];
        for (int k = 0; k < parameters.length; k++) {
            parameterNames[k] = parameters[k].name;
        }
        return parameterNames;
    }

    @Override
    public String[] getInputNames() {
        return new String[] { "h" };
    }

    @Override
    public String[] getOutputNames() {
        return new String[] { "Q" };
    }

    @Override
    public String getXtra(String workspace) {
        boolean[][] matrix = this.controlMatrix.getControlMatrix();
        return ControlMatrix.toXtra(matrix);
    }

    @Override
    public Parameter[] getParameters() {
        return hydraulicControls.getParameters();
    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {
        JSONObject json = new JSONObject();

        // **********************************************************
        // Control matrix
        json.put("controlMatrix", controlMatrix.toJSON());

        // **********************************************************
        // Hydraulic controls

        json.put("hydraulicControls", hydraulicControls.toJSON());

        // **********************************************************
        // Stage grid configuration
        JSONObject stageGridConfigJson = priorRatingCurveStageGrid.toJSON();

        json.put("stageGridConfig", stageGridConfigJson);

        // **********************************************************
        String zipPath = null;
        if (bamRunConfigAndRes != null) {
            json.put("bamRunId", bamRunConfigAndRes.id);
            zipPath = bamRunConfigAndRes.zipRun(writeFiles);
        }

        if (backup != null) {
            json.put("backup", BamConfigRecord.toJSON(backup));
        }

        return zipPath == null ? new BamConfigRecord(json) : new BamConfigRecord(json, zipPath);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

        // **********************************************************
        // Control matrix
        if (json.has("controlMatrix")) {
            controlMatrix.fromJSON(json.getJSONObject("controlMatrix"));
        } else {
            ConsoleLogger.log("missing 'controlMatrix'");
        }

        // **********************************************************
        // Hydraulic controls
        if (json.has("hydraulicControls")) {

            hydraulicControls.fromJSON(json.getJSONObject("hydraulicControls"));

        } else {
            ConsoleLogger.log("missing 'hydraulicControls'");
        }

        // **********************************************************
        // Stage grid configuration

        if (json.has("stageGridConfig")) {

            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            priorRatingCurveStageGrid.fromJSON(stageGridJson);

        } else {
            ConsoleLogger.log("missing 'stageGridConfig'");
        }

        if (json.has("backup")) {
            JSONObject backupJson = json.getJSONObject("backup");
            backup = BamConfigRecord.fromJSON(backupJson);

        } else {
            ConsoleLogger.log("missing 'backup'");
        }

        // **********************************************************
        // prior rating curve BaM results
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            bamRunConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            BamProjectLoader.addDelayedAction(() -> {
                buildPlot();
            });
        } else {
            ConsoleLogger.log("missing 'bamRunZipFileName'");
        }

        checkPriorRatingCurveSync();
    }

    @Override
    public PredExpSet getPredExps() {

        PredictionOutput maxpostOutput = PredictionOutput.buildPredictionOutput("maxpost", "Q", false);
        PredictionOutput uParamOutput = PredictionOutput.buildPredictionOutput("uParam", "Q", false);

        PredictionInput predInput = priorRatingCurveStageGrid.getPredictionInput();
        if (predInput == null) {
            ConsoleLogger.warn("No valid rating curve stage grid.");
            return null;
        }

        return new PredExpSet(
                new PredExp(PredictionConfig.buildPriorPrediction(
                        "maxpost",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { maxpostOutput },
                        new PredictionState[] {},
                        false,
                        AppSetup.CONFIG.N_REPLICATES, false)),
                new PredExp(PredictionConfig.buildPriorPrediction(
                        "u",
                        new PredictionInput[] { predInput },
                        new PredictionOutput[] { uParamOutput },
                        new PredictionState[] {},
                        true,
                        AppSetup.CONFIG.N_REPLICATES,
                        false)));

    }

    private void buildPlot() {

        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();
        CalibrationConfig calibrationConfig = bamRunConfigAndRes.getCalibrationConfig();
        CalibrationResult calibrationResults = bamRunConfigAndRes.getCalibrationResults();

        double[] stage = predResults[0].predictionConfig.inputs[0].dataColumns.get(0);
        double[] dischargeMaxpost = predResults[0].outputResults.get(0).spag().get(0);
        List<double[]> paramU = predResults[1].outputResults.get(0).env().subList(1, 3);

        List<EstimatedParameter> parameters = calibrationResults.estimatedParameters;

        boolean[][] controlMatrix = ControlMatrix.fromXtra(calibrationConfig.model.xTra);

        resultsPanel.updateResults(
                stage,
                dischargeMaxpost,
                paramU,
                parameters,
                controlMatrix);

    }
}
