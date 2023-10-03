package org.baratinage.ui.baratin;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PriorPredictionExperiment;
import org.baratinage.ui.bam.RunConfigAndRes;
import org.baratinage.ui.bam.RunPanel;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.JSONcomparator;
import org.json.JSONArray;
import org.json.JSONObject;

public class HydraulicConfiguration
        extends BamItem
        implements IModelDefinition, IPriors, IPredictionMaster {

    private ControlMatrix controlMatrix;
    private HydraulicControlPanels hydraulicControls;
    private RatingCurveStageGrid priorRatingCurveStageGrid;

    private RowColPanel outOufSyncPanel;

    private RunPanel runPanel;
    private PriorRatingCurvePlot plotPanel;
    private RunConfigAndRes bamRunConfigAndRes;

    private String jsonStringBackup;

    public static final ImageIcon controlMatrixIcon = SvgIcon.buildCustomAppImageIcon("control_matrix.svg");
    public static final ImageIcon priorSpecificationIcon = SvgIcon.buildCustomAppImageIcon("prior_densities.svg");
    public static final ImageIcon priorRatingCurveIcon = SvgIcon.buildCustomAppImageIcon("prior_rating_curve.svg");

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

        plotPanel = new PriorRatingCurvePlot();

        Dimension dim = new Dimension(400, 300);
        controlMatrix.setPreferredSize(dim);
        plotPanel.setPreferredSize(dim);

        runPanel = new RunPanel(false, true, false);
        runPanel.setModelDefintion(this);
        runPanel.setPriors(this);
        runPanel.setPredictionExperiments(this);
        runPanel.addRunSuccessListerner((RunConfigAndRes res) -> {
            bamRunConfigAndRes = res;
            jsonStringBackup = toJSON().toString();
            buildPlot();
            checkPriorRatingCurveSync();
        });

        RowColPanel priorRatingCurvePanel = new RowColPanel(RowColPanel.AXIS.COL);

        outOufSyncPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outOufSyncPanel.setPadding(5);

        priorRatingCurvePanel.appendChild(priorRatingCurveStageGrid, 0);
        priorRatingCurvePanel.appendChild(new JSeparator(), 0);
        priorRatingCurvePanel.appendChild(outOufSyncPanel, 0);
        priorRatingCurvePanel.appendChild(runPanel, 0);
        priorRatingCurvePanel.appendChild(plotPanel, 1);

        // **********************************************************************
        // SPLIT PANE APPROACH

        // Font LARGE_BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

        RowColPanel controlMatrixContainer = new RowColPanel(RowColPanel.AXIS.COL);
        Title controlMatrixTitle = new Title(controlMatrixIcon, "");

        controlMatrixContainer.appendChild(controlMatrixTitle, 0);
        controlMatrixContainer.appendChild(controlMatrix, 1);

        RowColPanel priorRCplotPanel = new RowColPanel(RowColPanel.AXIS.COL);
        Title priorRCplotTitle = new Title(priorRatingCurveIcon, "");

        priorRCplotTitle.setIcon(priorRatingCurveIcon);
        priorRCplotPanel.appendChild(priorRCplotTitle, 0);
        priorRCplotPanel.appendChild(priorRatingCurvePanel, 1);

        RowColPanel priorSepecificationPanel = new RowColPanel(RowColPanel.AXIS.COL);
        Title priorSpecificationTitle = new Title(priorSpecificationIcon, "");

        priorSpecificationTitle.setIcon(priorSpecificationIcon);
        priorSepecificationPanel.appendChild(priorSpecificationTitle, 0);
        priorSepecificationPanel.appendChild(hydraulicControls, 1);

        SplitContainer mainContainer = SplitContainer.build2Left1RightSplitContainer(
                controlMatrixContainer,
                priorRCplotPanel,
                priorSepecificationPanel);

        Lg.register(mainContainer, () -> {
            controlMatrixTitle.setText(Lg.html("control_matrix"));
            priorSpecificationTitle.setText(Lg.html("prior_parameter_specification"));
            priorRCplotTitle.setText(Lg.html("prior_rating_curve"));
        });

        // **********************************************************************
        // TAB SYSTEM APPROACH

        // SimpleTabContainer mainContainer = new SimpleTabContainer();
        // mainContainer.addTab("control_matrix", controlMatrixIcon, controlMatrix);
        // mainContainer.addTab("prior_parameter_specification", priorSpecificationIcon,
        // hydraulicControls);
        // mainContainer.addTab("prior_rating_curve", priorRatingCurveIcon,
        // priorRatingCurvePanel);

        // Lg.register(mainContainer, () -> {
        // mainContainer.setTitleTextAt(0, Lg.html("control_matrix"));
        // mainContainer.setTitleTextAt(1, Lg.html("prior_parameter_specification"));
        // mainContainer.setTitleTextAt(2, Lg.html("prior_rating_curve"));
        // });

        setContent(mainContainer);

        boolean[][] mat = controlMatrix.getControlMatrix();
        updateHydraulicControls(mat);
    }

    private void checkPriorRatingCurveSync() {
        outOufSyncPanel.clear();
        if (jsonStringBackup == null) {
            Lg.register(runPanel.runButton, "compute_prior_rc", true);
            runPanel.runButton.setForeground(new JButton().getForeground());
            updateUI();
            return;
        }

        JSONObject currentJson = toJSON();
        JSONObject backupJson = new JSONObject(jsonStringBackup);
        Map<String, Boolean> matching = JSONcomparator.areMatchingByEntry(currentJson, backupJson);

        List<MsgPanel> outOfSyncMessages = new ArrayList<>();

        if (!matching.get("stageGridConfig")) {
            System.out.println("Stage grid config different");
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
            msg.message.setText("oos_stage_grid");
            JButton revertBackBtn = new JButton();
            revertBackBtn.setText("cancel_changes");
            revertBackBtn.addActionListener((e) -> {
                priorRatingCurveStageGrid.fromJSON(
                        backupJson.getJSONObject("stageGridConfig"));
                checkPriorRatingCurveSync();
            });
            msg.addButton(revertBackBtn);
            outOfSyncMessages.add(msg);
        }
        if (!matching.get("controlMatrix")) {
            System.out.println("Control matrix different");
            MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
            msg.message.setText("oos_control_matrix");
            JButton revertBackBtn = new JButton();
            revertBackBtn.setText("cancel_changes");
            revertBackBtn.addActionListener((e) -> {
                controlMatrix.fromJSON(
                        backupJson.getJSONObject("controlMatrix"));
                checkPriorRatingCurveSync();
            });
            msg.addButton(revertBackBtn);
            outOfSyncMessages.add(msg);
        } else {
            // check priors only if matrix control match
            // otherwise, likely to run into some issues
            // e.g. I want to make sure the number of control matches
            if (!matching.get("hydraulicControls")) {

                JSONArray currentControls = currentJson.getJSONObject("hydraulicControls").getJSONArray("controls");
                JSONArray backupControls = backupJson.getJSONObject("hydraulicControls").getJSONArray("controls");
                boolean controlsMatching = true;
                for (int k = 0; k < currentControls.length(); k++) {
                    Map<String, Boolean> matchingControl = JSONcomparator.areMatchingByEntry(
                            currentControls.getJSONObject(k),
                            backupControls.getJSONObject(k));
                    if (!matchingControl.get("kacControl")) {
                        controlsMatching = false;
                        break;
                    }
                }
                if (!controlsMatching) {
                    System.out.println("Hydraulic controls are different");
                    MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR);
                    msg.message.setText("oos_hydraulic_controls");
                    JButton revertBackBtn = new JButton();
                    revertBackBtn.setText("cancel_changes");
                    revertBackBtn.addActionListener((e) -> {
                        hydraulicControls.fromJSON(
                                backupJson.getJSONObject("hydraulicControls"));
                        checkPriorRatingCurveSync();
                    });
                    msg.addButton(revertBackBtn);
                    outOfSyncMessages.add(msg);
                }

            }
        }
        for (MsgPanel mp : outOfSyncMessages) {
            outOufSyncPanel.appendChild(mp);
        }
        if (outOfSyncMessages.size() > 0) {
            Lg.register(runPanel.runButton, "recompute_prior_rc", true);
            runPanel.runButton.setForeground(AppConfig.AC.INVALID_COLOR_FG);
        } else {
            Lg.register(runPanel.runButton, "compute_prior_rc", true);
            runPanel.runButton.setForeground(new JButton().getForeground());
        }
        Lg.register(outOufSyncPanel, () -> {

        });

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
        String xtra = "";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                xtra += matrix[i][j] ? "1 " : "0 ";
            }
            if ((i + 1) != matrix.length) {
                xtra += "\n";
            }
        }
        return xtra;
    }

    @Override
    public Parameter[] getParameters() {
        return hydraulicControls.getParameters();
    }

    @Override
    public JSONObject toJSON() {
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

        json.put("jsonStringBackup", jsonStringBackup);

        // **********************************************************

        if (bamRunConfigAndRes != null) {
            json.put("bamRunId", bamRunConfigAndRes.id);
            String zipPath = bamRunConfigAndRes.zipRun();
            registerFile(zipPath);
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        // **********************************************************
        // Control matrix
        if (json.has("controlMatrix")) {
            controlMatrix.fromJSON(json.getJSONObject("controlMatrix"));
        } else {
            System.out.println("HydraulicConfiguration: missing 'controlMatrix'");
        }

        // **********************************************************
        // Hydraulic controls
        if (json.has("hydraulicControls")) {

            hydraulicControls.fromJSON(json.getJSONObject("hydraulicControls"));

        } else {
            System.out.println("HydraulicConfiguration: missing 'hydraulicControls'");
        }

        // **********************************************************
        // Stage grid configuration

        if (json.has("stageGridConfig")) {

            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            priorRatingCurveStageGrid.fromJSON(stageGridJson);

        } else {
            System.out.println("HydraulicConfiguration: missing 'stageGridConfig'");
        }

        if (json.has("jsonStringBackup")) {
            jsonStringBackup = json.getString("jsonStringBackup");
        } else {
            System.out.println("HydraulicConfiguration: missing 'jsonStringBackup'");
        }

        // **********************************************************
        // prior rating curve BaM results
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            bamRunConfigAndRes = RunConfigAndRes.buildFromTempZipArchive(bamRunId);
            buildPlot();
        } else {
            System.out.println("HydraulicConfiguration: missing 'bamRunZipFileName'");
        }

        checkPriorRatingCurveSync();
    }

    @Override
    public HydraulicConfiguration clone(String uuid) {
        HydraulicConfiguration cloned = new HydraulicConfiguration(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

    @Override
    public IPredictionExperiment[] getPredictionExperiments() {
        int nReplicates = 500;
        PriorPredictionExperiment ppeMaxpost = new PriorPredictionExperiment("maxpost",
                false, nReplicates,
                this, priorRatingCurveStageGrid);

        PriorPredictionExperiment ppeParamUncertainty = new PriorPredictionExperiment(
                "parametricUncertainty",
                true, nReplicates,
                this, priorRatingCurveStageGrid);

        IPredictionExperiment[] predictionExperiments = new PriorPredictionExperiment[] {
                ppeMaxpost,
                ppeParamUncertainty
        };
        return predictionExperiments;
    }

    private void buildPlot() {

        PredictionConfig[] predConfigs = bamRunConfigAndRes.getPredictionConfigs();
        PredictionResult[] predResults = bamRunConfigAndRes.getPredictionResults();
        Parameter[] params = bamRunConfigAndRes.getCalibrationConfig().model.parameters;

        double[] stage = predConfigs[0].inputs[0].dataColumns.get(0);
        String outputName = predConfigs[0].outputs[0].name;

        double[] dischargeMaxpost = predResults[0].outputResults.get(outputName).spag().get(0);
        List<double[]> dischargeParamU = predResults[1].outputResults.get(outputName).env().subList(1, 3);

        List<double[]> transitionStages = new ArrayList<>();
        for (Parameter p : params) {
            if (p.name.startsWith("k_")) {
                Distribution d = p.distribution;
                if (d.type == DistributionType.GAUSSIAN) {
                    double[] distParams = d.parameterValues;
                    double mean = distParams[0];
                    double std = distParams[1];
                    transitionStages.add(new double[] {
                            mean, mean - 2 * std, mean + 2 * std
                    });
                }

            }
        }

        plotPanel.updatePlot(
                stage, dischargeMaxpost, dischargeParamU, transitionStages);
    }
}
