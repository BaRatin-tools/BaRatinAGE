package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.jbam.utils.BamFilesHelpers;

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
        g1parameter.set(DISTRIBUTION.UNIFORM, 1, new double[] { 0, 1000 });
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        Parameter[] modelParameters = new Parameter[] {
                g1parameter.getParameter()
        };
        String name = "constant_model";
        StructuralErrorModel structuralErrorModel = new StructuralErrorModel(
                name,
                String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
                "Constant",
                modelParameters);
        return structuralErrorModel;
    }

    @Override
    public void setFromParameters(Parameter[] parameters) {
        if (parameters.length == 1) {
            Parameter p = parameters[0];
            Distribution d = p.distribution;
            g1parameter.set(d.distribution, p.initalGuess, d.parameterValues);
        }
    }

}