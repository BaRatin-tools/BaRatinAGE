package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.CommonParameterDistSimplified;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class ChannelRect extends PriorControlPanel {

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
        super(2, 5, "Q = K_s * B_w * S ^ (1/2)  * (h-b) ^ c");

        kb = kMode ? CommonParameterDistSimplified.getActivationHeight()
                : CommonParameterDistSimplified.getOffsetHeight();
        stricklerCoef = CommonParameterDistSimplified.getStricklerCoeff();
        width = CommonParameterDistSimplified.getRectWidth();
        slope = CommonParameterDistSimplified.getSlope();
        exponent = CommonParameterDistSimplified.getExponent();
        exponent.setDefaultValues(1.67, 0.05);
        exponent.setLock(true);
        manningCoef = CommonParameterDistSimplified.getManningCoeff();

        manningCoef.meanValueField.addChangeListener(l -> {
            Double d = manningCoef.meanValueField.getDoubleValue();
            stricklerCoef.meanValueField.setValue(
                    d == null || d == 0 || d == Double.NaN ? Double.NaN : 1 / d);
        });

        stricklerCoef.meanValueField.addChangeListener(l -> {
            Double d = stricklerCoef.meanValueField.getDoubleValue();
            manningCoef.meanValueField.setValue(
                    d == null || d == 0 || d == Double.NaN ? Double.NaN : 1 / d);
        });

        manningCoef.uncertaintyValueField.addChangeListener(l -> {
            Double d = manningCoef.uncertaintyValueField.getDoubleValue();
            Double mean = manningCoef.meanValueField.getDoubleValue();
            if (d == null || mean == null || mean == 0) {
                stricklerCoef.uncertaintyValueField.setValue(Double.NaN);
                return;
            }
            Double std = d / 2;
            Double stricklerStd = Math.abs(std / (mean * mean));
            stricklerCoef.uncertaintyValueField.setValue(stricklerStd * 2);
        });

        stricklerCoef.uncertaintyValueField.addChangeListener(l -> {
            Double d = stricklerCoef.uncertaintyValueField.getDoubleValue();
            Double mean = stricklerCoef.meanValueField.getDoubleValue();
            if (d == null || mean == null || mean == 0) {
                manningCoef.uncertaintyValueField.setValue(Double.NaN);
                return;
            }
            Double std = d / 2;
            Double manningStd = Math.abs(std / (mean * mean));
            manningCoef.uncertaintyValueField.setValue(manningStd * 2);
        });

        addParameter(kb);
        addParameter(stricklerCoef);
        addParameter(width);
        addParameter(slope);
        addParameter(exponent);

        AppSetup.CONFIG.USE_MANNING_COEF.subscribe(this, v -> {
            display(kb, v ? manningCoef : stricklerCoef, width, slope, exponent, v ? stricklerCoef : manningCoef);
        });

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

        double Astd = Math.sqrt(Math.pow(W * sqrtOfSlope * Kstd, 2) +
                Math.pow(K * sqrtOfSlope * Wstd, 2) +
                Math.pow(K * W / (2 * sqrtOfSlope) * Sstd, 2));

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
                kMean, kStd == null ? null : kStd / 2,
                AGaussianConfig[0], AGaussianConfig[1],
                cMean, cStd == null ? null : cStd / 2);
    }
}
