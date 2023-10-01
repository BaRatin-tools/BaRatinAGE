package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.IStructuralErrorModels;

public class DefaultStructuralErrorModels implements IStructuralErrorModels {

    private final StructuralErrorModel[] strucErrorModels;

    public DefaultStructuralErrorModels(int nOutputs) {
        strucErrorModels = new StructuralErrorModel[nOutputs];
        Distribution defaultDist = new Distribution(DistributionType.UNIFORM, 0, 10000);
        Parameter gamma1 = new Parameter("gamma1", 1, defaultDist);
        Parameter gamma2 = new Parameter("gamma2", 0.1, defaultDist);
        for (int k = 0; k < nOutputs; k++) {
            String name = "Y_" + (k + 1);
            String fileName = String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name);
            strucErrorModels[k] = new StructuralErrorModel(name, fileName, "Linear", gamma1, gamma2);
        }
    }

    @Override
    public StructuralErrorModel[] getStructuralErrorModels() {
        return strucErrorModels;
    }

}
