package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.lg.Lg;

public class ChannelRect extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;

    public ChannelRect() {
        super(2, "K<sub>s</sub>B<sub>w</sub>S<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        stricklerCoef = new ParameterPriorDistSimplified();
        stricklerCoef.setIcon(stricklerCoefIcon);
        stricklerCoef.setSymbolUnitLabels("K<sub>s</sub>", "m<sup>1/3</sup>.s<sup>-1</sup>");

        width = new ParameterPriorDistSimplified();
        width.setIcon(widthIcon);
        width.setSymbolUnitLabels("B<sub>w</sub>", "m");

        slope = new ParameterPriorDistSimplified();
        slope.setIcon(slopeIcon);
        slope.setSymbolUnitLabels("S", "-");
        slope.setDefaultValues(9.81, 0.01);
        slope.setLocalLock(true);

        exponent = new ParameterPriorDistSimplified();
        exponent.setIcon(exponentIcon);
        exponent.setSymbolUnitLabels("c", "-");
        exponent.setDefaultValues(1.67, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(slope);
        addParameter(exponent);

        Lg.register(this, () -> {
            setHeaders(
                    Lg.html("mean_value"),
                    Lg.html("uncertainty_value"));
            activationHeight.setNameLabel(Lg.html("activation_stage"));
            stricklerCoef.setNameLabel(Lg.html("strickler_coef"));
            width.setNameLabel(Lg.html("channel_width"));
            slope.setNameLabel(Lg.html("channel_slope"));
            exponent.setNameLabel(Lg.html("exponent"));
        });
    }

    @Override
    public Double[] toA() {

        if (!stricklerCoef.meanValueField.isValueValid() ||
                !width.meanValueField.isValueValid() ||
                !slope.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double K = stricklerCoef.meanValueField.getDoubleValue();
        double W = width.meanValueField.getDoubleValue();
        double S = slope.meanValueField.getDoubleValue();

        double sqrtOfSlope = Math.sqrt(S);

        double A = K * W * sqrtOfSlope;

        if (!stricklerCoef.uncertaintyValueField.isValueValid() ||
                !width.uncertaintyValueField.isValueValid() ||
                !slope.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Kstd = stricklerCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Wstd = width.uncertaintyValueField.getDoubleValue() / 2;
        double Sstd = slope.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Kstd, 2) * Math.pow(W * sqrtOfSlope, 2) +
                        Math.pow(Wstd, 2) * Math.pow(K * sqrtOfSlope, 2) +
                        Math.pow(Sstd, 2) * Math.pow(K * W * 1 / sqrtOfSlope * 0.5, 2));

        return new Double[] { A, Astd };

    }

}
