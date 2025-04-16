package org.baratinage.ui.config;

import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.baratinage.ui.component.SimpleComboBox;
import org.json.JSONObject;

public class ConfigItemList extends ConfigItem {

    private final Supplier<String[]> optionsValueBuilder;
    private final Supplier<JLabel[]> optionsLabelBuilder;
    private final String defaultValue;
    private String value;

    public ConfigItemList(
            String id,
            String defaultValue,
            Supplier<String[]> optionsValueBuilder,
            Supplier<JLabel[]> optionsLabelBuilder, boolean requireRestart) {
        super(id, requireRestart);

        this.optionsValueBuilder = optionsValueBuilder;
        this.optionsLabelBuilder = optionsLabelBuilder;
        this.defaultValue = defaultValue;
        value = defaultValue;
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

    public void set(String id) {
        int index = getIndexFromValue(id);
        if (index >= 0) {
            value = id;
        }
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
    public JComponent getField() {

        SimpleComboBox field = new SimpleComboBox();
        field.setItems(optionsLabelBuilder.get(), true);
        field.setSelectedItem(getIndexFromValue(value));
        field.addChangeListener(l -> {
            value = getValueFromIndex(field.getSelectedIndex());
        });

        return field;
    }

}
