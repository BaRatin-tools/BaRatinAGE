package org.baratinage.ui.baratin;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
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
import org.baratinage.ui.component.SimpleTabContainer;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

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

    public static final ImageIcon controlMatrixIcon = SvgIcon.buildCustomAppImageIcon("control_matrix.svg",
            AppConfig.AC.ICON_SIZE);
    public static final ImageIcon priorSpecificationIcon = SvgIcon.buildCustomAppImageIcon("prior_densities.svg",
            AppConfig.AC.ICON_SIZE);
    public static final ImageIcon priorRatingCurveIcon = SvgIcon.buildCustomAppImageIcon("prior_rating_curve.svg",
            AppConfig.AC.ICON_SIZE);

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
            fireChangeListeners();
            checkPriorRatingCurveSync();
        });

        plotPanel = new PriorRatingCurvePlot();

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

        JSplitPane mainContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane leftContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        leftContainer.setTopComponent(controlMatrixContainer);
        leftContainer.setBottomComponent(priorRCplotPanel);
        mainContainer.setLeftComponent(leftContainer);
        mainContainer.setRightComponent(priorSepecificationPanel);

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
        if (jsonStringBackup != null) {
            String[] keysToIgnore = new String[] { "ui", "name", "description", "jsonStringBackup" };
            if (!isMatchingWith(jsonStringBackup, keysToIgnore, true)) {
                MsgPanel errMsg = new MsgPanel(MsgPanel.TYPE.ERROR);
                Lg.register(errMsg.message, "oos_prior_rating_curve", true);
                outOufSyncPanel.appendChild(errMsg);
                Lg.register(runPanel.runButton, "recompute_prior_rc", true);
                runPanel.runButton.setForeground(AppConfig.AC.INVALID_COLOR_FG);
                return;
            }
        }
        Lg.register(runPanel.runButton, "compute_prior_rc", true);
        runPanel.runButton.setForeground(new JButton().getForeground());
        outOufSyncPanel.updateUI();
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
        // ui only elements
        JSONObject uiJson = new JSONObject();
        uiJson.put("reversedControlMatrix", controlMatrix.getIsReversed());
        json.put("ui", uiJson);

        // **********************************************************
        // Control matrix
        boolean[][] matrix = controlMatrix.getControlMatrix();
        String stringMatrix = "";
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                stringMatrix += matrix[i][j] ? "0" : "1";
            }
            stringMatrix += ";";
        }
        json.put("controlMatrix", stringMatrix);

        // **********************************************************
        // Hydraulic controls

        json.put("hydraulicControls", hydraulicControls.toJSON());

        // **********************************************************
        // Stage grid configuration
        JSONObject stageGridConfigJson = new JSONObject();
        stageGridConfigJson.put("min", priorRatingCurveStageGrid.getMinValue());
        stageGridConfigJson.put("max", priorRatingCurveStageGrid.getMaxValue());
        stageGridConfigJson.put("step", priorRatingCurveStageGrid.getStepValue());

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
        // ui only elements
        if (json.has("ui")) {
            JSONObject uiJson = json.getJSONObject("ui");
            controlMatrix.setIsReversed(uiJson.getBoolean("reversedControlMatrix"));
        } else {
            System.out.println("HydraulicConfiguration: missing 'ui'");
        }

        // **********************************************************
        // Control matrix
        if (json.has("controlMatrix")) {
            String stringMatrix = (String) json.get("controlMatrix");
            String[] stringMatrixRow = stringMatrix.split(";");
            int n = stringMatrixRow.length;
            boolean[][] matrix = new boolean[n][n];
            char one = "1".charAt(0);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = stringMatrixRow[i].charAt(j) != one;
                }
            }
            controlMatrix.setControlMatrix(matrix);
        } else {
            System.out.println("HydraulicConfiguration: missing 'controlMatrix'");
        }

        // **********************************************************
        // Hydraulic controls
        if (json.has("hydraulicControls")) {
            // JSONArray jsonHydraulicControls = (JSONArray) json.get("hydraulicControls");

            // List<OneHydraulicControl> hydraulicControlList = new ArrayList<>();

            // for (int k = 0; k < jsonHydraulicControls.length(); k++) {
            // JSONObject jsonHydraulicControl = (JSONObject) jsonHydraulicControls.get(k);

            // OneHydraulicControl ohc = new OneHydraulicControl(k + 1);

            // //
            // ohc.activationStage.setValue(jsonHydraulicControl.getDouble("activationStage"));
            // //
            // ohc.activationStageUncertainty.setValue(jsonHydraulicControl.getDouble("activationStageUncertainty"));
            // // ohc.coefficient.setValue(jsonHydraulicControl.getDouble("coefficient"));
            // //
            // ohc.coefficientUncertainty.setValue(jsonHydraulicControl.getDouble("coefficientUncertainty"));
            // // ohc.exponent.setValue(jsonHydraulicControl.getDouble("exponent"));
            // //
            // ohc.exponentUncertainty.setValue(jsonHydraulicControl.getDouble("exponentUncertainty"));

            // hydraulicControlList.add(ohc);
            // }

            // hydraulicControls.setHydraulicControls(hydraulicControlList);
            hydraulicControls.fromJSON(json.getJSONObject("hydraulicControls"));

        } else {
            System.out.println("HydraulicConfiguration: missing 'hydraulicControls'");
        }
        // **********************************************************
        // Stage grid configuration

        if (json.has("stageGridConfig")) {

            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            priorRatingCurveStageGrid.setMinValue(stageGridJson.getDouble("min"));
            priorRatingCurveStageGrid.setMaxValue(stageGridJson.getDouble("max"));
            priorRatingCurveStageGrid.setStepValue(stageGridJson.getDouble("step"));

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
                if (d.distribution == DISTRIBUTION.GAUSSIAN) {
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
