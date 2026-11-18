package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.AppSetup;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.translation.T;

public class KBAC extends PriorControlPanel {

    public final ParameterPriorDist kb;
    public final ParameterPriorDist a;
    public final ParameterPriorDist c;

    private static final SvgIcon activationHeightIcon = AppSetup.ICONS
            .getCustomAppImageIcon("activation_height.svg");

    private static final SvgIcon exponentIcon = AppSetup.ICONS
            .getCustomAppImageIcon("exponent.svg");

    private static final SvgIcon coefficientIcon = AppSetup.ICONS
            .getCustomAppImageIcon("coefficient.svg");

    public KBAC(boolean kMode) {
        super(3, 3, "Q = a * (h-b) ^ c");// \\quad (h > κ)");

        kb = new ParameterPriorDist("k");
        kb.setIcon(activationHeightIcon);
        kb.setSymbolUnitLabels(kMode ? "κ" : "b", "m");

        a = new ParameterPriorDist("a");
        a.setIcon(coefficientIcon);
        a.setSymbolUnitLabels("a", "m ^ 3 * s ^ (-1)");

        c = new ParameterPriorDist("c");
        c.setIcon(exponentIcon);
        c.setSymbolUnitLabels("c", "-");

        addParameter(kb);
        addParameter(a);
        addParameter(c);

        display();

        T.t(this, () -> {
            setHeaders(
                    T.html("distribution"),
                    T.html("distribution_parameters"),
                    T.html("initial_guess"));
        });
    }

    public void setFromPhysicalParameter(PhysicalPriorControlPanel physicalParameter) {

        ParameterPriorDistSimplified ppdKB = physicalParameter.getKB();
        if (!ppdKB.isVariable()) {
            kb.fromGaussian(ppdKB.getMeanValue(), ppdKB.getStdValue());
        } else {
            kb.fromGaussian(ppdKB.getPeriodSet(), ppdKB.getMeanValues(), ppdKB.getStdValues());
        }
        Double[] parA = physicalParameter.toAMeanAndStd();
        a.fromGaussian(parA[0], parA[1]);
        ParameterPriorDistSimplified ppdC = physicalParameter.getExponent();
        c.fromGaussian(ppdC.getMeanValue(), ppdC.getStdValue());
        fireChangeListeners();
    }

}
