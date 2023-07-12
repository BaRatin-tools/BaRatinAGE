package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.IStructuralError;

public class DefaultStructuralErrorProvider implements IStructuralError {

    public enum TYPE {
        LINEAR, CONSTANT
    };

    StructuralErrorModel structuralErrorModel;

    public DefaultStructuralErrorProvider(TYPE type) {
        if (type == TYPE.LINEAR) {
            String name = "linear_default";
            structuralErrorModel = new StructuralErrorModel(
                    name,
                    String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
                    "Linear",
                    new Parameter[] {
                            new Parameter("gamma1", 1, new Distribution(DISTRIB.UNIFORM, 0, 1000)),
                            new Parameter("gamma2", 0.1, new Distribution(DISTRIB.UNIFORM, 0, 1000)),
                    });
        } else {
            String name = "constant_default";
            structuralErrorModel = new StructuralErrorModel(
                    name,
                    String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name),
                    "Constant",
                    new Parameter[] {
                            new Parameter("gamma1", 1, new Distribution(DISTRIB.UNIFORM, 0, 1000))
                    });
        }
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        return structuralErrorModel;
    }

}
