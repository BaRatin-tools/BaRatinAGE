package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

import org.baratinage.translation.T;

public class ChannelTriangle extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified angle;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;

    public ChannelTriangle() {
        super(2, "Q=K<sub>s</sub>tan(v/2)(0.5sin(v/2)<sup>2/3</sup>S<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        angle = CommonParameterDistSimplified.getAngle();
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2.67, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(stricklerCoef);
        addParameter(angle);
        addParameter(slope);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            angle.setNameLabel(T.html("angle"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        if (!stricklerCoef.meanValueField.isValueValid() ||
                !angle.meanValueField.isValueValid() ||
                !slope.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double K = stricklerCoef.meanValueField.getDoubleValue();
        double V = Math.toRadians(angle.meanValueField.getDoubleValue());
        double S = slope.meanValueField.getDoubleValue();

        double sqrtOfSlope = Math.sqrt(S);
        double tanOfVoverTwo = Math.tan(V / 2);
        double sinOfVoverTwo = Math.sin(V / 2);

        double A = K * sqrtOfSlope * tanOfVoverTwo * Math.pow(sinOfVoverTwo / 2, 2 / 3);

        if (!stricklerCoef.uncertaintyValueField.isValueValid() ||
                !angle.uncertaintyValueField.isValueValid() ||
                !slope.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Kstd = stricklerCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Vstd = Math.toRadians(angle.uncertaintyValueField.getDoubleValue()) / 2;
        double Sstd = slope.uncertaintyValueField.getDoubleValue() / 2;

        double Vpart = Math.pow(Vstd, 2) * Math.pow(
                K * sqrtOfSlope * Math.pow(0.5, 5 / 3) * Math.pow(sinOfVoverTwo, 2 / 3)
                        * (1 / Math.pow(Math.cos(V / 2), 2) + 2 / 3),
                2);

        double Spart = Math.pow(Sstd, 2) * Math.pow(
                K * tanOfVoverTwo * Math.pow(sinOfVoverTwo / 2, 2 / 3) / 2 * sqrtOfSlope,
                2);

        double Kpart = Math.pow(Kstd, 2)
                * Math.pow(sqrtOfSlope * tanOfVoverTwo * Math.pow(sinOfVoverTwo / 2, 2 / 3), 2);

        double Astd = Math.sqrt(Kpart + Spart + Vpart);
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
