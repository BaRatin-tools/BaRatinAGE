package org.baratinage.ui.config;

import org.baratinage.ui.component.SimpleTextField;
import org.json.JSONObject;

public class ConfigItemString extends ConfigItem {

    private final String defaultValue;
    private String value;

    public ConfigItemString(String id, String defaultValue, boolean requireRestart) {
        super(id, requireRestart);
        this.defaultValue = defaultValue;
        value = defaultValue;

    }

    public void set(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public void setFromJSON(JSONObject json) {
        value = json.optString(id, defaultValue);
    }

    @Override
    public SimpleTextField getField() {
        SimpleTextField field = new SimpleTextField();
        field.setText(value);
        value = field.getText();
        return field;
    }

}
