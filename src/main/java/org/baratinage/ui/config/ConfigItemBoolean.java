package org.baratinage.ui.config;

import javax.swing.JCheckBox;

import org.baratinage.utils.ConsoleLogger;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigItemBoolean extends ConfigItem<Boolean, JCheckBox> {

    public ConfigItemBoolean(String id, boolean requireRestart, Boolean defaultValue) {
        super(id, requireRestart, defaultValue);
    }

    @Override
    public void setFromJSON(JSONObject json, SCOPE scope) {
        if (!json.has(id)) {
            return;
        }
        try {
            Boolean value = json.getBoolean(id);
            set(value, scope);
        } catch (JSONException e) {
            ConsoleLogger.error(e);
        }
    }

    @Override
    protected JCheckBox buildField(SCOPE scope) {
        JCheckBox field = new JCheckBox();
        field.setSelected(values.containsKey(scope) ? values.get(scope) : defaultValue);
        field.addItemListener(l -> {
            set(field.isSelected(), scope);
        });
        return field;
    }

    @Override
    protected void setField(JCheckBox field, Boolean value) {
        field.setSelected(value);
    }

}
