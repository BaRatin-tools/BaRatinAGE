package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class ChannelRect extends ChannelPriorControlPanel {

    private final ParameterPriorDistSimplified kb;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified width;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;
    private final ParameterPriorDistSimplified manningCoef;

    public ChannelRect() {
        this(true);
    }

    public ChannelRect(boolean kMode) {
        super(2, 5,
                "Q = K_s * B_w * S ^ (1/2)  * (h-b) ^ c",
                "Q = 1 / n * B_w * S ^ (1/2)  * (h-b) ^ c");

        kb = kMode ? CommonParameterDistSimplified.getActivationHeight()
                : CommonParameterDistSimplified.getOffsetHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        width = CommonParameterDistSimplified.getRectWidth();
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(1.67, 0.05);
        exponent.setLock(true);
        manningCoef = CommonParameterDistSimplified.getManningCoeff();

        addParameter(kb);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(slope);
        addParameter(exponent);
        addParameter(manningCoef);

        display();

        T.t(this, () -> {
            setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            kb.setNameLabel(kMode ? T.html("activation_stage") : "Offset");
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            manningCoef.setNameLabel(T.html("manning_coef"));
            width.setNameLabel(T.html("channel_width"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        Double N = manningCoef.meanValueField.isValueValid() ? manningCoef.meanValueField.getDoubleValue() : null;
        Double K = stricklerCoef.meanValueField.isValueValid() ? stricklerCoef.meanValueField.getDoubleValue() : null;
        Double S = slope.meanValueField.isValueValid() ? slope.meanValueField.getDoubleValue() : null;
        Double B = width.meanValueField.isValueValid() ? width.meanValueField.getDoubleValue() : null;
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

        if (useManning()) {
            return getAandAstdManning(N, S, B, Nstd, Sstd, Bstd);
        } else {
            return getAandAstdStrickler(K, S, B, Kstd, Sstd, Bstd);
        }

    }

    private static Double[] getAandAstdStrickler(
            Double K,
            Double S,
            Double B,
            Double Kstd,
            Double Sstd,
            Double Bstd) {

        if (B == null || S == null || K == null) {
            return new Double[] { null, null };
        }

        // mean value
        Double A = K * Math.sqrt(S) * B;

        if (Bstd == null || Sstd == null || Kstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives
        Double dAdK = A / K;
        Double dAdS = A / (2.0 * S);
        Double dAdB = A / B;

        // std
        Double Astd = Math.sqrt(Math.pow(dAdK * Kstd, 2)
                + Math.pow(dAdS * Sstd, 2)
                + Math.pow(dAdB * Bstd, 2)

        );

        return new Double[] { A, Astd };
    }

    private static Double[] getAandAstdManning(
            Double N,
            Double S,
            Double B,
            Double Nstd,
            Double Sstd,
            Double Bstd) {

        if (B == null || S == null || N == null) {
            return new Double[] { null, null };
        }

        // mean value
        Double A = 1 / N * Math.sqrt(S) * B;

        if (Bstd == null || Sstd == null || Nstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives
        Double dAdN = -A / N;
        Double dAdS = A / (2.0 * S);
        Double dAdB = A / B;

        // std
        Double Astd = Math.sqrt(Math.pow(dAdN * Nstd, 2)
                + Math.pow(dAdS * Sstd, 2)
                + Math.pow(dAdB * Bstd, 2)

        );

        return new Double[] { A, Astd };
    }

    @Override
    public KBACGaussianConfig toKACGaussianConfig() {

        Double[] AGaussianConfig = toAMeanAndStd();

        Double kMean = kb.meanValueField.getDoubleValue();
        Double kStd = kb.uncertaintyValueField.getDoubleValue();
        Double cMean = exponent.meanValueField.getDoubleValue();
        Double cStd = exponent.uncertaintyValueField.getDoubleValue();

        return new KBACGaussianConfig(
                kMean, kStd == null ? null : kStd / 2.0,
                AGaussianConfig[0], AGaussianConfig[1],
                cMean, cStd == null ? null : cStd / 2.0);
    }
}
