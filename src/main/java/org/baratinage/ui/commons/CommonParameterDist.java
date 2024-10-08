package org.baratinage.ui.commons;

import org.baratinage.AppSetup;
import org.baratinage.ui.component.SvgIcon;

public class CommonParameterDist {

        public enum CommonParameterType {

                ACTIVATION_HEIGHT(
                                "activation_stage",
                                AppSetup.ICONS.getCustomAppImageIcon("activation_height.svg"),
                                "m", "k"),
                SLOPE(
                                "slope",
                                AppSetup.ICONS.getCustomAppImageIcon("slope.svg"),
                                "-", "S"),
                STRICKLER_COEFFICIENT(
                                "strickler_coef",
                                AppSetup.ICONS.getCustomAppImageIcon("strickler_coef.svg"),
                                "m<sup>1/3</sup>.s<sup>-1</sup>", "K<sub>s</sub>"),
                ANGLE(
                                "angle",
                                AppSetup.ICONS.getCustomAppImageIcon("angle.svg"),
                                "°", "v"),
                WIDTH(
                                "width",
                                AppSetup.ICONS.getCustomAppImageIcon("width.svg"),
                                "m", "B<sub>r</sub>"),
                PARABOLA_WIDTH(
                                "parabola_width",
                                AppSetup.ICONS.getCustomAppImageIcon("parabola_width.svg"),
                                "m", "B<sub>p</sub>"),
                PARABOLA_HEIGHT(
                                "parabola_height",
                                AppSetup.ICONS.getCustomAppImageIcon("parabola_height.svg"),
                                "m", "H<sub>p</sub>"),
                ORIFICE_AREA(
                                "orifice_area",
                                AppSetup.ICONS.getCustomAppImageIcon("orifice_area.svg"),
                                "m<sup>2</sup>", "A<sub>o</sub>"),
                AREA(
                                "area",
                                AppSetup.ICONS.getCustomAppImageIcon("area.svg"),
                                "m<sup>2</sup>", "A"),
                GRAVITY(
                                "gravity_acceleration",
                                AppSetup.ICONS.getCustomAppImageIcon("gravity.svg"),
                                "m.s<sup>-2</sup>", "g"),
                EXPONENT(
                                "exponent",
                                AppSetup.ICONS.getCustomAppImageIcon("exponent.svg"),
                                "-", "c"),
                WEIR_COEFFICIENT(
                                "weir_coefficient",
                                AppSetup.ICONS.getCustomAppImageIcon("weir_coeff.svg"),
                                "-", "C<sub>r</sub>");

                public final String id;
                public final SvgIcon icon;
                public final String unit;
                public final String defaultSymbole;

                private CommonParameterType(String id, SvgIcon icon, String unit, String defaultSymbole) {
                        this.id = id;
                        this.icon = icon;
                        this.unit = unit;
                        this.defaultSymbole = defaultSymbole;
                }
        }

        public ParameterPriorDistSimplified buildParameterPriorDistSimplified(CommonParameterType type) {
                ParameterPriorDistSimplified p = new ParameterPriorDistSimplified();
                p.setIcon(type.icon);
                p.setSymbolUnitLabels(type.defaultSymbole, type.unit);
                return p;
        }

        public ParameterPriorDistSimplified buildParameterPriorDistSimplified(
                        CommonParameterType type,
                        String symbole) {
                ParameterPriorDistSimplified p = buildParameterPriorDistSimplified(type);
                p.setSymbolUnitLabels(symbole, type.unit);
                return p;
        }

        public ParameterPriorDist buildParameterPriorDist(CommonParameterType type, String bamName) {
                ParameterPriorDist p = new ParameterPriorDist(bamName);
                p.setIcon(type.icon);
                p.setSymbolUnitLabels(bamName, type.unit);
                return p;
        }

}
