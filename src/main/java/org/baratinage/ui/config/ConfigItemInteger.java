package org.baratinage.ui.config;

import org.baratinage.ui.component.SimpleIntegerField;
import org.json.JSONObject;

public class ConfigItemInteger extends ConfigItem {

    private final Integer defaultValue;
    private Integer value;

    public ConfigItemInteger(String id, Integer defaultValue, boolean requireRestart) {
        super(id, requireRestart);

        this.defaultValue = defaultValue;
        value = defaultValue;

    }

    @Override
    public Integer get() {
        return value;
    }

    @Override
    public void setFromJSON(JSONObject json) {
        value = json.optInt(id, defaultValue);
    }

    @Override
    public SimpleIntegerField getField() {
        SimpleIntegerField field = new SimpleIntegerField();
        field.setValue(value);
        field.addChangeListener(l -> {
            value = field.getIntValue();
        });
        return field;
    }
}
