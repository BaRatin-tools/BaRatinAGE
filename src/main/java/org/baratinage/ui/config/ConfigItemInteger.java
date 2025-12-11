package org.baratinage.ui.config;

import org.baratinage.ui.component.SimpleIntegerField;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigItemInteger extends ConfigItem<Integer, SimpleIntegerField> {

    public ConfigItemInteger(String id, boolean requireRestart, Integer defaultValue) {
        super(id, requireRestart, defaultValue);
    }

    @Override
    public void setFromJSON(JSONObject json, SCOPE scope) {
        if (!json.has(id)) {
            return;
        }
        try {
            Integer value = json.getInt(id);
            set(value, scope);
        } catch (JSONException e) {
            ConsoleLogger.error(e);
        }
    }

    @Override
    public SimpleIntegerField buildField(SCOPE scope) {
        SimpleIntegerField field = new SimpleIntegerField();
        field.setValue(values.containsKey(scope) ? values.get(scope) : defaultValue);
        field.addChangeListener(l -> {
            set(field.getIntValue(), scope);
        });
        return field;
    }

    @Override
    protected void setField(SimpleIntegerField field, Integer value) {
        field.setValue(value);
    }

}
