package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;

public class ConstantStructuralErrorModel extends AbstractStructuralErrorModel {

    private final ParameterPriorDist gamma1;

    public ConstantStructuralErrorModel() {

        super();
        gamma1 = new ParameterPriorDist();
        gamma1.setNameLabel("");
        gamma1.setSymbolUnitLabels("&gamma;<sub>1</sub>", "m<sup>3<sup>.s<sup>-1</sup>");

        insertChild(gamma1.symbolUnitLabel, 0, 1);
        insertChild(gamma1.initialGuessField, 1, 1);
        insertChild(gamma1.distributionField.distributionCombobox, 2, 1);
        insertChild(gamma1.distributionField.parameterFieldsPanel, 3, 1);

        // FIXME: add change listeners!

    }

    @Override
    public void applyDefaultConfig() {
        Distribution d = new Distribution(Distribution.DISTRIBUTION.UNIFORM, 0, 1000);
        Parameter p = new Parameter("", 1, d);
        gamma1.configure(true, p);
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        String name = "constant_model";
        return new StructuralErrorModel(
                name,
                String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
                "Constant",
                gamma1.getParameter());
    }

    @Override
    public void setFromParameters(Parameter[] parameters) {
        if (parameters.length == 1) {
            gamma1.configure(true, parameters[0]);
        }
    }

}