package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.Distribution.DISTRIB;

public class LinearStructuralErrorModel extends AbstractStructuralErrorModel {

    private ParameterPriorDist g1parameter;
    private ParameterPriorDist g2parameter;

    private String gamma1LabelString = "<html>&gamma;<sub>1</sub></html>";
    private String gamma2LabelString = "<html>&gamma;<sub>2</sub></html>";

    public LinearStructuralErrorModel() {
        super();

        setColWeight(3, 1);
        setGap(5);
        setPadding(5);

        g1parameter = new ParameterPriorDist();
        g1parameter.nameLabel.setText(gamma1LabelString);

        insertChild(g1parameter.nameLabel, 0, 1);
        insertChild(g1parameter.initialGuessField, 1, 1);
        insertChild(g1parameter.distComboBox, 2, 1);
        insertChild(g1parameter.parametersInputsPanel, 3, 1);

        g2parameter = new ParameterPriorDist();
        g2parameter.nameLabel.setText(gamma2LabelString);

        insertChild(g2parameter.nameLabel, 0, 2);
        insertChild(g2parameter.initialGuessField, 1, 2);
        insertChild(g2parameter.distComboBox, 2, 2);
        insertChild(g2parameter.parametersInputsPanel, 3, 2);

    }

    @Override
    public void applyDefaultConfig() {
        Parameter g1 = new Parameter(gamma1LabelString, 1, new Distribution(DISTRIB.UNIFORM, 0, 1000));
        Parameter g2 = new Parameter(gamma2LabelString, 0.1, new Distribution(DISTRIB.UNIFORM, 0, 1000));
        g1parameter.setFromParameter(g1);
        g2parameter.setFromParameter(g2);
        repaint();
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        Parameter[] modelParameters = new Parameter[] {
                g1parameter.getParameter(),
                g2parameter.getParameter()
        };
        StructuralErrorModel structuralErrorModel = new StructuralErrorModel("linear_model", "Linear",
                modelParameters);
        return structuralErrorModel;
    }
}