package org.baratinage.ui.bam;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;

public class DefaultStructuralErrorProvider implements IStructuralError {

    public enum TYPE {
        LINEAR, CONSTANT
    };

    StructuralErrorModel structuralErrorModel;

    public DefaultStructuralErrorProvider(TYPE type) {
        if (type == TYPE.LINEAR) {
            structuralErrorModel = new StructuralErrorModel("linear_default", "Linear",
                    new Parameter[] {
                            new Parameter("gamma1", 1, Distribution.Uniform(0, 1000)),
                            new Parameter("gamma2", 0.1, Distribution.Uniform(0, 1000)),
                    });
        } else {
            structuralErrorModel = new StructuralErrorModel("constant_default", "Constant",
                    new Parameter[] {
                            new Parameter("gamma1", 1, Distribution.Uniform(0, 1000))
                    });
        }
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        return structuralErrorModel;
    }

}
