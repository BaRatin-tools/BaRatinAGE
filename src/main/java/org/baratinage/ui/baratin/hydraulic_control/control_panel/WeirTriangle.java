package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

public class WeirTriangle extends PhysicalPriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified angle;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirTriangle() {
        super(2, 5, "Q = C_t * tan(v/2) * sqrt(2 * g) * (h-b) ^ c"); // \\quad (h > κ)");
        // "Q=C<sub>t</sub>tan(v/2)(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("t");
        weirCoef.setDefaultValues(0.31, 0.05);
        angle = CommonParameterDistSimplified.getAngle();
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2.5, 0.05);
        exponent.setLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(angle);
        addParameter(gravity);
        addParameter(exponent);

        display();

        T.t(this, () -> {
            setHeaders(T.html("mean_value"), T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            angle.setNameLabel(T.html("angle"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    @Override
    protected Double[] toAMeanAndStd() {
        Double C = weirCoef.getMeanValue();
        Double V = angle.getMeanValue();
        Double G = gravity.getMeanValue();

        if (C == null || V == null || G == null) {
            return new Double[] { null, null };
        }

        V = Math.toRadians(V);

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double tanOfVoverTwo = Math.tan(V / 2.0);
        double A = C * sqrtOfTwoG * tanOfVoverTwo;

        Double Cstd = weirCoef.getStdValue();
        Double Vstd = angle.getStdValue();
        Double Gstd = gravity.getStdValue();

        if (Cstd == null || Vstd == null || Gstd == null) {
            return new Double[] { A, null };
        }

        Vstd = Math.toRadians(Vstd);

        double Vpart = Math.pow(Vstd, 2) *
                Math.pow((C * sqrtOfTwoG) / 2.0 / Math.pow(Math.cos(V / 2.0), 2), 2);
        double Gpart = Math.pow(Gstd, 2) * Math.pow((C * tanOfVoverTwo) / sqrtOfTwoG, 2);
        double Cpart = Math.pow(Cstd, 2) * Math.pow(sqrtOfTwoG * tanOfVoverTwo, 2);
        double Astd = Math.sqrt(Cpart + Gpart + Vpart);
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
