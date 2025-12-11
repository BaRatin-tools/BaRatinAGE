package org.baratinage.ui.config;

import java.util.function.Supplier;

import javax.swing.JLabel;

import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigItemList extends ConfigItem<String, SimpleComboBox> {

    private final Supplier<String[]> optionsValueBuilder;
    private final Supplier<JLabel[]> optionsLabelBuilder;

    public ConfigItemList(
            String id,
            boolean requireRestart,
            String defaultValue,
            Supplier<String[]> optionsValueBuilder,
            Supplier<JLabel[]> optionsLabelBuilder) {
        super(id, requireRestart, defaultValue);
        this.optionsValueBuilder = optionsValueBuilder;
        this.optionsLabelBuilder = optionsLabelBuilder;
    }

    private int getIndexFromValue(String id) {
        String[] ids = optionsValueBuilder.get();
        for (int k = 0; k < ids.length; k++) {
            if (ids[k].equals(id)) {
                return k;
            }
        }
        return -1;
    }

    private String getValueFromIndex(int index) {
        String[] ids = optionsValueBuilder.get();
        return index >= 0 && index < ids.length ? ids[index] : "";
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
    protected SimpleComboBox buildField(SCOPE scope) {
        SimpleComboBox field = new SimpleComboBox();
        field.setItems(optionsLabelBuilder.get(), true);
        field.setSelectedItem(
                getIndexFromValue(
                        values.containsKey(scope) ? values.get(scope) : defaultValue));
        field.addChangeListener(l -> {
            set(getValueFromIndex(field.getSelectedIndex()), scope);
        });

        return field;
    }

    @Override
    protected void setField(SimpleComboBox field, String value) {
        field.setSelectedItem(getIndexFromValue(value));
    }

}
