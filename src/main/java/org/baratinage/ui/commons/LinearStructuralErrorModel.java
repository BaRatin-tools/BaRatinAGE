package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;

public class LinearStructuralErrorModel extends AbstractStructuralErrorModel {

    private final ParameterPriorDist gamma1;
    private final ParameterPriorDist gamma2;

    public LinearStructuralErrorModel() {
        super();

        setColWeight(3, 1);
        setGap(5);
        setPadding(5);

        gamma1 = new ParameterPriorDist();
        gamma1.setNameLabel("");
        gamma1.setSymbolUnitLabels("&gamma;<sub>1</sub>", "m<sup>3<sup>.s<sup>-1</sup>");

        insertChild(gamma1.symbolUnitLabel, 0, 1);
        insertChild(gamma1.initialGuessField, 1, 1);
        insertChild(gamma1.distributionField.distributionCombobox, 2, 1);
        insertChild(gamma1.distributionField.parameterFieldsPanel, 3, 1);

        gamma2 = new ParameterPriorDist();
        gamma2.setNameLabel("");
        gamma2.setSymbolUnitLabels("&gamma;<sub>1</sub>", "m<sup>3<sup>.s<sup>-1</sup>");

        insertChild(gamma2.symbolUnitLabel, 0, 2);
        insertChild(gamma2.initialGuessField, 1, 2);
        insertChild(gamma2.distributionField.distributionCombobox, 2, 2);
        insertChild(gamma2.distributionField.parameterFieldsPanel, 3, 2);

    }

    @Override
    public void applyDefaultConfig() {

        Distribution d1 = new Distribution(Distribution.DISTRIBUTION.UNIFORM, 0, 1000);
        Parameter p1 = new Parameter("gamma1", 1, d1);
        gamma1.configure(true, p1);

        Distribution d2 = new Distribution(Distribution.DISTRIBUTION.UNIFORM, 0, 1000);
        Parameter p2 = new Parameter("gamma2", 0.1, d2);
        gamma1.configure(true, p2);

    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        String name = "linear_model";
        return new StructuralErrorModel(
                name,
                String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
                "Linear",
                gamma1.getParameter(),
                gamma2.getParameter());
    }

    @Override
    public void setFromParameters(Parameter[] parameters) {
        if (parameters.length == 2) {
            gamma1.configure(true, parameters[0]);
            gamma1.configure(true, parameters[2]);

        }
    }

}