package org.baratinage.ui.baratin;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import org.baratinage.jbam.Parameter;
// import org.baratinage.jbam.Parameter;
// import org.baratinage.jbam.PredictionConfig;
// import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;

class HydraulicConfiguration extends BaRatinItem
        implements IModelDefinition, IPriors {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);
    static private final String defaultNameTemplate = "Configuration Hydraulique #%s";
    static private int nInstance = 0;

    private ControlMatrix controlMatrix;
    private HydraulicControls hydraulicControls;

    public HydraulicConfiguration() {
        super(TYPE);

        nInstance++;
        setName(String.format(
                defaultNameTemplate,
                nInstance + ""));
        setDescription("");

        setNameFieldLabel("Nom de la configuration hydraulique");
        setDescriptionFieldLabel("Description de la configuration hydraulique");

        controlMatrix = new ControlMatrix();
        controlMatrix.addFollower((o) -> {
            System.out.println("Control matrix has changed!");
            hasChanged();
            updateControls(controlMatrix.getControlMatrix());
        });

        hydraulicControls = new HydraulicControls();
        hydraulicControls.addFollower((o) -> {
            System.out.println("Hydraulic controls have changed!");
            hasChanged();
        });

        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        splitPaneContainer.setLeftComponent(controlMatrix);
        splitPaneContainer.setRightComponent(hydraulicControls);
        splitPaneContainer.setResizeWeight(0.5);

        setContent(splitPaneContainer);

        updateControls(controlMatrix.getControlMatrix());

    }

    private void updateControls(boolean[][] controlMatrix) {
        hydraulicControls.setNumberOfControls(controlMatrix.length);

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
            xtra += "\n";
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
    public String toJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJsonString'");
    }

    @Override
    public void fromJsonString(String jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJsonString'");
    }

}
