package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.jbam.DistributionType;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.translation.T;

public class KAC extends PriorControlPanel {

    public final ParameterPriorDist k;
    public final ParameterPriorDist a;
    public final ParameterPriorDist c;

    public KAC() {
        super(
                3, "a(h-b)<sup>c</sup> (h>k)");

        k = new ParameterPriorDist("k");
        k.setIcon(activationHeightIcon);
        k.setSymbolUnitLabels("k", "m");

        a = new ParameterPriorDist("a");
        a.setIcon(coefficientIcon);
        a.setSymbolUnitLabels("a", "m<sup>3</sup>.s<sup>-1</sup>");

        c = new ParameterPriorDist("c");
        c.setIcon(exponentIcon);
        c.setSymbolUnitLabels("c", "-");

        addParameter(k);
        addParameter(a);
        addParameter(c);

        T.t(this, (kac) -> {
            kac.setHeaders(
                    T.html("initial_guess"),
                    T.html("distribution"),
                    T.html("distribution_parameters"));
        });
    }

    public void setAToGaussian(Double mean, Double std) {
        a.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        a.distributionField.setParameters(mean, std);
    }

    public void setFromKACGaussianConfig(KACGaussianConfig kacCongig) {
        k.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        k.distributionField.setParameters(kacCongig.kMean(), kacCongig.kStd());
        k.initialGuessField.setValue(kacCongig.kMean());
        a.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        a.distributionField.setParameters(kacCongig.aMean(), kacCongig.aStd());
        a.initialGuessField.setValue(kacCongig.aMean());
        c.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        c.distributionField.setParameters(kacCongig.cMean(), kacCongig.cStd());
        c.initialGuessField.setValue(kacCongig.cMean());

        fireChangeListeners();
    }

    @Override
    public KACGaussianConfig toKACGaussianConfig() {
        // irrelevant
        return null;
    }

}
