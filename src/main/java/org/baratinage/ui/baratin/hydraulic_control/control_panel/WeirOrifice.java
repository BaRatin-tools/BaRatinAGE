package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.translation.T;

public class WeirOrifice extends PriorControlPanel {

    private final ParameterPriorDistSimplified activationHeight;
    private final ParameterPriorDistSimplified weirCoef;
    private final ParameterPriorDistSimplified area;
    private final ParameterPriorDistSimplified gravity;
    private final ParameterPriorDistSimplified exponent;

    public WeirOrifice() {
        super(
                2,
                "C<sub>o</sub>A<sub>o</sub>(2g)<sup>1/2</sup>(h-b)<sup>c</sup>&nbsp;(h>k)");

        activationHeight = new ParameterPriorDistSimplified();
        activationHeight.setIcon(activationHeightIcon);
        activationHeight.setSymbolUnitLabels("k", "m");

        weirCoef = new ParameterPriorDistSimplified();
        weirCoef.setIcon(weirCoefOIcon);
        weirCoef.setSymbolUnitLabels("C<sub>o</sub>", "-");
        weirCoef.setDefaultValues(0.4, 0.05);

        area = new ParameterPriorDistSimplified();
        area.setIcon(areaIcon);
        area.setSymbolUnitLabels("A<sub>o</sub>", "m<sup>2</sup>");

        gravity = new ParameterPriorDistSimplified();
        gravity.setIcon(gravityIcon);
        gravity.setSymbolUnitLabels("g", "m.s<sup>-2</sup>");
        gravity.setDefaultValues(9.81, 0.01);
        gravity.setLocalLock(true);

        exponent = new ParameterPriorDistSimplified();
        exponent.setIcon(exponentIcon);
        exponent.setSymbolUnitLabels("c", "-");
        exponent.setDefaultValues(0.5, 0.05);
        exponent.setLocalLock(true);

        addParameter(activationHeight);
        addParameter(weirCoef);
        addParameter(area);
        addParameter(gravity);
        addParameter(exponent);

        T.t(this, (wRect) -> {
            wRect.setHeaders(
                    T.html("mean_value"),
                    T.html("uncertainty_value"));
            wRect.activationHeight.setNameLabel(T.html("activation_stage"));
            wRect.weirCoef.setNameLabel(T.html("weir_coefficient"));
            wRect.area.setNameLabel(T.html("orifice_area"));
            wRect.gravity.setNameLabel(T.html("gravity_acceleration"));
            wRect.exponent.setNameLabel(T.html("exponent"));
        });

    }

    private Double[] toAMeanAndStd() {

        if (!weirCoef.meanValueField.isValueValid() ||
                !area.meanValueField.isValueValid() ||
                !gravity.meanValueField.isValueValid()) {
            return new Double[] { null, null };
        }

        double C = weirCoef.meanValueField.getDoubleValue();
        double AR = area.meanValueField.getDoubleValue();
        double G = gravity.meanValueField.getDoubleValue();

        double sqrtOfTwoG = Math.sqrt(2 * G);

        double A = C * AR * sqrtOfTwoG;

        if (!weirCoef.uncertaintyValueField.isValueValid() ||
                !area.uncertaintyValueField.isValueValid() ||
                !gravity.uncertaintyValueField.isValueValid()) {
            return new Double[] { A, null };
        }

        double Cstd = weirCoef.uncertaintyValueField.getDoubleValue() / 2;
        double ARstd = area.uncertaintyValueField.getDoubleValue() / 2;
        double Gstd = gravity.uncertaintyValueField.getDoubleValue() / 2;

        double Astd = Math.sqrt(
                Math.pow(Cstd, 2) * Math.pow(AR * sqrtOfTwoG, 2) +
                        Math.pow(ARstd, 2) * Math.pow(C * sqrtOfTwoG, 2) +
                        Math.pow(Gstd, 2) * Math.pow(AR * C * Math.pow(2 * G, -1 / 2), 2));

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
