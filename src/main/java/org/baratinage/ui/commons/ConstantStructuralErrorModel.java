package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.Distribution.DISTRIB;

public class ConstantStructuralErrorModel extends AbstractStructuralErrorModel {

    private ParameterPriorDist g1parameter;

    public ConstantStructuralErrorModel() {

        super();

        g1parameter = new ParameterPriorDist();
        g1parameter.nameLabel.setText("<html>&gamma;<sub>1</sub></html>");

        insertChild(g1parameter.nameLabel, 0, 1);
        insertChild(g1parameter.initialGuessField, 1, 1);
        insertChild(g1parameter.distComboBox, 2, 1);
        insertChild(g1parameter.parametersInputsPanel, 3, 1);

    }

    @Override
    public void applyDefaultConfig() {
        Parameter g1 = new Parameter("<html>&gamma;<sub>1</sub></html>", 1,
                new Distribution(DISTRIB.UNIFORM, 0, 1000));
        g1parameter.setFromParameter(g1);
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        Parameter[] modelParameters = new Parameter[] {
                g1parameter.getParameter()
        };
        StructuralErrorModel structuralErrorModel = new StructuralErrorModel("constant_model", "Constant",
                modelParameters);
        return structuralErrorModel;
    }

}