package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.json.JSONArray;
import org.json.JSONObject;

class HydraulicConfiguration extends BaRatinItem
        implements IModelDefinition, IPriors {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);
    static private final String defaultNameTemplate = "Configuration Hydraulique #%s";
    static private int nInstance = 0;

    private ControlMatrix controlMatrix;
    private HydraulicControls hydraulicControls;

    public HydraulicConfiguration() {
        this(String.format(
                defaultNameTemplate,
                nInstance + 1 + ""));
        nInstance++;
    }

    public HydraulicConfiguration(String name) {
        super(TYPE);

        setName(name);
        setDescription("");

        setNameFieldLabel("Nom de la configuration hydraulique");
        setDescriptionFieldLabel("Description de la configuration hydraulique");

        controlMatrix = new ControlMatrix();
        controlMatrix.addPropertyChangeListener("controlMatrix", (e) -> {
            hasChanged();
            updateHydraulicControls(controlMatrix.getControlMatrix());
        });

        hydraulicControls = new HydraulicControls();
        hydraulicControls.addPropertyChangeListener("hydraulicControls", (e) -> {
            hasChanged();
        });

        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        splitPaneContainer.setLeftComponent(controlMatrix);
        splitPaneContainer.setRightComponent(hydraulicControls);
        splitPaneContainer.setResizeWeight(0.5);

        setContent(splitPaneContainer);

        updateHydraulicControls(controlMatrix.getControlMatrix());

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
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getName());

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
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        setName((String) json.get("name"));
        setDescription((String) json.get("description"));

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
        controlMatrix.setFromBooleanMatrix(matrix);

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
    }

}
