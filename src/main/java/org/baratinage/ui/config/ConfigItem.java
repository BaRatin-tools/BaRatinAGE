package org.baratinage.ui.config;

import javax.swing.JComponent;

import org.json.JSONObject;

public abstract class ConfigItem {

    public final String id;
    public final boolean requireRestart;

    public ConfigItem(String id, boolean requireRestart) {
        this.id = id;
        this.requireRestart = requireRestart;
    }

    public abstract Object get();

    public abstract void setFromJSON(JSONObject json);

    public abstract JComponent getField();

}
