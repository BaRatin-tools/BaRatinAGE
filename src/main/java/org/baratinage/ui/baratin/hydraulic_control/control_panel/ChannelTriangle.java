package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class ChannelTriangle extends ChannelPriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified stricklerCoef;
    private final ParameterPriorDistSimplified angle;
    private final ParameterPriorDistSimplified slope;
    private final ParameterPriorDistSimplified exponent;
    private final ParameterPriorDistSimplified manningCoef;

    public ChannelTriangle() {
        super(2, 5,
                "Q = K_s  * tan(v/2) * sqrt(S) * (sin(v/2) / 2) ^ (2/3)  * (h-b) ^ c",
                "Q = 1 / n  * tan(v/2) * sqrt(S) * (sin(v/2) / 2) ^ (2/3)  * (h-b) ^ c");

        activationHeight = CommonParameterDistSimplified.getActivationHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        angle = CommonParameterDistSimplified.getAngle();
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(2.67, 0.05);
        exponent.setLock(true);
        manningCoef = CommonParameterDistSimplified.getManningCoeff();

        addParameter(activationHeight);
        addParameter(stricklerCoef);
        addParameter(angle);
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
            angle.setNameLabel(T.html("angle"));
            slope.setNameLabel(T.html("channel_slope"));
            exponent.setNameLabel(T.html("exponent"));
        });
    }

    private Double[] toAMeanAndStd() {

        Double N = manningCoef.meanValueField.isValueValid() ? manningCoef.meanValueField.getDoubleValue() : null;
        Double K = stricklerCoef.meanValueField.isValueValid() ? stricklerCoef.meanValueField.getDoubleValue() : null;
        Double V = angle.meanValueField.isValueValid() ? Math.toRadians(angle.meanValueField.getDoubleValue()) : null;
        Double S = slope.meanValueField.isValueValid() ? slope.meanValueField.getDoubleValue() : null;
        Double Nstd = manningCoef.uncertaintyValueField.isValueValid()
                ? manningCoef.uncertaintyValueField.getDoubleValue() / 2.0
                : null;
        Double Kstd = stricklerCoef.uncertaintyValueField.isValueValid()
                ? stricklerCoef.uncertaintyValueField.getDoubleValue() / 2.0
                : null;
        Double Vstd = angle.uncertaintyValueField.isValueValid()
                ? Math.toRadians(angle.uncertaintyValueField.getDoubleValue()) / 2.0
                : null;
        Double Sstd = slope.uncertaintyValueField.isValueValid() ? slope.uncertaintyValueField.getDoubleValue() / 2.0
                : null;

        if (useManning()) {
            return getAandAstdManning(V, S, N, Vstd, Sstd, Nstd);
        } else {
            return getAandAstdStrickler(V, S, K, Vstd, Sstd, Kstd);
        }
    }

    private static Double[] getAandAstdStrickler(
            Double V,
            Double S,
            Double K,
            Double Vstd,
            Double Sstd,
            Double Kstd) {

        if (V == null || S == null || K == null) {
            return new Double[] { null, null };
        }

        double halfV = V / 2.0;

        double sqrtS = Math.sqrt(S);
        double sinHalfV = Math.sin(halfV);
        double cosHalfV = Math.cos(halfV);
        double tanHalfV = Math.tan(halfV);

        // mean value
        double A = K *
                sqrtS *
                tanHalfV *
                Math.pow(sinHalfV / 2.0, 2.0 / 3.0);

        if (Vstd == null || Sstd == null || Kstd == null) {
            return new Double[] { A, null };
        }

        // partial derivatives

        // Ks
        double dAdKs = A / K;

        // S
        double dAdS = A / (2.0 * S);

        // v
        double dtan = 1.0 / 2.0 * 1.0 / Math.pow(cosHalfV, 2.0);
        double dpow = 1.0 / 6.0 * cosHalfV / Math.pow(1.0 / 2.0 * sinHalfV, 1.0 / 3.0);
        double dV = dtan * Math.pow(sinHalfV / 2.0, 2.0 / 3.0) + dpow * tanHalfV;
        double dAdV = K * sqrtS * dV;

        // standard deviation
        double Astd = Math.sqrt(Math.pow(dAdKs * Kstd, 2) +
                Math.pow(dAdS * Sstd, 2) +
                Math.pow(dAdV * Vstd, 2));

        return new Double[] { A, Astd };
    }

    private static Double[] getAandAstdManning(
            Double V,
            Double S,
            Double N,
            Double Vstd,
            Double Sstd,
            Double Nstd) {

        if (V == null || S == null || N == null) {
            return new Double[] { null, null };
        }

        double halfV = V / 2.0;

        double sqrtS = Math.sqrt(S);
        double sinHalfV = Math.sin(halfV);
        double cosHalfV = Math.cos(halfV);
        double tanHalfV = Math.tan(halfV);

        // Mean value
        double A = (1.0 / N) *
                sqrtS *
                tanHalfV *
                Math.pow(sinHalfV / 2.0, 2.0 / 3.0);

        if (Vstd == null || Sstd == null || Nstd == null) {
            return new Double[] { A, null };
        }

        // Partial derivatives

        // N
        double dAdN = -A / N;

        // S
        double dAdS = A / (2.0 * S);

        // V (same structure as before)
        double dtan = 1.0 / (2.0 * Math.pow(cosHalfV, 2.0));

        double dpow = (1.0 / 6.0) *
                cosHalfV /
                Math.pow(sinHalfV / 2.0, 1.0 / 3.0);

        double dV = dtan * Math.pow(sinHalfV / 2.0, 2.0 / 3.0)
                + dpow * tanHalfV;

        double dAdV = (1.0 / N) * sqrtS * dV;

        // Standard deviation
        double Astd = Math.sqrt(Math.pow(dAdN * Nstd, 2) +
                Math.pow(dAdS * Sstd, 2) +
                Math.pow(dAdV * Vstd, 2));

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
