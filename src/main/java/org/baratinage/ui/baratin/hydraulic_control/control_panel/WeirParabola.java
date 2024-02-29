package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class WeirParabola extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified height;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirParabola() {
        super(
                2,
                "Q=C<sub>p</sub>B<sub>p</sub>H<sub>p</sub><sup>-1/2</sup>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("p");
        weirCoef.setDefaultValues(0.22, 0.05);
        width = CommonParameterDistSimplified.getParabolaWidth();
        height = CommonParameterDistSimplified.getHeight("p");
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLocalLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(width);
        addParameter(height);
        addParameter(gravity);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            width.setNameLabel(T.html("parabola_width"));
            height.setNameLabel(T.html("parabola_height"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });

    }

    private Double[] toAMeanAndStd() {

        if (!weirCoef.meanValueField.isValueValid() ||
                !width.meanValueField.isValueValid() ||
                !height.meanValueField.isValueValid() ||
                !gravity.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        // double toRadFact = Math.PI / 180d;

        double C = weirCoef.meanValueField.getDoubleValue();
        double W = width.meanValueField.getDoubleValue();
        double H = height.meanValueField.getDoubleValue();
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double sqrtOfHeight = 1 / Math.sqrt(H);
        // double toRadFactorOverTwo = Math.PI / 180d * V / 2d;
        // double VOverTwoInRad = toRadFactorOverTwo * toRadFactorOverTwo;
        // double tanOfVOverTwo = Math.tan(VOverTwoInRad);

        double A = C * W / sqrtOfHeight * sqrtOfTwoG;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !width.uncertaintyValueField.isValueValid() ||
                !height.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Wstd = width.uncertaintyValueField.getDoubleValue() / 2;
        double Hstd = height.uncertaintyValueField.getDoubleValue() / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(W / sqrtOfHeight * sqrtOfTwoG, 2) +
                        Math.pow(Wstd, 2) * Math.pow(C / sqrtOfHeight * sqrtOfTwoG, 2) +
                        Math.pow(Hstd, 2) * Math.pow(C * W * sqrtOfTwoG * Math.pow(H, -1.5), 2) +
                        Math.pow(Gstd, 2) * Math.pow(C * W * Math.pow(2 * G * H, -1 / 2), 2));

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
