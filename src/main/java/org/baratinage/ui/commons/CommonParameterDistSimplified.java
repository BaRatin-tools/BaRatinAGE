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
                p.setSymbolUnitLabels("k", "m");
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
                p.setSymbolUnitLabels("K<sub>s</sub>", "m<sup>1/3</sup>.s<sup>-1</sup>");
                return p;
        }

        public static ParameterPriorDistSimplified getParabolaWidth() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(parabolaWidthIcon);
                p.setSymbolUnitLabels("B<sub>p</sub>", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getRectWidth() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(widthIcon);
                p.setSymbolUnitLabels("B<sub>r</sub>", "m");
                return p;
        }

        public static ParameterPriorDistSimplified getHeight(String sub) {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(parabolaHeightIcon);
                p.setSymbolUnitLabels("H<sub>" + sub + "</sub>", "m");
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
                p.setSymbolUnitLabels("g", "m.s<sup>-2</sup>");
                p.setDefaultValues(9.81, 0.01);
                return p;
        }

        public static ParameterPriorDistSimplified getCircleArea() {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(circleAreaIcon);
                p.setSymbolUnitLabels("A<sub>o</sub>", "m<sup>2</sup>");
                return p;
        }

        public static ParameterPriorDistSimplified getWeirCoeff(String sub) {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(weirCoefIcon);
                p.setSymbolUnitLabels("C<sub>" + sub + "</sub>", "-");
                return p;
        }

}