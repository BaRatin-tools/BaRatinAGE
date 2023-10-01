package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.lg.Lg;

public class KAC extends PriorControlPanel {

    private final ParameterPriorDist k;
    private final ParameterPriorDist a;
    private final ParameterPriorDist c;

    public KAC() {
        super(
                3, "a(h-b)<sup>c</sup> (h>k)");

        k = new ParameterPriorDist();
        k.setIcon(activationHeightIcon);
        k.setSymbolUnitLabels("k", "m");

        a = new ParameterPriorDist();
        a.setIcon(coefficientIcon);
        a.setSymbolUnitLabels("a", "m<sup>3</sup>.s<sup>-1</sup>");

        c = new ParameterPriorDist();
        c.setIcon(exponentIcon);
        c.setSymbolUnitLabels("c", "-");

        addParameter(k);
        addParameter(a);
        addParameter(c);

        Lg.register(this, () -> {
            setHeaders(
                    Lg.html("initial_guess"),
                    Lg.html("distribution"),
                    Lg.html("distribution_parameters"));
        });
    }

    public void setAToGaussian(Double mean, Double std) {
        a.distributionField.setDistributionType(DISTRIBUTION.GAUSSIAN);
        a.distributionField.setParameters(mean, std);
    }

    @Override
    public Double[] toA() {
        return new Double[] {}; // irrelevant here
    }

}
