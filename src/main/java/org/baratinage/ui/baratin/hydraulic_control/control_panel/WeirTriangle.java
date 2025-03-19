package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class WeirTriangle extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified angle;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirTriangle() {
        super(
                2,
                "Q=C<sub>t</sub>tan(v/2)(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        weirCoef = CommonParameterDistSimplified.getWeirCoeff("t");
        weirCoef.setDefaultValues(0.31, 0.05);
        angle = CommonParameterDistSimplified.getAngle();
        gravity = CommonParameterDistSimplified.getGravity();
        gravity.setLocalLock(true);
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2.5, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(angle);
        addParameter(gravity);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            weirCoef.setNameLabel(T.html("weir_coefficient"));
            angle.setNameLabel(T.html("angle"));
            gravity.setNameLabel(T.html("gravity_acceleration"));
            exponent.setNameLabel(T.html("exponent"));
        });

    }

    private Double[] toAMeanAndStd() {

        if (!weirCoef.meanValueField.isValueValid() ||
                !angle.meanValueField.isValueValid() ||
                !gravity.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        // double toRadFact = Math.PI / 180d;

        double C = weirCoef.meanValueField.getDoubleValue();
        double V = Math.toRadians(angle.meanValueField.getDoubleValue());
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double tanOfVoverTwo = Math.tan(V / 2);

        double A = C * sqrtOfTwoG * tanOfVoverTwo;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !angle.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Vstd = Math.toRadians(angle.uncertaintyValueField.getDoubleValue()) / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Vpart = Math.pow(Vstd, 2) * Math.pow(C * sqrtOfTwoG / 2 / Math.pow(Math.cos(V / 2), 2), 2);
        double Gpart = Math.pow(Gstd, 2) * Math.pow((C * tanOfVoverTwo / sqrtOfTwoG), 2);
        double Cpart = Math.pow(Cstd, 2) * Math.pow(sqrtOfTwoG * tanOfVoverTwo, 2);
        double Astd = Math.sqrt(Cpart + Gpart + Vpart);
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
