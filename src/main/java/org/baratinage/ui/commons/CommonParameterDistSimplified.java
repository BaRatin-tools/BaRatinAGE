package org.baratinage.ui.commons;

import org.baratinage.AppSetup;
import org.baratinage.ui.component.SvgIcon;

public class CommonParameterDistSimplified {

        private static final SvgIcon activationHeightIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("activation_height.svg");

        private static final SvgIcon slopeIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("slope.svg");

        private static final SvgIcon weirCoefIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("weir_coeff.svg");

        private static final SvgIcon stricklerCoefIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("strickler_coef.svg");

        private static final SvgIcon manningCoefIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("strickler_coef.svg");

        private static final SvgIcon angleIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("angle.svg");

        private static final SvgIcon widthIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("width.svg");

        private static final SvgIcon parabolaWidthIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("parabola_width.svg");

        private static final SvgIcon parabolaHeightIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("parabola_height.svg");

        private static final SvgIcon circleAreaIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("orifice_area.svg");

        private static final SvgIcon gravityIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("gravity.svg");

        private static final SvgIcon exponentIcon = AppSetup.ICONS
                        .getCustomAppImageIcon("exponent.svg");

        public static ParameterPriorDistSimplified getActivationHeight() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(activationHeightIcon);
                p.setSymbolUnitLabels("κ", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getOffsetHeight() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(activationHeightIcon);
                p.setSymbolUnitLabels("b", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getExponent() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(exponentIcon);
                p.setSymbolUnitLabels("c", "-");
                return p;
        }

        public static ParameterPriorDistSimplified getStricklerCoeff() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(stricklerCoefIcon);
                p.setSymbolUnitLabels("K_s", "m ^ (1/3) * s ^ (-1)");
                return p;
        }

        public static ParameterPriorDistSimplified getManningCoeff() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(manningCoefIcon);
                p.setSymbolUnitLabels("n", "s / m ^ (1/3)");
                return p;
        }

        public static ParameterPriorDistSimplified getParabolaWidth() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(parabolaWidthIcon);
                p.setSymbolUnitLabels("B_p", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getRectWidth() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(widthIcon);
                p.setSymbolUnitLabels("B_w", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getHeight(String sub) {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(parabolaHeightIcon);
                p.setSymbolUnitLabels("H_" + sub, "m");
                return p;
        }

        public static ParameterPriorDistSimplified getSlope() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(slopeIcon);
                p.setSymbolUnitLabels("S", "-");
                return p;
        }

        public static ParameterPriorDistSimplified getAngle() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(angleIcon);
                p.setSymbolUnitLabels("v", "°");
                return p;
        }

        public static ParameterPriorDistSimplified getGravity() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(gravityIcon);
                p.setSymbolUnitLabels("g", "m * s ^ (-2)");
                p.setDefaultValues(9.81, 0.01);
                return p;
        }

        public static ParameterPriorDistSimplified getCircleArea() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(circleAreaIcon);
                p.setSymbolUnitLabels("A_o", "m ^ 2");
                return p;
        }

        public static ParameterPriorDistSimplified getWeirCoeff(String sub) {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(weirCoefIcon);
                p.setSymbolUnitLabels("C_" + sub, "-");
                return p;
        }

}