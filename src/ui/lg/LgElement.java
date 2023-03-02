package ui.lg;

import java.awt.Component;

public abstract class LgElement<T> {
    public T component;

    public LgElement(T component) {
        this.component = component;
    }

    public abstract void setTranslatedText();
}
