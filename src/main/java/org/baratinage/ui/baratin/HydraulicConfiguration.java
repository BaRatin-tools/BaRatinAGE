package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.AllHydraulicControls;
import org.baratinage.ui.baratin.hydraulic_control.OneHydraulicControl;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.LgElement;
import org.json.JSONArray;
import org.json.JSONObject;

class HydraulicConfiguration extends BamItem

        implements IModelDefinition, IPriors {

    private ControlMatrix controlMatrix;
    private AllHydraulicControls hydraulicControls;
    private RatingCurveStageGrid priorRatingCurveStageGrid;
    private PriorRatingCurve priorRatingCurve;

    private String jsonStringBackup;

    public HydraulicConfiguration(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG, uuid, project);

        controlMatrix = new ControlMatrix();
        controlMatrix.addChangeListener((e) -> {
            fireChangeListeners();
            updateHydraulicControls(controlMatrix.getControlMatrix());
            checkPriorRatingCurveSync();
        });

        hydraulicControls = new AllHydraulicControls();
        hydraulicControls.addChangeListener((e) -> {
            fireChangeListeners();
            checkPriorRatingCurveSync();
        });

        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        splitPaneContainer.setLeftComponent(controlMatrix);
        splitPaneContainer.setRightComponent(hydraulicControls);
        splitPaneContainer.setResizeWeight(0.5);

        priorRatingCurveStageGrid = new RatingCurveStageGrid();
        priorRatingCurveStageGrid.addChangeListener((e) -> {
            fireChangeListeners();
            checkPriorRatingCurveSync();
        });

        RowColPanel priorRatingCurvePanel = new RowColPanel(RowColPanel.AXIS.COL);
        priorRatingCurve = new PriorRatingCurve();
        priorRatingCurve.runButton.addActionListener((e) -> {
            JSONObject json = toJSON();
            json.remove("jsonStringBackup");
            jsonStringBackup = json.toString();
            checkPriorRatingCurveSync();
        });
        priorRatingCurve.setPredictionDataProvider(priorRatingCurveStageGrid);
        priorRatingCurve.setPriorsProvider(this);
        priorRatingCurve.setModelDefintionProvider(this);

        priorRatingCurvePanel.appendChild(priorRatingCurveStageGrid, 0);
        priorRatingCurvePanel.appendChild(new JSeparator(), 0);
        priorRatingCurvePanel.appendChild(priorRatingCurve, 1);

        JSplitPane mainSplitPaneContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        mainSplitPaneContainer.setLeftComponent(splitPaneContainer);
        mainSplitPaneContainer.setRightComponent(priorRatingCurvePanel);

        setContent(mainSplitPaneContainer);

        boolean[][] mat = controlMatrix.getControlMatrix();
        updateHydraulicControls(mat);
    }

    private void checkPriorRatingCurveSync() {
        if (jsonStringBackup != null) {
            String[] keysToIgnore = new String[] { "ui", "name", "description", "jsonStringBackup" };
            if (!isMatchingWith(jsonStringBackup, keysToIgnore, true)) {
                WarningAndActions warning = new WarningAndActions();
                LgElement.registerLabel(warning.message, "ui", "oos_prior_rating_curve", true);
                priorRatingCurve.setWarnings(new WarningAndActions[] { warning });
            } else {
                priorRatingCurve.setWarnings(new WarningAndActions[] {});
            }
        }
    }

    private void updateHydraulicControls(boolean[][] controlMatrix) {
        hydraulicControls.updateHydraulicControlListFromNumberOfControls(controlMatrix.length);
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
            parameterNames[k] = parameters[k].getName();
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
    public String[] getTempDataFileNames() {
        RunBam runBam = priorRatingCurve.getRunBam();
        return runBam == null ? new String[] {} : new String[] { runBam.zipName };
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
        List<OneHydraulicControl> hydraulicControlList = hydraulicControls.getHydraulicControls();

        JSONArray jsonHydraulicControls = new JSONArray();
        for (OneHydraulicControl hc : hydraulicControlList) {
            JSONObject jsonHydraulicControl = new JSONObject();
            jsonHydraulicControl.put("name", hc.nameLabel.getText());
            jsonHydraulicControl.put("activationStage", hc.getActivationStage());
            jsonHydraulicControl.put("activationStageUncertainty", hc.getActivationStageUncertainty());
            jsonHydraulicControl.put("coefficient", hc.getCoefficient());
            jsonHydraulicControl.put("coefficientUncertainty", hc.getCoefficientUncertainty());
            jsonHydraulicControl.put("exponent", hc.getExponent());
            jsonHydraulicControl.put("exponentUncertainty", hc.getExponentUncertainty());

            jsonHydraulicControls.put(jsonHydraulicControl);
        }

        json.put("hydraulicControls", jsonHydraulicControls);

        // **********************************************************
        // Stage grid configuration
        JSONObject stageGridConfigJson = new JSONObject();
        stageGridConfigJson.put("min", priorRatingCurveStageGrid.getMinValue());
        stageGridConfigJson.put("max", priorRatingCurveStageGrid.getMaxValue());
        stageGridConfigJson.put("step", priorRatingCurveStageGrid.getStepValue());

        json.put("stageGridConfig", stageGridConfigJson);

        json.put("jsonStringBackup", jsonStringBackup);

        // **********************************************************
        // prior rating curve BaM results
        RunBam runBam = priorRatingCurve.getRunBam();
        if (runBam != null) {
            json.put("bamRunId", runBam.id);
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
            System.out.println("MISSING 'ui'");
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
            System.out.println("MISSING 'controlMatrix'");
        }

        // **********************************************************
        // Hydraulic controls
        if (json.has("hydraulicControls")) {
            JSONArray jsonHydraulicControls = (JSONArray) json.get("hydraulicControls");

            List<OneHydraulicControl> hydraulicControlList = new ArrayList<>();

            for (int k = 0; k < jsonHydraulicControls.length(); k++) {
                JSONObject jsonHydraulicControl = (JSONObject) jsonHydraulicControls.get(k);

                OneHydraulicControl hydraulicControl = new OneHydraulicControl();
                hydraulicControl.nameLabel.setText((String) jsonHydraulicControl.get("name"));
                hydraulicControl
                        .setActivationStage(((Number) jsonHydraulicControl.get("activationStage")).doubleValue());
                hydraulicControl
                        .setActivationStageUncertainty(
                                ((Number) jsonHydraulicControl.get("activationStageUncertainty")).doubleValue());
                hydraulicControl.setCoefficient(((Number) jsonHydraulicControl.get("coefficient")).doubleValue());
                hydraulicControl.setCoefficientUncertainty(
                        ((Number) jsonHydraulicControl.get("coefficientUncertainty")).doubleValue());
                hydraulicControl.setExponent(((Number) jsonHydraulicControl.get("exponent")).doubleValue());
                hydraulicControl
                        .setExponentUncertainty(
                                ((Number) jsonHydraulicControl.get("exponentUncertainty")).doubleValue());

                hydraulicControlList.add(hydraulicControl);
            }

            hydraulicControls.setHydraulicControls(hydraulicControlList);

        } else {
            System.out.println("MISSING 'hydraulicControls'");
        }
        // **********************************************************
        // Stage grid configuration

        if (json.has("stageGridConfig")) {

            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            priorRatingCurveStageGrid.setMinValue(stageGridJson.getDouble("min"));
            priorRatingCurveStageGrid.setMaxValue(stageGridJson.getDouble("max"));
            priorRatingCurveStageGrid.setStepValue(stageGridJson.getDouble("step"));

        } else {
            System.out.println("MISSING 'stageGridConfig'");
        }

        if (json.has("jsonStringBackup")) {
            jsonStringBackup = json.getString("jsonStringBackup");
        } else {
            System.out.println("MISSING 'jsonStringBackup'");
        }

        // **********************************************************
        // prior rating curve BaM results
        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            priorRatingCurve.setRunBam(bamRunId);
        } else {
            System.out.println("MISSING 'bamRunZipFileName'");
        }

        checkPriorRatingCurveSync();
    }

    @Override
    public HydraulicConfiguration clone(String uuid) {
        HydraulicConfiguration cloned = new HydraulicConfiguration(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}
