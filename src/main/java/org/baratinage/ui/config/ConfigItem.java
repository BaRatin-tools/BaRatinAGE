package org.baratinage.ui.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.JSONObject;

public abstract class ConfigItem<A extends Object, B extends JComponent> {

    public enum SCOPE {
        GLOBAL, PROJECT
    };

    public final String id;
    public final boolean requireRestart;

    public final A defaultValue;
    protected final HashMap<SCOPE, A> values;
    protected final HashMap<SCOPE, B> fields;

    private boolean disableChangeListeners = false;

    public ConfigItem(String id, boolean requireRestart, A defaultValue) {
        this.id = id;
        this.requireRestart = requireRestart;
        this.defaultValue = defaultValue;
        this.values = new HashMap<>();
        this.fields = new HashMap<>();
    }

    public A get(SCOPE scope) {
        return values.get(scope);
    }

    public A get() {
        if (values.containsKey(SCOPE.PROJECT)) {
            return values.get(SCOPE.PROJECT);
        } else if (values.containsKey(SCOPE.GLOBAL)) {
            return values.get(SCOPE.GLOBAL);
        }
        return defaultValue;
    }

    public void set(A value, SCOPE scope, SCOPE... scopes) {
        SCOPE[] _scopes = new SCOPE[scopes.length + 1];
        _scopes[0] = scope;
        System.arraycopy(scopes, 0, _scopes, 1, scopes.length);
        for (SCOPE s : _scopes) {
            values.put(s, value);
            if (fields.containsKey(s)) {
                setField(fields.get(s), value);
            }
        }
        fireChangeListeners();
    }

    public void unset(SCOPE... scopes) {
        disableChangeListeners = true;
        for (SCOPE scope : scopes) {
            values.remove(scope);
            if (fields.containsKey(scope)) {
                setField(fields.get(scope), defaultValue);
            }
        }
        disableChangeListeners = false;
        fireChangeListeners();
    }

    public boolean isSet(SCOPE scope) {
        return values.containsKey(scope);
    }

    public abstract void setFromJSON(JSONObject json, SCOPE scope);

    protected abstract B buildField(SCOPE scope);

    protected abstract void setField(B field, A value);

    public B getField(SCOPE scope) {
        if (!fields.containsKey(scope)) {
            fields.put(scope, buildField(scope));
        }
        return fields.get(scope);
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    private void fireChangeListeners() {
        if (disableChangeListeners) {
            return;
        }
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }
}
