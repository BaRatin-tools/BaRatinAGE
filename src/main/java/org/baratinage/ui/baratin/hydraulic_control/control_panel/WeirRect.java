package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.SvgIcon;

public class WeirRect extends PhysicalPriorControlPanel {

    private final ParameterPriorDistSimplified kb;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public static final SvgIcon weirCoefIcon = AppSetup.ICONS.getCustomAppImageIcon("weir_coeff.svg");

    public WeirRect() {
        this(true);
    }

    public WeirRect(boolean kMode) {
        super(2, 5, "Q = C_r * B_w * sqrt(2 * g) * (h-b) ^ c"); // \\quad (h > κ)");
        // "Q=C<sub>r</sub>B<sub>w</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        kb = kMode
                ? CommonParameterDistSimplified.getActivationHeight()
                : CommonParameterDistSimplified.getOffsetHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("r");
        weirCoef.setDefaultValues(0.4, 0.05);
        width = CommonParameterDistSimplified.getRectWidth();
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(1.5, 0.05);
        exponent.setLock(true);

        addParameter(kb);
        addParameter(weirCoef);
        addParameter(width);
        addParameter(gravity);
        addParameter(exponent);

        display();

        T.t(this, () -> {
            setHeaders(T.html("mean_value"), T.html("uncertainty_value"));
            kb.setNameLabel(kMode ? T.html("activation_stage") : "Offset"); // FIXME: i18n;
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            width.setNameLabel(T.html("weir_width"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    @Override
    protected Double[] toAMeanAndStd() {
        Double C = weirCoef.getMeanValue();
        Double W = width.getMeanValue();
        Double G = gravity.getMeanValue();

        if (C == null || W == null || G == null) {
            return new Double[] { null, null };
        }

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double A = C * W * sqrtOfTwoG;

        Double Cstd = weirCoef.getStdValue();
        Double Wstd = width.getStdValue();
        Double Gstd = gravity.getStdValue();

        if (Cstd == null || Wstd == null || Gstd == null) {
            return new Double[] { A, null };
        }

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(W * sqrtOfTwoG, 2) +
                        Math.pow(Wstd, 2) * Math.pow(C * sqrtOfTwoG, 2) +
                        // Math.pow(Gstd, 2) * Math.pow(W * C * Math.pow(2 * G, -1 / 2), 2));
                        Math.pow(Gstd, 2) * Math.pow(W * C * Math.pow(2 * G, -1.0 / 2.0), 2));

        return new Double[] { A, Astd };
    }

    @Override
    protected ParameterPriorDistSimplified getKB() {
        return kb;
    }

    @Override
    protected ParameterPriorDistSimplified getExponent() {
        return exponent;
    }
}
