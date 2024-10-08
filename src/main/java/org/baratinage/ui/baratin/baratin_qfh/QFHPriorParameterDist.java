package org.baratinage.ui.baratin.baratin_qfh;

import org.baratinage.ui.commons.CommonParameterDist.CommonParameterType;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SvgIcon;

public class QFHPriorParameterDist extends ParameterPriorDist {

    private static CommonParameterType[] validParameterTypes = new CommonParameterType[] {
            CommonParameterType.ACTIVATION_HEIGHT,
            CommonParameterType.WIDTH,
            CommonParameterType.EXPONENT,
            CommonParameterType.WEIR_COEFFICIENT,
            CommonParameterType.AREA,
            CommonParameterType.SLOPE,
            CommonParameterType.ANGLE,
            CommonParameterType.GRAVITY,
            CommonParameterType.STRICKLER_COEFFICIENT,
    };

    public final SimpleComboBox knownParameterType;

    public QFHPriorParameterDist(String bamName) {
        super(bamName);

        setNameLabel(bamName);

        knownParameterType = new SimpleComboBox();

        setKnownParameterTypeCombobox();

        T.t(this, () -> {
            setKnownParameterTypeCombobox();
        });
    }

    private void setKnownParameterTypeCombobox() {

        String[] knownParaTypeLabels = new String[validParameterTypes.length];
        SvgIcon[] knownParaTypeIcons = new SvgIcon[validParameterTypes.length];
        for (int k = 0; k < validParameterTypes.length; k++) {
            knownParaTypeLabels[k] = String.format("<html>%s (%s)</html>", T.text(validParameterTypes[k].id),
                    validParameterTypes[k].unit);
            knownParaTypeIcons[k] = validParameterTypes[k].icon;
        }
        knownParameterType.setItems(knownParaTypeLabels, knownParaTypeIcons);
    }

    public void setParameterType(String id) {
        for (int k = 0; k < validParameterTypes.length; k++) {
            if (validParameterTypes[k].id.equals(id)) {
                knownParameterType.setSelectedItem(k);
                return;
            }
        }
    }

    // public void setParameterTypeEnabled(boolean enabled) {
    // knownParameterType.setEnabled(enabled);
    // if (enabled)
    // }

}
