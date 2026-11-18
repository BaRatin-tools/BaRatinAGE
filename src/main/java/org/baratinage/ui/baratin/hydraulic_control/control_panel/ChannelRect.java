package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.translation.T;
import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;

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
        super(
                2,
                5,
                "Q = K_s * B_w * sqrt(S) * (h-b) ^ c",
                "Q = 1 / n * B_w * sqrt(S) * (h-b) ^ c");

        kb = kMode
                ? CommonParameterDistSimplified.getActivationHeight()
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
            setHeaders(T.html("mean_value"), T.html("uncertainty_value"));
            kb.setNameLabel(kMode ? T.html("activation_stage") : "Offset");
            stricklerCoef.setNameLabel(T.html("strickler_coef"));
            manningCoef.setNameLabel(T.html("manning_coef"));
            width.setNameLabel(T.html("channel_width"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    @Override
    protected Double[] toAMeanAndStd() {
        Double N = manningCoef.getMeanValue();
        Double K = stricklerCoef.getMeanValue();
        Double S = slope.getMeanValue();
        Double B = width.getMeanValue();

        Double Nstd = manningCoef.getStdValue();
        Double Kstd = stricklerCoef.getStdValue();
        Double Sstd = slope.getStdValue();
        Double Bstd = width.getStdValue();

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
        Double Astd = Math.sqrt(
                Math.pow(dAdK * Kstd, 2) +
                        Math.pow(dAdS * Sstd, 2) +
                        Math.pow(dAdB * Bstd, 2));

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
        Double A = (1 / N) * Math.sqrt(S) * B;

        if (Bstd == null || Sstd == null || Nstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives
        Double dAdN = -A / N;
        Double dAdS = A / (2.0 * S);
        Double dAdB = A / B;

        // std
        Double Astd = Math.sqrt(
                Math.pow(dAdN * Nstd, 2) +
                        Math.pow(dAdS * Sstd, 2) +
                        Math.pow(dAdB * Bstd, 2));

        return new Double[] { A, Astd };
    }

    @Override
    protected ParameterPriorDistSimplified getKB() {
        return kb;
    }

    @Override
    protected ParameterPriorDistSimplified getExponent() {
        return exponent;
    }
}
