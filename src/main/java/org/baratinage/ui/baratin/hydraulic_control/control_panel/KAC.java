package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.lg.Lg;

public class KAC extends PriorControlPanel {

    public KAC() {
        super(
                3, "a(h-b)<sup>c</sup> (h>k)");

        ParameterPriorDist k = new ParameterPriorDist();
        k.setIcon(activationHeightIcon);
        k.setSymbolUnitLabels("k", "m");

        ParameterPriorDist a = new ParameterPriorDist();
        a.setIcon(coefficientIcon);
        a.setSymbolUnitLabels("a", "m<sup>3</sup>.s<sup>-1</sup>");

        ParameterPriorDist c = new ParameterPriorDist();
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

    // @Override
    // public JSONObject toJSON() {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    // }

    // @Override
    // public void fromJSON(JSONObject json) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    // }

}
