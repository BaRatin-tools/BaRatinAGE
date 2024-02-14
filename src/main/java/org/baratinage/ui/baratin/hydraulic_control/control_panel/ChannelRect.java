package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

import org.baratinage.translation.T;

public class ChannelRect extends PriorControlPanel {

    private final ParameterPriorDistSimplified kb;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;

    public ChannelRect() {
        this(true);
    }

    public ChannelRect(boolean kMode) {
        super(2, "Q=K<sub>s</sub>B<sub>w</sub>S<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        kb = kMode ? CommonParameterDistSimplified.getActivationHeight()
                : CommonParameterDistSimplified.getOffsetHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        width = CommonParameterDistSimplified.getRectWidth();
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(1.67, 0.05);
        exponent.setLocalLock(true);

        addParameter(kb);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(slope);
        addParameter(exponent);

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            kb.setNameLabel(kMode ? T.html("activation_stage") : "Offset");
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            width.setNameLabel(T.html("channel_width"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        if (!stricklerCoef.meanValueField.isValueValid() ||
                !width.meanValueField.isValueValid() ||
                !slope.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double K = stricklerCoef.meanValueField.getDoubleValue();
        double W = width.meanValueField.getDoubleValue();
        double S = slope.meanValueField.getDoubleValue();

        double sqrtOfSlope = Math.sqrt(S);

        double A = K * W * sqrtOfSlope;

        if (!stricklerCoef.uncertaintyValueField.isValueValid() ||
                !width.uncertaintyValueField.isValueValid() ||
                !slope.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Kstd = stricklerCoef.uncertaintyValueField.getDoubleValue() / 2;
        double Wstd = width.uncertaintyValueField.getDoubleValue() / 2;
        double Sstd = slope.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Kstd, 2) * Math.pow(W * sqrtOfSlope, 2) +
                        Math.pow(Wstd, 2) * Math.pow(K * sqrtOfSlope, 2) +
                        Math.pow(Sstd, 2) * Math.pow(K * W * 1 / sqrtOfSlope * 0.5, 2));

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
