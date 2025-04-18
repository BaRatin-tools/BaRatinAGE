package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.AppSetup;
import org.baratinage.jbam.DistributionType;
import org.baratinage.ui.commons.ParameterPriorDist;
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
        super(
                3, "Q=a(h-b)<sup>c</sup> (h>\u03BA)");

        kb = new ParameterPriorDist("k");
        kb.setIcon(activationHeightIcon);
        kb.setSymbolUnitLabels(kMode ? "\u03BA" : "b", "m");

        a = new ParameterPriorDist("a");
        a.setIcon(coefficientIcon);
        a.setSymbolUnitLabels("a", "m<sup>3</sup>.s<sup>-1</sup>");

        c = new ParameterPriorDist("c");
        c.setIcon(exponentIcon);
        c.setSymbolUnitLabels("c", "-");

        addParameter(kb);
        addParameter(a);
        addParameter(c);

        T.t(this, () -> {
            setHeaders(
                    T.html("distribution"),
                    T.html("distribution_parameters"),
                    T.html("initial_guess"));
        });
    }

    public void setAToGaussian(Double mean, Double std) {
        a.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        a.distributionField.setParameters(mean, std);
    }

    public void setFromKACGaussianConfig(KACGaussianConfig kbacCongig) { // FIXME: rename method and parameter
        kb.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        kb.distributionField.setParameters(kbacCongig.kMean(), kbacCongig.kStd());
        kb.initialGuessField.setValue(kbacCongig.kMean());
        a.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        a.distributionField.setParameters(kbacCongig.aMean(), kbacCongig.aStd());
        a.initialGuessField.setValue(kbacCongig.aMean());
        c.distributionField.setDistributionType(DistributionType.GAUSSIAN);
        c.distributionField.setParameters(kbacCongig.cMean(), kbacCongig.cStd());
        c.initialGuessField.setValue(kbacCongig.cMean());

        fireChangeListeners();
    }

    @Override
    public void setGlobalLock(boolean lock) {
        super.setGlobalLock(lock);
        lockLabel.setVisible(!lock);
    }

    @Override
    public KACGaussianConfig toKACGaussianConfig() {
        // irrelevant
        return null;
    }

}
