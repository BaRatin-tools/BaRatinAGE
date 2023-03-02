package ui.lg;

public abstract class LgElement<T> {
    public T component;

    public LgElement(T component) {
        this.component = component;
    }

    public abstract void setTranslatedText();
}
