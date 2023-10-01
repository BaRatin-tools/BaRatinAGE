package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.lg.Lg;

public class WeirRect extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirRect() {
        super(
                2,
                "C<sub>r</sub>B<sub>w</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        weirCoef = new ParameterPriorDistSimplified();
        weirCoef.setIcon(weirCoefIcon);
        weirCoef.setSymbolUnitLabels("C<sub>r</sub>", "-");
        weirCoef.setDefaultValues(0.4, 0.1);

        width = new ParameterPriorDistSimplified();
        width.setIcon(widthIcon);
        width.setSymbolUnitLabels("B<sub>w</sub>", "m");

        gravity = new ParameterPriorDistSimplified();
        gravity.setIcon(gravityIcon);
        gravity.setSymbolUnitLabels("g", "m.s<sup>-2</sup>");
        gravity.setDefaultValues(9.81, 0.01);
        gravity.setLocalLock(true);

        exponent = new ParameterPriorDistSimplified();
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

    private Double[] toAMeanAndStd() {

        if (!weirCoef.meanValueField.isValueValid() ||
                !width.meanValueField.isValueValid() ||
                !gravity.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double C = weirCoef.meanValueField.getDoubleValue();
        double W = width.meanValueField.getDoubleValue();
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);

        double A = C * W * sqrtOfTwoG;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !width.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Wstd = width.uncertaintyValueField.getDoubleValue() / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(W * sqrtOfTwoG, 2) +
                        Math.pow(Wstd, 2) * Math.pow(C * sqrtOfTwoG, 2) +
                        Math.pow(Gstd, 2) * Math.pow(W * C * Math.pow(2 * G, -1 / 2), 2));

        return new Double[] { A, Astd };

    }

    @Override
    public KACGaussianConfig toKACGaussianConfig() {

        Double[] AGaussianConfig = toAMeanAndStd();

        Double kMean = activationHeight.meanValueField.getDoubleValue();
        Double kStd = activationHeight.uncertaintyValueField.getDoubleValue();
        Double cMean = exponent.meanValueField.getDoubleValue();
        Double cStd = exponent.uncertaintyValueField.getDoubleValue();

        return new KACGaussianConfig(
                kMean, kStd == null ? null : kStd / 2,
                AGaussianConfig[0], AGaussianConfig[1],
                cMean, cStd == null ? null : cStd / 2);
    }

}
