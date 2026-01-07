package org.baratinage.ui.config;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;

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
        notifySubscribers();
    }

    public void unset(SCOPE... scopes) {
        doNotNotifySubsribers = true;
        for (SCOPE scope : scopes) {
            if (fields.containsKey(scope)) {
                setField(fields.get(scope), defaultValue);
            }
            values.remove(scope);
        }
        doNotNotifySubsribers = false;
        notifySubscribers();
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

    // ---------------------
    // Subscriber management
    // ---------------------

    private boolean doNotNotifySubsribers = false;

    private class Entry {
        final WeakReference<Object> owner;
        final Consumer<A> subscriber;

        Entry(Object owner, Consumer<A> subscriber) {
            this.owner = new WeakReference<>(owner);
            this.subscriber = subscriber;
        }
    }

    private final List<Entry> subscribers = new ArrayList<>();

    public void subscribe(Object owner, Consumer<A> subscriber) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(subscriber);
        subscribers.add(new Entry(owner, subscriber));
        subscriber.accept(get());
    }

    public void unsubscribe(Object owner) {
        subscribers.removeIf(e -> e.owner.get() == owner);
    }

    private void notifySubscribers() {
        if (doNotNotifySubsribers) {
            return;
        }
        Iterator<Entry> it = subscribers.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (e.owner.get() == null) {
                it.remove();
            } else {
                e.subscriber.accept(get());
            }
        }
    }

}
