package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;

import org.baratinage.translation.T;

public class ChannelTriangle extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified angle;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;

    public ChannelTriangle() {
        super(2, "K<sub>s</sub>tan(v/2)(0.5sin(v/2)<sup>2/3</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        stricklerCoef = new ParameterPriorDistSimplified();
        stricklerCoef.setIcon(stricklerCoefIcon);
        stricklerCoef.setSymbolUnitLabels("K<sub>s</sub>", "m<sup>1/3</sup>.s<sup>-1</sup>");

        angle = new ParameterPriorDistSimplified();
        angle.setIcon(angleIcon);
        angle.setSymbolUnitLabels("v", "°");

        slope = new ParameterPriorDistSimplified();
        slope.setIcon(slopeIcon);
        slope.setSymbolUnitLabels("S", "-");

        exponent = new ParameterPriorDistSimplified();
        exponent.setIcon(exponentIcon);
        exponent.setSymbolUnitLabels("c", "-");
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

    public Double[] toAMeanAndStd() {

        if (!stricklerCoef.meanValueField.isValueValid() ||
                !angle.meanValueField.isValueValid() ||
                !slope.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double K = stricklerCoef.meanValueField.getDoubleValue();
        double V = angle.meanValueField.getDoubleValue();
        double S = slope.meanValueField.getDoubleValue();

        double toRadFact = Math.PI / 180d;
        double VInRadOverTwo = V * toRadFact / 2;
        double trigoStuff = Math.tan(VInRadOverTwo) * Math.pow(0.5 * Math.sin(VInRadOverTwo), 2.0 / 3.0);
        double sqrtOfSlope = Math.sqrt(S);

        double A = K * trigoStuff * sqrtOfSlope;

        if (!stricklerCoef.uncertaintyValueField.isValueValid() ||
                !angle.uncertaintyValueField.isValueValid() ||
                !slope.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double trigoStuff2 = 0.5 * toRadFact *
                Math.pow(Math.sin(VInRadOverTwo), 2.0 / 3.0) *
                ((2.0 / 3.0) + (1.0 / Math.pow(Math.cos(VInRadOverTwo), 2)));

        double Kstd = stricklerCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Vstd = angle.uncertaintyValueField.getDoubleValue() / 2;
        double Sstd = slope.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Kstd, 2) * Math.pow(trigoStuff * sqrtOfSlope, 2) +
                        Math.pow(Vstd, 2) * Math.pow(K * sqrtOfSlope * trigoStuff2, 2) +
                        Math.pow(Sstd, 2) * Math.pow(K * trigoStuff * 0.5 * 1 / sqrtOfSlope * 0.5, 2));

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
