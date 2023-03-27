package org.baratinage.ui.baratin;

// import org.baratinage.jbam.Parameter;
// import org.baratinage.jbam.PredictionConfig;
// import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IModelDefinition;
// import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.component.TitledTextField;
import org.baratinage.ui.container.FlexPanel;
// import org.baratinage.ui.bam.IPredictionData;
// import org.baratinage.ui.bam.IPredictionExperiment;

class HydraulicConfiguration extends BamItem
        implements IModelDefinition {

    static private final String defaultNameTemplate = "Configuration Hydraulique #%s";
    static private int nInstance = 0;

    private String name;
    private String description;

    public HydraulicConfiguration() {
        super();
        HydraulicConfiguration.nInstance++;
        this.name = String.format(
                HydraulicConfiguration.defaultNameTemplate,
                HydraulicConfiguration.nInstance + "");
        this.description = "";
        System.out.println(description);

        TitledTextField nameField = new TitledTextField("Nom de la configuration hydraulique");
        nameField.addChangeListener(nt -> {
            this.name = nt;
            hasChanged();
        });
        nameField.setText(this.name);
        TitledTextField descField = new TitledTextField("Description");
        FlexPanel content = new FlexPanel();
        this.appendChild(nameField);
        this.appendChild(descField);
        this.appendChild(content, 1);

        content.appendChild(new ControlMatrix(), 1);
    }

    public void setName(String name) {
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
