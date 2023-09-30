package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.lg.Lg;

public class RectChannelPriorControlPanel extends PriorControlPanel {

    public RectChannelPriorControlPanel() {
        super(
                2,
                "C<sub>r</sub>B<sub>w</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        ParameterPriorDistSimplified activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        ParameterPriorDistSimplified weirCoef = new ParameterPriorDistSimplified();
        weirCoef.setIcon(weirCoefIcon);
        weirCoef.setSymbolUnitLabels("C<sub>r</sub>", "-");
        weirCoef.setDefaultValues(0.4, 0.1);

        ParameterPriorDistSimplified width = new ParameterPriorDistSimplified();
        width.setIcon(widthIcon);
        width.setSymbolUnitLabels("B<sub>w</sub>", "m");

        ParameterPriorDistSimplified gravity = new ParameterPriorDistSimplified();
        gravity.setIcon(gravityIcon);
        gravity.setSymbolUnitLabels("g", "m.s<sup>-2</sup>");
        gravity.setDefaultValues(9.81, 0.01);
        gravity.setLocalLock(true);

        ParameterPriorDistSimplified exponent = new ParameterPriorDistSimplified();
        exponent.setIcon(exponentIcon);
        exponent.setSymbolUnitLabels("c", "-");
        exponent.setDefaultValues(1.5, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(width);
        addParameter(gravity);
        addParameter(exponent);

        Lg.register(this, () -> {
            setHeaders(
                    Lg.html("mean_value"),
                    Lg.html("uncertainty_value"));
            activationHeight.setNameLabel(Lg.html("activation_stage"));
            weirCoef.setNameLabel(Lg.html("weir_coefficient"));
            width.setNameLabel(Lg.html("weir_width"));
            gravity.setNameLabel(Lg.html("gravity_acceleration"));
            exponent.setNameLabel(Lg.html("exponent"));
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
