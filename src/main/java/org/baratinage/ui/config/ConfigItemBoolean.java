package org.baratinage.ui.config;

import javax.swing.JCheckBox;

import org.json.JSONObject;

public class ConfigItemBoolean extends ConfigItem {

    private boolean defaultValue;
    private boolean value;

    public ConfigItemBoolean(String id, boolean defaultValue, boolean requireRestart) {
        super(id, requireRestart);
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void setFromJSON(JSONObject json) {
        value = json.optBoolean(id, defaultValue);
    }

    @Override
    public JCheckBox getField() {

        JCheckBox field = new JCheckBox();
        field.setSelected(value);
        field.addChangeListener(l -> {
            value = field.isSelected();
        });

        return field;
    }

}
