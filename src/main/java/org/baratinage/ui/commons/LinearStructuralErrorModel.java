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

        g1parameter = new ParameterPriorDist("gamma1");
        g1parameter.nameLabel.setText(gamma1LabelString);
        g1parameter.addChangeListener((e) -> {
            fireChangeListener();
        });

        insertChild(g1parameter.nameLabel, 0, 1);
        insertChild(g1parameter.initialGuessField, 1, 1);
        insertChild(g1parameter.distComboBox, 2, 1);
        insertChild(g1parameter.parametersInputsPanel, 3, 1);

        g2parameter = new ParameterPriorDist("gamma2");
        g2parameter.nameLabel.setText(gamma2LabelString);
        g2parameter.addChangeListener((e) -> {
            fireChangeListener();
        });

        insertChild(g2parameter.nameLabel, 0, 2);
        insertChild(g2parameter.initialGuessField, 1, 2);
        insertChild(g2parameter.distComboBox, 2, 2);
        insertChild(g2parameter.parametersInputsPanel, 3, 2);

    }

    @Override
    public void applyDefaultConfig() {
        g1parameter.set(DISTRIB.UNIFORM, 1, new double[] { 0, 10000 });
        g2parameter.set(DISTRIB.UNIFORM, 0.1, new double[] { 0, 10000 });
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

    @Override
    public void setFromParameters(Parameter[] parameters) {
        if (parameters.length == 2) {
            Parameter p1 = parameters[0];
            Distribution d1 = p1.getDistribution();
            g1parameter.set(d1.getDistrib(), p1.getInitalGuess(), d1.getParameterValues());
            Parameter p2 = parameters[1];
            Distribution d2 = p2.getDistribution();
            g2parameter.set(d2.getDistrib(), p2.getInitalGuess(), d2.getParameterValues());
        }
    }

}