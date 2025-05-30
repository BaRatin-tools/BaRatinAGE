package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

import org.baratinage.translation.T;

public class ChannelParabola extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified height;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;

    public ChannelParabola() {
        super(2, "Q=K<sub>s</sub>B<sub>p</sub>H<sub>p</sub><sup>-1/2</sup>(2/3)<sup>5/3</sup>S<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>\u03BA)");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        width = CommonParameterDistSimplified.getParabolaWidth();
        height = CommonParameterDistSimplified.getHeight("p");
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getActivationHeight();
        exponent.setLock(true);
        exponent.setDefaultValues(2.17, 0.05);

        addParameter(activationHeight);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(height);
        addParameter(slope);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            width.setNameLabel(T.html("parabola_width"));
            height.setNameLabel(T.html("parabola_height"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        if (!stricklerCoef.meanValueField.isValueValid() ||
                !width.meanValueField.isValueValid() ||
                !height.meanValueField.isValueValid() ||
                !slope.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double K = stricklerCoef.meanValueField.getDoubleValue();
        double W = width.meanValueField.getDoubleValue();
        double H = height.meanValueField.getDoubleValue();
        double S = slope.meanValueField.getDoubleValue();

        double sqrtOfSlope = Math.sqrt(S);
        double OneOverSqrtOfHeight = 1 / Math.sqrt(H);
        double widthOverSqrtOfHeight = W * OneOverSqrtOfHeight;

        double A = K * widthOverSqrtOfHeight * sqrtOfSlope;

        if (!stricklerCoef.uncertaintyValueField.isValueValid() ||
                !width.uncertaintyValueField.isValueValid() ||
                !height.uncertaintyValueField.isValueValid() ||
                !slope.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Kstd = stricklerCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Wstd = width.uncertaintyValueField.getDoubleValue() / 2;
        double Hstd = height.uncertaintyValueField.getDoubleValue() / 2;
        double Sstd = slope.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Kstd, 2) * Math.pow(widthOverSqrtOfHeight * sqrtOfSlope, 2) +
                        Math.pow(Wstd, 2) * Math.pow(K * sqrtOfSlope * OneOverSqrtOfHeight, 2) +
                        Math.pow(Hstd, 2) * Math.pow(0.5 * K * sqrtOfSlope * W * Math.pow(H, -1.5), 2) +
                        Math.pow(Sstd, 2) * Math.pow(K * widthOverSqrtOfHeight * 1 / sqrtOfSlope * 0.5, 2));

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
