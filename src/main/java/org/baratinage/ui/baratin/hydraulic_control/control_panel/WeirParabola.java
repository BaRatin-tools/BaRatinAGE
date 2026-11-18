package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

public class WeirParabola extends PhysicalPriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified height;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirParabola() {
        super(2, 6, "Q = C_p * B_p / sqrt(H_p) * sqrt(2 * g) * (h-b) ^ c"); // \\quad (h > κ)");
        // "Q=C<sub>p</sub>B<sub>p</sub>H<sub>p</sub><sup>-1/2</sup>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("p");
        weirCoef.setDefaultValues(0.22, 0.05);
        width = CommonParameterDistSimplified.getParabolaWidth();
        height = CommonParameterDistSimplified.getHeight("p");
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2, 0.05);
        exponent.setLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(width);
        addParameter(height);
        addParameter(gravity);
        addParameter(exponent);

        display();

        T.t(this, () -> {
            setHeaders(T.html("mean_value"), T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            width.setNameLabel(T.html("parabola_width"));
            height.setNameLabel(T.html("parabola_height"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    @Override
    protected Double[] toAMeanAndStd() {
        Double C = weirCoef.getMeanValue();
        Double W = width.getMeanValue();
        Double H = height.getMeanValue();
        Double G = gravity.getMeanValue();

        if (C == null || W == null || H == null || G == null) {
            return new Double[] { null, null };
        }

        double sqrtOfTwoG = Math.sqrt(2.0 * G);
        double sqrtOfHeight = 1 / Math.sqrt(H);
        double A = ((C * W) / sqrtOfHeight) * sqrtOfTwoG;

        Double Cstd = weirCoef.getStdValue();
        Double Wstd = width.getStdValue();
        Double Hstd = height.getStdValue();
        Double Gstd = gravity.getStdValue();

        if (Cstd == null || Wstd == null || Hstd == null || Gstd == null) {
            return new Double[] { A, null };
        }

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow((W / sqrtOfHeight) * sqrtOfTwoG, 2) +
                        Math.pow(Wstd, 2) * Math.pow((C / sqrtOfHeight) * sqrtOfTwoG, 2) +
                        Math.pow(Hstd, 2) *
                                Math.pow(C * W * sqrtOfTwoG * Math.pow(H, -1.5), 2)
                        +
                        Math.pow(Gstd, 2) * Math.pow(C * W * Math.pow(2 * G * H, -1.0 / 2.0), 2));

        return new Double[] { A, Astd };
    }

    @Override
    protected ParameterPriorDistSimplified getKB() {
        return activationHeight;
    }

    @Override
    protected ParameterPriorDistSimplified getExponent() {
        return exponent;
    }
}
