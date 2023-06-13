package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.AllHydraulicControls;
import org.baratinage.ui.baratin.hydraulic_control.OneHydraulicControl;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONArray;
import org.json.JSONObject;

class HydraulicConfiguration extends BaRatinItem
        implements IModelDefinition, IPriors {

    static private final String defaultNameTemplate = "Configuration Hydraulique #%s";
    static private int nInstance = 0;

    private ControlMatrix controlMatrix;
    private AllHydraulicControls hydraulicControls;
    private RatingCurveStageGrid priorRatingCurveStageGrid;
    private PriorRatingCurve priorRatingCurve;

    public HydraulicConfiguration(String uuid) {
        super(ITEM_TYPE.HYRAULIC_CONFIG, uuid);

        setName(String.format(
                defaultNameTemplate,
                nInstance + 1 + ""));
        setDescription("");

        setNameFieldLabel("Nom de la configuration hydraulique");
        setDescriptionFieldLabel("Description de la configuration hydraulique");

        controlMatrix = new ControlMatrix();
        controlMatrix.addPropertyChangeListener("controlMatrix", (e) -> {
            hasChanged();
            updateHydraulicControls(controlMatrix.getControlMatrix());
        });

        hydraulicControls = new AllHydraulicControls();
        hydraulicControls.addPropertyChangeListener("hydraulicControls", (e) -> {
            hasChanged();
        });

        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        splitPaneContainer.setLeftComponent(controlMatrix);
        splitPaneContainer.setRightComponent(hydraulicControls);
        splitPaneContainer.setResizeWeight(0.5);

        RowColPanel priorRatingCurvePanel = new RowColPanel(RowColPanel.AXIS.COL);
        priorRatingCurve = new PriorRatingCurve(
                priorRatingCurveStageGrid,
                this, this);

        priorRatingCurveStageGrid = new RatingCurveStageGrid();
        priorRatingCurve.setPredictionDataProvider(priorRatingCurveStageGrid);

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
    public void parentHasChanged(BamItem parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parentHasChanged'");
    }

    @Override
    public String[] getTempDataFileNames() {
        String priorRatingCurveZipFileName = priorRatingCurve.getBamRunZipFileName();
        return priorRatingCurveZipFileName == null ? new String[] {} : new String[] { priorRatingCurveZipFileName };
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

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
            jsonHydraulicControl.put("name", hc.getName());
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

        // **********************************************************
        // prior rating curve BaM results
        json.put("bamRunZipFileName", priorRatingCurve.getBamRunZipFileName());

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        // **********************************************************
        // Control matrix
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

        // **********************************************************
        // Hydraulic controls
        JSONArray jsonHydraulicControls = (JSONArray) json.get("hydraulicControls");

        List<OneHydraulicControl> hydraulicControlList = new ArrayList<>();

        for (int k = 0; k < jsonHydraulicControls.length(); k++) {
            JSONObject jsonHydraulicControl = (JSONObject) jsonHydraulicControls.get(k);

            OneHydraulicControl hydraulicControl = new OneHydraulicControl();
            hydraulicControl.setName((String) jsonHydraulicControl.get("name"));
            hydraulicControl.setActivationStage(((Number) jsonHydraulicControl.get("activationStage")).doubleValue());
            hydraulicControl
                    .setActivationStageUncertainty(
                            ((Number) jsonHydraulicControl.get("activationStageUncertainty")).doubleValue());
            hydraulicControl.setCoefficient(((Number) jsonHydraulicControl.get("coefficient")).doubleValue());
            hydraulicControl.setCoefficientUncertainty(
                    ((Number) jsonHydraulicControl.get("coefficientUncertainty")).doubleValue());
            hydraulicControl.setExponent(((Number) jsonHydraulicControl.get("exponent")).doubleValue());
            hydraulicControl
                    .setExponentUncertainty(((Number) jsonHydraulicControl.get("exponentUncertainty")).doubleValue());

            hydraulicControlList.add(hydraulicControl);
        }

        hydraulicControls.setHydraulicControls(hydraulicControlList);

        // **********************************************************
        // Stage grid configuration

        JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
        priorRatingCurveStageGrid.setMinValue(stageGridJson.getDouble("min"));
        priorRatingCurveStageGrid.setMaxValue(stageGridJson.getDouble("max"));
        priorRatingCurveStageGrid.setStepValue(stageGridJson.getDouble("step"));

        // **********************************************************
        // prior rating curve BaM results
        String bamRunZipFileName = json.getString("bamRunZipFileName");
        priorRatingCurve.setBamRunZipFileName(bamRunZipFileName);

    }

}
