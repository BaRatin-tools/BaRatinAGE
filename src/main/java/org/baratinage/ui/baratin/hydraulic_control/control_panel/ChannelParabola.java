package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class ChannelParabola extends ChannelPriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified height;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;
    private final ParameterPriorDistSimplified manningCoef;

    public ChannelParabola() {
        super(2, 6,
                "Q = K_s * (2/3) ^ (5/3) * sqrt(S) * B_p / H_p * (h-b) ^ c",
                "Q = 1 / n * (2/3) ^ (5/3) * sqrt(S) * B_p / H_p * (h-b) ^ c");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        width = CommonParameterDistSimplified.getParabolaWidth();
        height = CommonParameterDistSimplified.getHeight("p");
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getActivationHeight();
        exponent.setLock(true);
        exponent.setDefaultValues(2.17, 0.05);
        manningCoef = CommonParameterDistSimplified.getManningCoeff();

        addParameter(activationHeight);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(height);
        addParameter(slope);
        addParameter(exponent);
        addParameter(manningCoef);

        display();

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            activationHeight.setNameLabel(T.html("activation_stage"));
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            manningCoef.setNameLabel(T.html("manning_coef"));
            width.setNameLabel(T.html("parabola_width"));
            height.setNameLabel(T.html("parabola_height"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        Double N = manningCoef.meanValueField.isValueValid() ? manningCoef.meanValueField.getDoubleValue() : null;
        Double K = stricklerCoef.meanValueField.isValueValid() ? stricklerCoef.meanValueField.getDoubleValue() : null;
        Double S = slope.meanValueField.isValueValid() ? slope.meanValueField.getDoubleValue() : null;
        Double B = width.meanValueField.isValueValid() ? width.meanValueField.getDoubleValue() : null;
        Double H = height.meanValueField.isValueValid() ? height.meanValueField.getDoubleValue() : null;
        Double Nstd = manningCoef.uncertaintyValueField.isValueValid()
                ? manningCoef.uncertaintyValueField.getDoubleValue() / 2.0
                : null;
        Double Kstd = stricklerCoef.uncertaintyValueField.isValueValid()
                ? stricklerCoef.uncertaintyValueField.getDoubleValue() / 2.0
                : null;
        Double Sstd = slope.uncertaintyValueField.isValueValid() ? slope.uncertaintyValueField.getDoubleValue() / 2.0
                : null;
        Double Bstd = width.uncertaintyValueField.isValueValid() ? width.uncertaintyValueField.getDoubleValue() / 2.0
                : null;

        Double Hstd = height.uncertaintyValueField.isValueValid() ? height.uncertaintyValueField.getDoubleValue() / 2.0
                : null;

        if (useManning()) {
            return getAandAstdManning(N, S, B, H, Nstd, Sstd, Bstd, Hstd);
        } else {
            return getAandAstdStrickler(K, S, B, H, Kstd, Sstd, Bstd, Hstd);
        }

    }

    private static Double[] getAandAstdStrickler(
            Double K,
            Double S,
            Double B,
            Double H,
            Double Kstd,
            Double Sstd,
            Double Bstd,
            Double Hstd) {

        if (H == null || B == null || S == null || K == null) {
            return new Double[] { null, null };
        }

        // mean value
        Double A = K * Math.pow(2.0 / 3.0, 5.0 / 3.0) * Math.sqrt(S) * B / H;

        if (Hstd == null || Bstd == null || Sstd == null || Kstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives
        Double dAdK = A / K;
        Double dAdS = A / (2.0 * S);
        Double dAdB = A / B;
        Double dAdH = -A / H;

        // std
        Double Astd = Math.sqrt(Math.pow(dAdK * Kstd, 2)
                + Math.pow(dAdS * Sstd, 2)
                + Math.pow(dAdB * Bstd, 2)
                + Math.pow(dAdH * Hstd, 2)

        );

        return new Double[] { A, Astd };
    }

    private static Double[] getAandAstdManning(
            Double N,
            Double S,
            Double B,
            Double H,
            Double Nstd,
            Double Sstd,
            Double Bstd,
            Double Hstd) {

        if (H == null || B == null || S == null || N == null) {
            return new Double[] { null, null };
        }

        // mean value
        Double A = 1 / N * Math.pow(2.0 / 3.0, 5.0 / 3.0) * Math.sqrt(S) * B / H;

        if (Hstd == null || Bstd == null || Sstd == null || Nstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives
        Double dAdN = -A / N;
        Double dAdS = A / (2.0 * S);
        Double dAdB = A / B;
        Double dAdH = -A / H;

        // std
        Double Astd = Math.sqrt(Math.pow(dAdN * Nstd, 2)
                + Math.pow(dAdS * Sstd, 2)
                + Math.pow(dAdB * Bstd, 2)
                + Math.pow(dAdH * Hstd, 2)

        );

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
