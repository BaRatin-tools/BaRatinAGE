package org.baratinage.ui.baratin.baratin_qfh;

import org.baratinage.ui.commons.CommonParameterDist.CommonParameterType;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SvgIcon;
import org.json.JSONObject;

public class QFHPriorParameterDist extends ParameterPriorDist {

    private static CommonParameterType[] validParameterTypes = new CommonParameterType[] {
            CommonParameterType.ACTIVATION_HEIGHT,
            CommonParameterType.OFFSET,
            CommonParameterType.WIDTH,
            CommonParameterType.DISTANCE,
            CommonParameterType.AREA,
            CommonParameterType.SLOPE,
            CommonParameterType.ANGLE,
            CommonParameterType.WEIR_COEFFICIENT,
            CommonParameterType.COEFFICIENT,
            CommonParameterType.EXPONENT,
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
        int selectedIndex = knownParameterType.getSelectedIndex();
        knownParameterType.setItems(knownParaTypeLabels, knownParaTypeIcons);
        knownParameterType.setSelectedItem(selectedIndex, true);
    }

    public void setParameterType(String id) {
        for (int k = 0; k < validParameterTypes.length; k++) {
            if (validParameterTypes[k].id.equals(id)) {
                knownParameterType.setSelectedItem(k);
                return;
            }
        }
    }

    private CommonParameterType getSelectedParameterType() {
        int index = knownParameterType.getSelectedIndex();
        if (index < 0 || index > validParameterTypes.length) {
            return null;
        }
        return validParameterTypes[index];
    }

    @Override
    public Parameter getParameter() {
        Parameter p = super.getParameter();

        // FIXME: refactor to more generic ? (e.g. part of AbstractParameterPriorDist)
        CommonParameterType t = getSelectedParameterType();

        if (p == null || t == null || !t.id.equals("angle")) {
            return p;
        }
        Distribution d = p.distribution;
        double[] convertedPar = new double[d.parameterValues.length];
        for (int k = 0; k < d.parameterValues.length; k++) {
            convertedPar[k] = Math.toRadians(d.parameterValues[k]);
        }
        Distribution dConverted = new Distribution(d.type, convertedPar);
        return new Parameter(p.name, Math.toRadians(p.initalGuess), dConverted);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        CommonParameterType t = getSelectedParameterType();
        json.put("knownParameterTypeId", t == null ? null : t.id);
        json.put("knownParameterTypeEnabled", knownParameterType.isEnabled());

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);

        String knownParameterTypeId = json.optString("knownParameterTypeId", null);
        if (knownParameterTypeId != null) {
            setParameterType(knownParameterTypeId);
        }
        boolean knownParameterTypeEnabled = json.optBoolean("knownParameterTypeEnabled", true);

        knownParameterType.setEnabled(knownParameterTypeEnabled);
    }
}
