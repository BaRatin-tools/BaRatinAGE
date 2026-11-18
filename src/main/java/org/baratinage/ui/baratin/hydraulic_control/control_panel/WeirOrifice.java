package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

public class WeirOrifice extends PhysicalPriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified area;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirOrifice() {
        super(2, 5, "Q=C_o * A_o * sqrt(2 * g) * (h-b) ^ c"); // \\quad (h > κ)");
        // "Q=C<sub>o</sub>A<sub>o</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

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

        display();

        T.t(this, () -> {
            setHeaders(T.html("mean_value"), T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            area.setNameLabel(T.html("orifice_area"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    @Override
    protected Double[] toAMeanAndStd() {
        Double C = weirCoef.getMeanValue();
        Double AR = area.getMeanValue();
        Double G = gravity.getMeanValue();

        if (C == null || AR == null || G == null) {
            return new Double[] { null, null };
        }

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double A = C * AR * sqrtOfTwoG;

        Double Cstd = weirCoef.getStdValue();
        Double ARstd = area.getStdValue();
        Double Gstd = gravity.getStdValue();

        if (Cstd == null || ARstd == null || Gstd == null) {
            return new Double[] { A, null };
        }

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(AR * sqrtOfTwoG, 2) +
                        Math.pow(ARstd, 2) * Math.pow(C * sqrtOfTwoG, 2) +
                        Math.pow(Gstd, 2) * Math.pow(AR * C * Math.pow(2 * G, -1.0 / 2.0), 2));

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
