package org.baratinage.ui.config;

import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigItemString extends ConfigItem<String, SimpleTextField> {

    public ConfigItemString(String id, boolean requireRestart, String defaultValue) {
        super(id, requireRestart, defaultValue);
    }

    @Override
    public void setFromJSON(JSONObject json, SCOPE scope) {
        if (!json.has(id)) {
            return;
        }
        try {
            String value = json.getString(id);
            set(value, scope);
        } catch (JSONException e) {
            ConsoleLogger.error(e);
        }
    }

    @Override
    protected SimpleTextField buildField(SCOPE scope) {
        SimpleTextField field = new SimpleTextField();
        field.setText(values.containsKey(scope) ? values.get(scope) : defaultValue);
        field.addChangeListener(l -> {
            set(field.getText(), scope);
        });
        return field;
    }

    @Override
    protected void setField(SimpleTextField field, String value) {
        field.setText(value);
    }

}
