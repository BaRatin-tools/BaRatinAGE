package org.baratinage.ui.config;

import javax.swing.JComponent;

import org.json.JSONObject;

public abstract class ConfigItem {

    public final String id;

    public ConfigItem(String id) {
        this.id = id;
    }

    public abstract Object get();

    public abstract void setFromJSON(JSONObject json);

    public abstract JComponent getField();

}
