package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.Distribution.DISTRIB;

public class ConstantStructuralErrorModel extends AbstractStructuralErrorModel {

    private ParameterPriorDist g1parameter;

    public ConstantStructuralErrorModel() {

        super();

        g1parameter = new ParameterPriorDist("gamma1");
        g1parameter.nameLabel.setText("<html>&gamma;<sub>1</sub></html>");
        g1parameter.addChangeListener((e) -> {
            fireChangeListener();
        });

        insertChild(g1parameter.nameLabel, 0, 1);
        insertChild(g1parameter.initialGuessField, 1, 1);
        insertChild(g1parameter.distComboBox, 2, 1);
        insertChild(g1parameter.parametersInputsPanel, 3, 1);

    }

    @Override
    public void applyDefaultConfig() {
        g1parameter.set(DISTRIB.UNIFORM, 1, new double[] { 0, 1000 });
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

    @Override
    public void setFromParameters(Parameter[] parameters) {
        if (parameters.length == 1) {
            Parameter p = parameters[0];
            Distribution d = p.getDistribution();
            g1parameter.set(d.getDistrib(), p.getInitialGuess(), d.getParameterValues());
        }
    }

}