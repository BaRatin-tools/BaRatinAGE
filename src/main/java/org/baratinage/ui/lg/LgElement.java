package org.baratinage.ui.lg;

public abstract class LgElement<T> {

    public final T object;

    public LgElement(T obj) {
        object = obj;
    }

    public abstract void setTranslatedText();
}
