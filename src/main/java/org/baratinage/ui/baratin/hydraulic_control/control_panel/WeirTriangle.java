package org.baratinage.ui.baratin.hydraulic_control.control_panel;

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
                "C<sub>t</sub>tan(v/2)(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        weirCoef = new ParameterPriorDistSimplified();
        weirCoef.setIcon(weirCoefTIcon);
        weirCoef.setSymbolUnitLabels("C<sub>t</sub>", "-");
        weirCoef.setDefaultValues(0.31, 0.05);

        angle = new ParameterPriorDistSimplified();
        angle.setIcon(angleIcon);
        angle.setSymbolUnitLabels("v", "°");

        gravity = new ParameterPriorDistSimplified();
        gravity.setIcon(gravityIcon);
        gravity.setSymbolUnitLabels("g", "m.s<sup>-2</sup>");
        gravity.setDefaultValues(9.81, 0.01);
        gravity.setLocalLock(true);

        exponent = new ParameterPriorDistSimplified();
        exponent.setIcon(exponentIcon);
        exponent.setSymbolUnitLabels("c", "-");
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
        double V = angle.meanValueField.getDoubleValue();
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);
        double toRadFactorOverTwo = Math.PI / 180d * V / 2d;
        double VOverTwoInRad = toRadFactorOverTwo * toRadFactorOverTwo;
        double tanOfVOverTwo = Math.tan(VOverTwoInRad);

        double A = C * tanOfVOverTwo * sqrtOfTwoG;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !angle.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Vstd = angle.uncertaintyValueField.getDoubleValue() / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(tanOfVOverTwo * sqrtOfTwoG, 2) +
                        Math.pow(Vstd, 2) *
                                Math.pow((C * sqrtOfTwoG * toRadFactorOverTwo) /
                                        Math.pow(Math.cos(VOverTwoInRad), 2), 2)
                        +
                        Math.pow(Gstd, 2) * Math.pow(tanOfVOverTwo * C * Math.pow(2 * G, -1 / 2), 2));

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
