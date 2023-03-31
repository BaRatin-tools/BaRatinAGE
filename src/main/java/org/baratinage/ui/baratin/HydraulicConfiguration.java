package org.baratinage.ui.baratin;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

// import org.baratinage.jbam.Parameter;
// import org.baratinage.jbam.PredictionConfig;
// import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.component.TextField;
// import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

class HydraulicConfiguration extends BamItem
        implements IModelDefinition {

    static private final String defaultNameTemplate = "Configuration Hydraulique #%s";
    static private int nInstance = 0;

    private String name;
    private String description;

    private ControlMatrix controlMatrix;

    private HydraulicControls hydraulicControls;

    public HydraulicConfiguration() {
        super();
        HydraulicConfiguration.nInstance++;
        this.name = String.format(
                HydraulicConfiguration.defaultNameTemplate,
                HydraulicConfiguration.nInstance + "");
        this.description = "";
        System.out.println(description);

        GridPanel header = new GridPanel();
        header.setGap(5);
        header.setPadding(5);
        header.setRowWeight(0, 1);
        header.setColWeight(1, 1);

        JLabel nameFieldLabel = new JLabel("Nom de la configuration hydraulique");
        TextField nameField = new TextField();
        nameField.addChangeListener(nt -> {
            setName(nt);
            hasChanged();
        });
        nameField.setText(this.name);
        JLabel descFieldLabel = new JLabel("Description");
        TextField descField = new TextField();

        header.insertChild(nameFieldLabel, 0, 0);
        header.insertChild(nameField, 1, 0);
        header.insertChild(descFieldLabel, 0, 1);
        header.insertChild(descField, 1, 1);

        controlMatrix = new ControlMatrix();
        controlMatrix.addChangeListener(matrix -> {
            System.out.println("Control matrix has changed!");
            updateControls(matrix);
        });
        hydraulicControls = new HydraulicControls();

        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        splitPaneContainer.setLeftComponent(controlMatrix);
        splitPaneContainer.setRightComponent(hydraulicControls);
        splitPaneContainer.setResizeWeight(0.5);

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);
        content.appendChild(header, 0);
        content.appendChild(splitPaneContainer, 1);
        content.setGap(5);

        setContent(content);

        updateControls(controlMatrix.getControlMatrix());
    }

    private void updateControls(boolean[][] controlMatrix) {
        ControlMatrix.printMatrix(controlMatrix);
        hydraulicControls.setNumberOfControls(controlMatrix.length);

    }

    public void setName(String name) {
        setTitle(name);
        this.name = name;
    }

    @Override
    public String getModelId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelId'");
    }

    @Override
    public String[] getParameterNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameterNames'");
    }

    @Override
    public String[] getInputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputNames'");
    }

    @Override
    public String[] getOutputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputNames'");
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getXtra(String workspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getXtra'");
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
