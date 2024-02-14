package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class WeirRect extends PriorControlPanel {

    private final ParameterPriorDistSimplified kb;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public static final SvgIcon weirCoefIcon = AppSetup.ICONS
            .getCustomAppImageIcon("weir_coeff.svg");

    public WeirRect() {
        this(true);
    }

    public WeirRect(boolean kMode) {
        super(
                2,
                "Q=C<sub>r</sub>B<sub>w</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        kb = kMode ? CommonParameterDistSimplified.getActivationHeight()
                : CommonParameterDistSimplified.getOffsetHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("r");
        width = CommonParameterDistSimplified.getRectWidth();
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLocalLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(1.5, 0.05);
        exponent.setLocalLock(true);

        addParameter(kb);
        addParameter(weirCoef);
        addParameter(width);
        addParameter(gravity);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            kb.setNameLabel(kMode ? T.html("activation_stage") : "Offset"); // FIXME: i18n;
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            width.setNameLabel(T.html("weir_width"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
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

        Double kMean = kb.meanValueField.getDoubleValue();
        Double kStd = kb.uncertaintyValueField.getDoubleValue();
        Double cMean = exponent.meanValueField.getDoubleValue();
        Double cStd = exponent.uncertaintyValueField.getDoubleValue();

        return new KACGaussianConfig(
                kMean, kStd == null ? null : kStd / 2,
                AGaussianConfig[0], AGaussianConfig[1],
                cMean, cStd == null ? null : cStd / 2);
    }

}
