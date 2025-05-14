package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class WeirOrifice extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified area;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirOrifice() {
        super(
                2,
                "Q=C<sub>o</sub>A<sub>o</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("o");
        weirCoef.setDefaultValues(0.6, 0.05);
        area = CommonParameterDistSimplified.getCircleArea();
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(0.5, 0.05);
        exponent.setLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(area);
        addParameter(gravity);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            area.setNameLabel(T.html("orifice_area"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });

    }

    private Double[] toAMeanAndStd() {

        if (!weirCoef.meanValueField.isValueValid() ||
                !area.meanValueField.isValueValid() ||
                !gravity.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double C = weirCoef.meanValueField.getDoubleValue();
        double AR = area.meanValueField.getDoubleValue();
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);

        double A = C * AR * sqrtOfTwoG;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !area.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double ARstd = area.uncertaintyValueField.getDoubleValue() / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(AR * sqrtOfTwoG, 2) +
                        Math.pow(ARstd, 2) * Math.pow(C * sqrtOfTwoG, 2) +
                        Math.pow(Gstd, 2) * Math.pow(AR * C * Math.pow(2 * G, -1 / 2), 2));

        return new Double[] { A, Astd };

    }

    @Override
    public KBACGaussianConfig toKACGaussianConfig() {

        Double[] AGaussianConfig = toAMeanAndStd();

        Double kMean = activationHeight.meanValueField.getDoubleValue();
        Double kStd = activationHeight.uncertaintyValueField.getDoubleValue();
        Double cMean = exponent.meanValueField.getDoubleValue();
        Double cStd = exponent.uncertaintyValueField.getDoubleValue();

        return new KBACGaussianConfig(
                kMean, kStd == null ? null : kStd / 2,
                AGaussianConfig[0], AGaussianConfig[1],
                cMean, cStd == null ? null : cStd / 2);
    }

}
